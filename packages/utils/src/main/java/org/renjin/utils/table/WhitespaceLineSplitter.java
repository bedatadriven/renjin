package org.renjin.utils.table;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Splits lines into fields separated by one more whitespace characters
 * and quoted by {@code quoteChar}
 */
public class WhitespaceLineSplitter implements LineSplitter {

    private final char quote;


    public WhitespaceLineSplitter() {
        this.quote = 0;
    }

    public WhitespaceLineSplitter(char quoteChar) {
        this.quote = quoteChar;
    }

    public List<String> split(String line) {
        StringBuilder sb = new StringBuilder();
        List<String> fields = Lists.newArrayList();
        boolean quoted = false;
        int i = 0;
        while(i < line.length()) {
            char c = line.charAt(i);
            if(c == quote) {
                quoted = !quoted;
            } else if(!quoted && Character.isWhitespace(c)) {
                if(i != 0) {
                    fields.add(sb.toString());
                    sb.setLength(0);
                }
                while(i+1 < line.length() && 
                        Character.isWhitespace(line.charAt(i+1))) {
                    i++;
                }
            } else {
                sb.append(c);
            }
            i++;
        }
        if(sb.length() > 0) {
            fields.add(sb.toString());
        }
        return fields;
    }
}
