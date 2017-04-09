package org.librazy.tooltipfilter;

import org.intellij.lang.annotations.Language;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

final class Filters {
    static void remove(List<String> tooltip, @Language("RegExp") String regExp, boolean exact, boolean allButFirst) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator();
        while (rit.hasNext()) {
            String s = rit.next();
            if (s.matches(regExp)) {
                check(exact, allButFirst, set, rit, s);
            }
        }
    }

    static void removeRev(List<String> tooltip, @Language("RegExp") String regExp, boolean exact, boolean allButLast) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator(tooltip.size());
        while (rit.hasPrevious()) {
            String s = rit.previous();
            if (s.matches(regExp)) {
                check(exact, allButLast, set, rit, s);
            }
        }
    }

    static void replace(List<String> tooltip, @Language("RegExp") String regExp, String replace, boolean exact, boolean allButFirst) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator();
        while (rit.hasNext()) {
            String s = rit.next();
            if (s.matches(regExp)) {
                checkReplace(regExp, replace, exact, allButFirst, set, rit, s);
            }
        }
    }

    static void replaceRev(List<String> tooltip, @Language("RegExp") String regExp, String replace, boolean exact, boolean allButLast) {
        Set<String> set = new HashSet<>();
        ListIterator<String> rit = tooltip.listIterator(tooltip.size());
        while (rit.hasPrevious()) {
            String s = rit.previous();
            if (s.matches(regExp)) {
                checkReplace(regExp, replace, exact, allButLast, set, rit, s);
            }
        }
    }

    static void combineMatch(List<String> tooltip, @Language("RegExp") String regExp) {
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

    static void combineExact(List<String> tooltip, @Language("RegExp") String regExp) {
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

    private Filters() throws Exception {
        throw new Exception();
    }
}
