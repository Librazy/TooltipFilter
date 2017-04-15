package org.librazy.tooltipfilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Set;

public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        // NO-OP
    }

    @Override
    public boolean hasConfigGui() {
        return false;
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parent){
        return null;
    }

    public static class ConfigGui extends GuiConfig {

        public ConfigGui(GuiScreen parentScreen) {
            super(parentScreen, new ConfigElement(TooltipFilter.configuration.getCategory("tooltipfilter.filters")).getChildElements(), TooltipFilter.MODID, false, false, GuiConfig.getAbridgedConfigPath(TooltipFilter.configuration.toString()));
        }

    }

}