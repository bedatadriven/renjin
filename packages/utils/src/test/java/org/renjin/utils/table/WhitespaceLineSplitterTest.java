package org.renjin.utils.table;

import org.junit.Test;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;

public class WhitespaceLineSplitterTest {

    @Test
    public void spaces() {
        WhitespaceLineSplitter splitter = new WhitespaceLineSplitter();
        assertThat(splitter.split("a b c"), equalTo(asList("a", "b", "c")));
    }

    @Test
    public void tabs() {
        WhitespaceLineSplitter splitter = new WhitespaceLineSplitter();
        assertThat(splitter.split("a\tb\tc"), equalTo(asList("a", "b", "c")));
    }
    
    @Test
    public void extraSpace() {
        WhitespaceLineSplitter splitter = new WhitespaceLineSplitter();
        assertThat(splitter.split("a   b    c "), equalTo(asList("a", "b", "c")));
    }

    @Test
    public void extraPrecedingSpace() {
        WhitespaceLineSplitter splitter = new WhitespaceLineSplitter();
        assertThat(splitter.split("   a   b    c "), equalTo(asList("a", "b", "c")));
    }

}