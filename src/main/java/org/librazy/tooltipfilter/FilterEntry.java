package org.librazy.tooltipfilter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.intellij.lang.annotations.RegExp;

import java.util.Base64;

import static org.librazy.tooltipfilter.TooltipFilter.*;

public class FilterEntry implements java.io.Serializable {
    @RegExp
    public String regExp;
    public Boolean isRegBase64;
    public Boolean isFullText;
    public String replace;
    public FilterMode mode;
    public String name;
    public FilterEntry(@RegExp String regex, String replace, Boolean isRegBase64, Boolean isFullText, FilterMode mode, String name){
        this.regExp = regex;
        this.replace = replace;
        this.isFullText = isFullText;
        this.isRegBase64 = isRegBase64;
        this.mode = mode;
        this.name = name;
    }

    public FilterEntry(){
        this.regExp = "";
        this.replace = "";
        this.isFullText = false;
        this.isRegBase64 = false;
        this.mode = FilterMode.REMOVE;
        this.name = "";
    }

    public void toBase64(){
        if(!this.isRegBase64){
            isRegBase64 = true;
            regExp = Base64.getEncoder().encodeToString(regExp.getBytes());
            replace = Base64.getEncoder().encodeToString(replace.getBytes());
        }
    }

    public void dump(){
        LogManager.getLogger(MODID).log(Level.INFO,  "====" + name + "====");
        LogManager.getLogger(MODID).log(Level.INFO, regExp);
        if(isRegBase64){
            LogManager.getLogger(MODID).log(Level.INFO, "Decoded:" + new String(Base64.getDecoder().decode(regExp)));
        }
        LogManager.getLogger(MODID).log(Level.INFO, replace);
        if(isRegBase64){
            LogManager.getLogger(MODID).log(Level.INFO, "Decoded:" + new String(Base64.getDecoder().decode(replace)));
        }
        LogManager.getLogger(MODID).log(Level.INFO, isFullText);
        LogManager.getLogger(MODID).log(Level.INFO, isRegBase64);
        LogManager.getLogger(MODID).log(Level.INFO, mode);
        LogManager.getLogger(MODID).log(Level.INFO, "==  ==  ==  ==  ==");
    }
}
