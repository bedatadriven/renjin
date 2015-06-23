package org.renjin.primitives.io.serialization;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

/**
 * Character vector backed by a byte array and a paired list of offsets into the array. This minimizes
 * the amount of time required to read a large character vector from an RDS file.
 */
public class StringByteArrayVector extends StringVector {

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;


    /**
     * Byte buffer containing the encoded contents of all elements of this vector.
     */
    private final byte[] buffer;
    
    private final Charset charset;
    private final CharsetDecoder decoder;

    /**
     * Array of offsets of the elements into the byte buffer. Elements that are NA have an offset of -1.
     */
    private int[] offsets;
    
    private int length;


    private StringByteArrayVector(int[] offsets, byte[] buffer, Charset charset, AttributeMap attributeMap) {
        super(attributeMap);
        this.offsets = offsets;
        this.buffer = buffer;
        this.charset = charset;
        this.decoder = charset.newDecoder();
        this.length = offsets.length - 1;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
        return new StringByteArrayVector(offsets, buffer, charset, attributes);
    }

    @Override
    public String getElementAsString(int index) {
        int offset = offsets[index];
        if(offset < 0) {
            return null;
        } else {
            int endPos = offsets[index + 1];
            
            // negative offsets indicate NAs
            if(endPos < 0) {
                endPos = -endPos;
            }
            int length = endPos - offset;
            return new String(buffer, offset, length, charset);
        }
    }

    @Override
    public boolean isElementNA(int index) {
        return offsets[index] == -1;
    }

    @Override
    public boolean isConstantAccessTime() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StringByteArrayVector{");
        for(int i=0;i<Math.min(10, length());++i) {
            if(i!=0) {
                sb.append(", ");
            }
            if(isElementNA(i)) {
                sb.append("NA");
            } else {
                sb.append("'").append(getElementAsString(i)).append("'");
            }
        }
        if(length() > 10) {
            sb.append(",... ").append(length()).append(" elements in total");
        }
        sb.append("}");
        return sb.toString();
    }

    public static class Builder {

        private int offsets[];
        private byte[] buffer;

        private int currentOffset = 0;
        private int currentIndex = 0;
        
        private Charset charset = Charset.defaultCharset();

        public Builder(int numElements) {
            this.offsets = new int[numElements+1];
            this.buffer = new byte[1024];
        }
        
        public void addNA() {
            offsets[currentIndex++] = -1;
        }

        public void readFrom(RDataReader.StreamReader in, int length) throws IOException {
            if(length < 0) {
                offsets[currentIndex] = -currentOffset;
            } else {
                int minimumCapacity = currentOffset + length;
                if (minimumCapacity > buffer.length) {
                    grow(minimumCapacity);
                }
                offsets[currentIndex] = currentOffset;
                in.readFully(buffer, currentOffset, length);
                currentOffset += length;
            }
            currentIndex++;
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }

        /**
         * Increases the capacity to ensure that it can hold at least the
         * number of elements specified by the minimum capacity argument.
         *
         * @param minCapacity the desired minimum capacity
         */
        private void grow(int minCapacity) {
            // overflow-conscious code
            int oldCapacity = buffer.length;
            
            double averageElementSize = ((double)currentOffset) / ((double)currentIndex);
            int expectedCapacity = (int)( averageElementSize * (double)offsets.length );
            
            int newCapacity;
            if(expectedCapacity > minCapacity) {
                newCapacity = expectedCapacity; 
            } else {
                newCapacity = oldCapacity << 1;
            }
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = hugeCapacity(minCapacity);
            }
            buffer = Arrays.copyOf(buffer, newCapacity);

//            System.out.println("Grew from " + oldCapacity + " to " + buffer.length);

        }

        private static int hugeCapacity(int minCapacity) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError();
            return (minCapacity > MAX_ARRAY_SIZE) ?
                    Integer.MAX_VALUE :
                    MAX_ARRAY_SIZE;
        }

        public SEXP build(AttributeMap attributeMap) {
            if(currentIndex != (offsets.length-1)) {
                throw new IllegalStateException("Expected " + (offsets.length-1) + " elements, but only " +
                        currentIndex + " added.");
            }
            
            offsets[currentIndex] = currentOffset;
            
            int excessCapacity = buffer.length - currentOffset;
            if(excessCapacity > 10 * 1024) {
//                System.out.println("Shrinking array from " + buffer.length + " to " + currentOffset + 
//                        " (Excess capacity = " + excessCapacity + ")");
                buffer = Arrays.copyOf(buffer, currentOffset);
            }
            
            return new StringByteArrayVector(offsets, buffer, charset, attributeMap);
        }

    }
}
