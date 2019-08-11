/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.parser;

import org.renjin.sexp.StringVector;

/**
 * Formats
 */
public class StringLiterals {


    /** Whether to escape strings above codepoint 126 (non ascii) or not, defaults to false.
     * Override with -D option or by setting a system variable for renjin.core.parser.escape.non.ascii
     */
    public static boolean ESCAPE_NON_ASCII = getPropertyOrEnv("renjin.core.parser.escape.non.ascii", "false")
       .equalsIgnoreCase("true");

    /**
     * First check property (-D option), if that not exist then look for variable in the environment,
     * if that does not exist either, then fallback to default.
     * @param key the property key to look for, case sensitive
     * @param defaultVal the fallback value if no key is defined
     * @return the value corresponding to the property key or the defaultVal if none exist
     */
    private static String getPropertyOrEnv(String key, String defaultVal) {
        String prop = System.getProperty(key);
        if (prop == null) {
            prop = System.getenv(key);
        }
        if (prop == null) {
            return defaultVal;
        }
        return prop;
    }

    /**
     * Formats a {@code String} as a literal
     *
     * @param value the {@code String} to format
     * @param naString the text to use for "NA" {@code String}s
     *
     * @return the value formatted as a literal
     */
    public static String format(String value, String naString) {
      if(StringVector.isNA(value)) {
        return naString;
      } else {
        StringBuilder sb = new StringBuilder("\"");
        appendEscaped(sb, value);
        sb.append('"');
        return sb.toString();
      }
    }


    public static void appendEscaped(StringBuilder buf, String s) {
        for(int i=0;i!=s.length(); ++i) {

            int codePoint = s.codePointAt(i);
            if(codePoint == '\n') {
                buf.append("\\n");
            } else if(codePoint == '\r') {
                buf.append("\\r");
            } else if(codePoint == '\t') {
                buf.append("\\t");
            } else if(codePoint == 7) {
                buf.append("\\a");
            } else if(codePoint == '\b') {
                buf.append("\\b");
            } else if(codePoint == '\f') {
                buf.append("\\f");
            } else if(codePoint == 11) {
                buf.append("\\v");
            } else if(codePoint == '\"') {
                buf.append("\\\"");
            } else if(codePoint == '\\') {
                buf.append("\\\\");
            } else if(codePoint < 32) {
                appendUnicodeEscape(buf, codePoint);
            } else if (ESCAPE_NON_ASCII && codePoint  > 126) {
                appendUnicodeEscape(buf, codePoint);
            } else {
                buf.appendCodePoint(codePoint);
            }
        }
    }

    private static void appendUnicodeEscape(StringBuilder buf, int codePoint) {
        buf.append("\\u");
        if(codePoint < 0xF) {
            buf.append("000");
        } else if(codePoint < 0xFF) {
            buf.append("00");
        } else if(codePoint < 0xFFF) {
            buf.append("0");
        }
        buf.append(Integer.toHexString(codePoint));
    }

}
