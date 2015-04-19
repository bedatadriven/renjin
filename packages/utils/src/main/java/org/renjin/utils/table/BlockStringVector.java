package org.renjin.utils.table;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

/**
 * A {@code StringVector} backed by a single {@code char[]} paired with an array of offsets
 */
public class BlockStringVector extends StringVector {

    
    private StringBlock block;

    public BlockStringVector(StringBlock block, AttributeMap attributeMap) {
        super(attributeMap);
        this.block = block;
    }

    @Override
    public int length() {
        return block.getCount();
    }

    @Override
    protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
        return new BlockStringVector(block, attributes);
    }

    @Override
    public String getElementAsString(int index) {
        return block.getStringAt(index);
    }

    @Override
    public boolean isConstantAccessTime() {
        return true;
    }
}
