package org.renjin.utils.table;

import org.renjin.parser.NumericLiterals;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringVector;

import java.nio.CharBuffer;
import java.util.Arrays;


public class StringBlock {
    
    public static final int NA_FLAG = Integer.MIN_VALUE;
    public static final int OFFSET_MASK = ~NA_FLAG;
    
    private final char[] chars;
    private final CharBuffer charSequence;
    private final int[] offsets;
    private final int count;

    private StringBlock(char[] chars, int[] offsets, int count) {
        this.chars = chars;
        this.charSequence = CharBuffer.wrap(chars);
        this.offsets = offsets;
        this.count = count;
    }
    
    public String getStringAt(int index) {
        int offset = offsets[index];
        if(offset < 0) {
            return StringVector.NA;
        } else {
            int endIndex = offsets[index+1] & OFFSET_MASK;
            int len = endIndex - offset;
            return new String(chars, offset, len);
        }
    }
    
    public boolean isNA(int index) {
        int offset = offsets[index];
        return (offset & NA_FLAG) != 0;
    }

    public int getCount() {
        return count;
    }

    public double parseDoubleAt(int index) {
        int offset = offsets[index];
        if( (offset & NA_FLAG) != 0) {
            return DoubleVector.NA;
        } else {
            int endIndex = offsets[index+1] & OFFSET_MASK;
            int len = endIndex - offset;
            if(len == 0) {
                return DoubleVector.NA;
            } else {
                return NumericLiterals.parseDouble(charSequence, offset, endIndex, '.', true);
            }
        }
    }


    public int parseIntAt(int index) {
        int offset = offsets[index];
        if(offset < 0) {
            return IntVector.NA;
        } else {
            int endIndex = offsets[index+1] & OFFSET_MASK;
            int len = endIndex - offset;
            if(len == 0) {
                return IntVector.NA;
            } else {
                return (int)NumericLiterals.parseDouble(charSequence, offset, endIndex, '.', true);
            }
        }
    }

    public static class Builder {
        private char[] chars = new char[100];
        private int[] offsets = new int[10];
        private int charCount = 0;
        private int length = 0;

        public void add(String str) {
            
            if(str == null) {
                addOffset(charCount | NA_FLAG);
            } else {
                addOffset(charCount);
                addChars(str);
            }
        }
        
        public void addNA() {
            addOffset(charCount | NA_FLAG);
        }

        public StringBlock build() {
            offsets[length] = charCount;
            return new StringBlock(chars, offsets, length);
        }
        
        private void addChars(String str) {
            int len = str.length();
            int minimumCapacity = charCount + len;
            if ((minimumCapacity - chars.length) > 0) {
                expandCharCapacity(minimumCapacity);
            }

            str.getChars(0, len, chars, charCount);
            charCount += len;
        }
        
        void expandCharCapacity(int minimumCapacity) {
            int newCapacity = chars.length * 2 + 2;
            if (newCapacity - minimumCapacity < 0)
                newCapacity = minimumCapacity;
            if (newCapacity < 0) {
                if (minimumCapacity < 0) // overflow
                    throw new OutOfMemoryError();
                newCapacity = Integer.MAX_VALUE;
            }
            chars = Arrays.copyOf(chars, newCapacity);
        }


        private void addOffset(int offset) {
            int minimumCapacity = length + 2;
            if( (minimumCapacity - offsets.length) > 0) {
                expandOffsetCapacity(minimumCapacity);
            }
            offsets[length] = offset;
            length++;
        }

        public void expandOffsetCapacity(int minCapacity) {
            int oldCapacity = offsets.length;
            int oldData[] = offsets;
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            offsets = Arrays.copyOf(oldData, newCapacity);
        }
    }
}
