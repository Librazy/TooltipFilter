package org.librazy.tooltipfilter;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.intellij.lang.annotations.Language;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Tooltip Filter.
 */
@Mod(modid = TooltipFilter.MODID, version = TooltipFilter.VERSION, name = TooltipFilter.MODNAME, clientSideOnly = true, canBeDeactivated = true, dependencies = TooltipFilter.dependencies, updateJSON = TooltipFilter.updateJSON, guiFactory = TooltipFilter.GUI)
public class TooltipFilter {
    /**
     * Version number must be changed in 3 spots before releasing a build:
     * <br><ol>
     * <li>VERSION
     * <li>src/main/resources/mcmod.info:"version", "mcversion"
     * <li>build.gradle:version
     * </ol>
     * If incrementing the Minecraft version, also update "curseFilenameParser" in AddVersionChecker()
     */
    public static final String VERSION = "@VERSION@";
    /**
     * The constant MODID.
     */
    public static final String MODID = "tooltipfilter";
    /**
     * The constant MODNAME.
     */
    public static final String MODNAME = "Tooltip Filter";

    public static final String GUI = "org.librazy.tooltipfilter.GuiFactory";

    public static final String updateJSON = "https://raw.githubusercontent.com/librazy/tooltipfilter/master/resources/update.json";

    public static final String dependencies = "required-after:forge@[13.20.0.2201,);";

    public static final String buildTime = "@BUILD_TIME@";

    public static final String[] modeNames = Arrays.stream(FilterMode.values()).map(Enum::name).toArray(String[]::new);


    public static List<FilterEntry> filters = new LinkedList<>();
    protected static Configuration configuration;
    private static File configFile;

    public TooltipFilter() {

    }

    public static void log(String msg) {
        LogManager.getLogger(MODID).log(Level.INFO, msg);
    }

