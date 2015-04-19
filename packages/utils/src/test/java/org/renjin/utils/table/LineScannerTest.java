package org.renjin.utils.table;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class LineScannerTest {

    @Test
    public void peek() throws IOException {
        
        LineScanner scanner = new LineScanner(new TableOptions(), new StringReader("a\nb\nc\n"));
        assertThat(scanner.peek(2), hasItems("a", "b"));
        assertThat(scanner.readLine(), equalTo("a"));
        assertThat(scanner.readLine(), equalTo("b"));
        assertThat(scanner.readLine(), equalTo("c"));
        assertThat(scanner.readLine(), nullValue());
    }
}