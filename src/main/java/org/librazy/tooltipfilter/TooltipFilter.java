package org.librazy.tooltipfilter;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.librazy.tooltipfilter.FilterMode.*;
import static org.librazy.tooltipfilter.Filters.*;

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

    public static final String updateJSON = "https://raw.githubusercontent.com/librazy/tooltipfilter/update/update.json";

    public static final String dependencies = "required-after:forge@[13.20.0.2201,);";

    public static final String buildTime = "@BUILD_TIME@";

    public static final String[] modeNames = Arrays.stream(FilterMode.values()).map(Enum::name).toArray(String[]::new);


    public static List<FilterEntry> filters = new ArrayList<>();

    protected static Configuration configuration;

    private static File configFile;

    private static File filterFile;

    private static Representer representer = new Representer();

    private static Constructor constructor = new Constructor();

    private static DumperOptions dumperOptions = new DumperOptions();

    static {
        representer.addClassTag(FilterEntry.class, new Tag("!filter"));
        dumperOptions.setIndent(4);
        dumperOptions.setPrettyFlow(true);
        constructor.addTypeDescription(new TypeDescription(FilterEntry.class, new Tag("!filter")));
    }

    public static void log(String msg) {
        LogManager.getLogger(MODID).log(Level.INFO, "[" + MODNAME + "]" + msg);
    }

    public static void load() {
        configuration = new Configuration(configFile);
        configuration.load();
        try {
            Yaml conf = new Yaml(constructor);

            Object object = conf.load(new InputStreamReader(new FileInputStream(filterFile), "UTF-8"));
            log("Read:" + object.toString());
            filters = (ArrayList<FilterEntry>) object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        save();
    }

    public static void save() {
        log("Starting saving");
        try {
            log(filters.size() + "");
            log(filters.toString());
            Yaml conf = new Yaml(representer, dumperOptions);
            conf.dump(filters, new OutputStreamWriter(new FileOutputStream(filterFile), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pre init.
     *
     * @param event the event
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFile = event.getSuggestedConfigurationFile();
        filterFile = new File(event.getModConfigurationDirectory() + "/" + TooltipFilter.MODNAME + "/filter.txt");
        try {
            Files.createDirectories(filterFile.getParentFile().toPath());
            Boolean suc = filterFile.createNewFile();
            if (!suc) {
                log("Found filter config");
            } else {
                log("Creating filter config");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

