package org.renjin.utils.table;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class StringBlockTest {

    @Test
    public void test() {
        
        StringBlock.Builder builder = new StringBlock.Builder();
        builder.add("abc");
        builder.add(null);
        builder.add("xyz");
        builder.add("42");

        StringBlock block = builder.build();
        
        assertThat(block.getStringAt(0), equalTo("abc"));
        assertThat(block.getStringAt(1), nullValue());
        assertThat(block.getStringAt(2), equalTo("xyz"));
        assertThat(block.getStringAt(3), equalTo("42"));
    }
}