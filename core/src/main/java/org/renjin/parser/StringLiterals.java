package org.renjin.parser;

import org.renjin.sexp.StringVector;

/**
 * Formats
 */
public class StringLiterals {
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
            } else if(codePoint < 32 || codePoint > 126) {
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
