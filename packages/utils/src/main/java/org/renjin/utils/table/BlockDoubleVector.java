package org.renjin.utils.table;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;


public class BlockDoubleVector extends DoubleVector {

   
    private StringBlock block;

    public BlockDoubleVector(StringBlock block, AttributeMap attributes) {
        super(attributes);
        this.block = block;
    }

    @Override
    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
        return new BlockDoubleVector(block, attributes);
    }

    @Override
    public double getElementAsDouble(int index) {
        return block.parseDoubleAt(index);
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
    public String toString() {
        return toString(this);
    }
}
