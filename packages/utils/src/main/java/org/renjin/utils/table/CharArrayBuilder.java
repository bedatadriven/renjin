package org.renjin.utils.table;

import java.util.Arrays;

/**
 * Builds a buffer array
 */
public class CharArrayBuilder {
    private char[] array = new char[100];
    private int count;
    

    public void add(String str) {
        assert str != null;
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, array, count);
        count += len;
    }

    /**
     * This method has the same contract as ensureCapacity, but is
     * never synchronized.
     */
    private void ensureCapacityInternal(int minimumCapacity) {
        // overflow-conscious code
        if (minimumCapacity - array.length > 0) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * This implements the expansion semantics of ensureCapacity with no
     * size check or synchronization.
     */
    void expandCapacity(int minimumCapacity) {
        int newCapacity = array.length * 2 + 2;
        if (newCapacity - minimumCapacity < 0)
            newCapacity = minimumCapacity;
        if (newCapacity < 0) {
            if (minimumCapacity < 0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        array = Arrays.copyOf(array, newCapacity);
    }
    
    public char[] build() {
        return array;
    }
}