    private static void remove(List<String> tooltip, @Language("RegExp") String regExp, boolean exact, boolean allButFirst) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator();
        while (rit.hasNext()) {
            String s = rit.next();
            if (s.matches(regExp)) {
                check(exact, allButFirst, set, rit, s);
            }
        }
    }

    private static void removeRev(List<String> tooltip, @Language("RegExp") String regExp, boolean exact, boolean allButLast) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator(tooltip.size());
        while (rit.hasPrevious()) {
            String s = rit.previous();
            if (s.matches(regExp)) {
                check(exact, allButLast, set, rit, s);
            }
        }
    }

    private static void replace(List<String> tooltip, @Language("RegExp") String regExp, String replace, boolean exact, boolean allButFirst) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator();
        while (rit.hasNext()) {
            String s = rit.next();
            if (s.matches(regExp)) {
                checkReplace(regExp, replace, exact, allButFirst, set, rit, s);
            }
        }
    }

    private static void replaceRev(List<String> tooltip, @Language("RegExp") String regExp, String replace, boolean exact, boolean allButLast) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator(tooltip.size());
        while (rit.hasPrevious()) {
            String s = rit.previous();
            if (s.matches(regExp)) {
                checkReplace(regExp, replace, exact, allButLast, set, rit, s);
            }
        }
    }

    private static void combineMatch(List<String> tooltip, @Language("RegExp") String regExp) {
        ListIterator<String> it = tooltip.listIterator();
        boolean lastMatch = false;
        while (it.hasNext()) {
            String s = it.next();
            boolean match = s.matches(regExp);
            if (match && lastMatch) {
                it.remove();
            }
            lastMatch = match;
        }
    }

    private static void combineExact(List<String> tooltip, @Language("RegExp") String regExp) {
        ListIterator<String> it = tooltip.listIterator();
        String lastMatch = null;
        while (it.hasNext()) {
            String s = it.next();
            boolean match = s.matches(regExp);
            if (match && s.equals(lastMatch)) {
                it.remove();
            }
            lastMatch = s;
        }
    }

    private static void check(boolean exact, boolean allBut, Set<String> set, ListIterator<String> lit, String s) {
        if (exact) {
            if (allBut != set.add(s)) {
                lit.remove();
            }
        } else {
            if (allBut != set.isEmpty()) {
                lit.remove();
            }
            set.add("");
        }
    }

    private static void checkReplace(String regExp, String replace, boolean exact, boolean allBut, Set<String> set, ListIterator<String> lit, String s) {
        if (exact) {
            if (allBut != set.add(s)) {
                lit.set(s.replaceAll(regExp, replace));
            }
        } else {
            if (allBut != set.isEmpty()) {
                lit.set(s.replaceAll(regExp, replace));
            }
            set.add("");
        }
    }

    public static void load() {
        configuration = new Configuration(configFile);
        configuration.load();
        List<ConfigCategory> entryConfigs = new LinkedList<>();
        Set<String> entryConfigNames = configuration.getCategoryNames().stream().filter(s -> s.startsWith("tooltipfilter.filters.")).collect(Collectors.toSet());
        for (String name : entryConfigNames) {
            entryConfigs.add(configuration.getCategory(name));
        }
        entryConfigs.forEach(
                configCategory -> {
                    if (configCategory.isEmpty()) return;
                    @Language("RegExp") String regExp = configCategory.get("regExp").getString();
                    boolean isRegBase64 = configCategory.get("isRegBase64").getBoolean();
                    boolean isFullText = configCategory.get("isFullText").getBoolean();
                    String replace = configCategory.get("replace").getString();
                    FilterMode mode = FilterMode.valueOf(configCategory.get("mode").getString().toUpperCase());
                    String name = configCategory.get("name").getString();

                    filters.add(new FilterEntry(regExp, replace, isRegBase64, isFullText, mode, name));
                }
        );
    }

    public static void save() {
        filters.forEach(filterEntry -> {
            ConfigCategory configCategory = configuration.getCategory("tooltipfilter.filters." + filterEntry.name);
            configCategory.put("regExp", new Property("regExp", filterEntry.regExp, Property.Type.STRING));
            configCategory.put("isRegBase64", new Property("isRegBase64", filterEntry.isRegBase64.toString(), Property.Type.BOOLEAN));
            configCategory.put("isFullText", new Property("isFullText", filterEntry.isFullText.toString(), Property.Type.BOOLEAN));
            configCategory.put("replace", new Property("replace", filterEntry.replace, Property.Type.STRING));
            configCategory.put("mode", new Property("mode", filterEntry.mode.name(), Property.Type.STRING, modeNames));
            configCategory.put("name", new Property("name", filterEntry.name, Property.Type.STRING));
            configCategory.setShowInGui(true);
        });
        if (configuration.hasChanged()) configuration.save();
    }

    /**
     * Pre init.
     *
     * @param event the event
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFile = event.getSuggestedConfigurationFile();
    }

    /**
     * Init.
     *
     * @param event the event
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        log(String.format("%s version: %s (%s)", MODNAME, VERSION, buildTime));

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChangeListener());
        load();
    }

    @SubscribeEvent
    public void ItemTooltipEvent(ItemTooltipEvent event) {
        List<String> tooltip = event.getToolTip();
        for (FilterEntry fi : filters) {
            @Language("RegExp") String reg = fi.isRegBase64 ? new String(Base64.getDecoder().decode(fi.regExp)) : fi.regExp;
            @Language("RegExp") String replace = fi.isRegBase64 ? new String(Base64.getDecoder().decode(fi.replace)) : fi.replace;
            if (!fi.isFullText) {
                switch (fi.mode) {
                    case REMOVE:
                        tooltip.removeIf(s -> s.matches(reg));
                        break;
                    case REMOVE_ALL_BUT_FIRST_MATCH:
                        remove(tooltip, reg, false, true);
                        break;
                    case REMOVE_ALL_BUT_LAST_MATCH:
                        removeRev(tooltip, reg, false, true);
                        break;
                    case REMOVE_FIRST_MATCH:
                        remove(tooltip, reg, false, false);
                        break;
                    case REMOVE_LAST_MATCH:
                        removeRev(tooltip, reg, false, false);
                        break;
                    case REMOVE_ALL_BUT_FIRST_EXACT:
                        remove(tooltip, reg, true, true);
                        break;
                    case REMOVE_ALL_BUT_LAST_EXACT:
                        removeRev(tooltip, reg, true, true);
                        break;
                    case REMOVE_FIRST_EXACT:
                        remove(tooltip, reg, false, false);
                        break;
                    case REMOVE_LAST_EXACT:
                        removeRev(tooltip, reg, true, false);
                        break;
                    case REPLACE:
                        tooltip = tooltip.stream().map(s -> s.replaceAll(reg, fi.replace)).collect(Collectors.toList());
                        break;
                    case REPLACE_ALL_BUT_FIRST_MATCH:
                        replace(tooltip, reg, replace, false, true);
                        break;
                    case REPLACE_ALL_BUT_LAST_MATCH:
                        replaceRev(tooltip, reg, replace, false, true);
                        break;
                    case REPLACE_FIRST_MATCH:
                        replace(tooltip, reg, replace, false, false);
                        break;
                    case REPLACE_LAST_MATCH:
                        replaceRev(tooltip, reg, replace, false, false);
                        break;
                    case REPLACE_ALL_BUT_FIRST_EXACT:
                        replace(tooltip, reg, replace, true, true);
                        break;
                    case REPLACE_ALL_BUT_LAST_EXACT:
                        replaceRev(tooltip, reg, replace, true, true);
                        break;
                    case REPLACE_FIRST_EXACT:
                        replace(tooltip, reg, replace, true, false);
                        break;
                    case REPLACE_LAST_EXACT:
                        replaceRev(tooltip, reg, replace, true, false);
                        break;
                    case COMBINE_NEAR_MATCH:
                        combineMatch(tooltip, reg);
                        break;
                    case COMBINE_NEAR_EXACT:
                        combineExact(tooltip, reg);
                        break;
                }
            } else {
                String tooltipStr = String.join("\n", tooltip);
                switch (fi.mode) {
                    case REPLACE:
                        tooltip.clear();
                        tooltip.addAll(Arrays.stream(tooltipStr.replaceAll(reg, replace).split("\n")).collect(Collectors.toList()));
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

    }

    public static class ChangeListener {

        @SubscribeEvent
        public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
            if (eventArgs.getModID().equals(TooltipFilter.MODID))
                load();
        }

    }
}

