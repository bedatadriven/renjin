package org.renjin.utils.table;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Splits lines by a single delimiter character
 */
public class DelimitedSplitter implements LineSplitter {

    private final char quote;
    private final char delimiter;

    public DelimitedSplitter(char quoteChar, char delimiter) {
        this.quote = quoteChar;
        this.delimiter = delimiter;
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
            } else if(!quoted && c == delimiter) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
            i++;
        }
        fields.add(sb.toString());
        return fields;
    }
}
