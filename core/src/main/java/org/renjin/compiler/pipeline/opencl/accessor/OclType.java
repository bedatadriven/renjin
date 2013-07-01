package org.renjin.compiler.pipeline.opencl.accessor;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.renjin.sexp.*;

public enum OclType {
  INT {
    @Override
    public int sizeOf() {
      return Sizeof.cl_int;
    }

    @Override
    public Pointer scalarPointerTo(Vector operand) {
      return Pointer.to(new int[] { operand.getElementAsInt(0) });
    }

    @Override
    public Pointer pointerTo(Vector operand) {
      if(operand instanceof IntArrayVector) {
        return Pointer.to(((IntArrayVector) operand).toIntArrayUnsafe());
      } else {
        return Pointer.to(((AtomicVector) operand).toIntArray());
      }
    }
  },
  DOUBLE {
    @Override
    public int sizeOf() {
      return Sizeof.cl_double;
    }

    @Override
    public Pointer scalarPointerTo(Vector operand) {
      return Pointer.to(new double[] { operand.getElementAsDouble(0)});
    }

    @Override
    public Pointer pointerTo(Vector operand) {
      if(operand instanceof DoubleArrayVector) {
        return Pointer.to(((DoubleArrayVector) operand).toDoubleArrayUnsafe());
      } else {
        return Pointer.to(((AtomicVector)operand).toDoubleArray());
      }
    }
  };

  public String getTypeName() {
    return name().toLowerCase();
  }

  public abstract int sizeOf();

  public abstract Pointer scalarPointerTo(Vector operand);

  public abstract Pointer pointerTo(Vector operand);

  public static OclType ofVector(Vector vector) {
    if(vector instanceof DoubleVector) {
      return DOUBLE;
    } else if(vector instanceof IntVector) {
      return INT;
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
