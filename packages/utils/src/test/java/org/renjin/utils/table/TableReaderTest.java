package org.renjin.utils.table;

import com.google.common.base.Charsets;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class TableReaderTest {

    @Test
    public void basic() throws IOException {
        TableOptions options = new TableOptions();
        options.header = true;
        options.autoDetectHeader = false;
        
        TableReader reader = new TableReader(options);
        ListVector dataFrame = reader.readString("a b c  d\n1 2  \t3  4\n4   5   6  7");
        System.out.println(dataFrame);

        assertThat("column names", dataFrame.getAttribute(Symbols.NAMES),
                Matchers.<SEXP>equalTo(new StringArrayVector("a", "b", "c", "d")));
        assertThat("column count", dataFrame.length(), IsEqual.equalTo(4));
        assertThat(dataFrame.get(0).length(), IsEqual.equalTo(2));
        assertThat(dataFrame.get(1).length(), IsEqual.equalTo(2));
        assertThat(dataFrame.get(2).length(), IsEqual.equalTo(2));
        assertThat(dataFrame.get(3).length(), IsEqual.equalTo(2));
    }
    
    @Test
    public void lineEndings() throws IOException {
        
        String[] variations = new String[] {
                "a b c\r\n1 2 3\r\n1 2 3\n",
                "a b c\n\r1 2 3\n\r1 2 3\n\r",
                "a b c\n1 2 3\n1 2 3\n",
                "a b c\n1 2 3\n1 2 3",
        };
        
        for(String variation : variations) {
            TableReader reader = new TableReader();
            ListVector dataFrame = reader.readString(variation);
            String prettyPrint = variation.replace("\n", "\\n").replace("\r", "\\r");

            assertThat(prettyPrint + " rows", dataFrame.get(0).length(), IsEqual.equalTo(3));
        }
    }
    
    @Test
    public void utf8newlines() throws IOException {


        StringBuilder sb = new StringBuilder();
        
        for(int cp=0;cp<Character.MAX_CODE_POINT;++cp) {
            sb.setLength(0);
            sb.appendCodePoint(cp);
            byte[] bytes = sb.toString().getBytes(Charsets.UTF_8);
            if(containsNewlineBytes(bytes)) {
                System.out.printf("Codepoint %6d: %s\n", cp, toString(bytes));
            }
        }
    }


    public boolean containsNewlineBytes(byte[] buffer) {
        for(int i=0;i<buffer.length;++i) {
            if(buffer[i] == '\n' || buffer[i] == '\r') {
                return true;
            }
        }
        return false;
    }

    public String toString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<buffer.length;++i) {
            if(buffer[i] == '\r' || buffer[i] == '\n') {
                sb.append(String.format(" [0x%02X]", buffer[i]));
            } else {
                sb.append(String.format("  0x%02X ", buffer[i]));
            }
            
        }
        return sb.toString();
    }
    
}