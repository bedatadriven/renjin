package org.renjin.utils.table;

import com.google.common.base.Strings;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColumnBufferTest {

    @Test
    public void test() {
        
        //System.out.println(Integer.toBinaryString(Integer.MIN_VALUE));
        
        int naFlag = Integer.MIN_VALUE;
        int offsetMask = ~naFlag;

        int offset = 1024;
        dump(offset);
        dump(offset & offsetMask);

        offset |= naFlag;
        dump(offset);

        offset = offset & offsetMask;
        dump(offset);

        
    }

    private void dump(int offset) {
        System.out.println(Strings.padStart(Integer.toBinaryString(offset), 32, '0'));
    }
}