package org.renjin.parser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringLiteralsTest {

    @Test
    public void unicodeEscapes() {
        assertThat(StringLiterals.format("\u0001", "NA"), equalTo("\"\\u0001\""));
        assertThat(StringLiterals.format("\u00a1", "NA"), equalTo("\"\\u00a1\""));
        assertThat(StringLiterals.format("\u01a1", "NA"), equalTo("\"\\u01a1\""));
        assertThat(StringLiterals.format("\u1fa1", "NA"), equalTo("\"\\u1fa1\""));
    }
}