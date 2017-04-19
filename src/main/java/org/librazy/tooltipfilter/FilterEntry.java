package org.librazy.tooltipfilter;

import org.intellij.lang.annotations.RegExp;

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
}
