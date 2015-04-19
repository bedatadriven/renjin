package org.renjin.utils.table;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

public class BlockIntVector extends IntVector {

    private StringBlock block;

    public BlockIntVector(StringBlock block, AttributeMap attributes) {
        super(attributes);
        this.block = block;
    }

    @Override
    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
        return new BlockDoubleVector(block, attributes);
    }

    @Override
    public boolean isConstantAccessTime() {
        return true;
    }

    @Override
    public int length() {
        return block.getCount();
    }

    @Override
    public int getElementAsInt(int i) {
        return block.parseIntAt(i);
    }

}
