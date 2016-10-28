package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;

import java.util.List;

/**
 * Selects a single element from within an atomic vector
 */
public class GetElement implements Specialization {


  private final ValueBounds resultBounds;
  private final Type type;

  public static boolean accept(ValueBounds source, ValueBounds subscript) {

    if(!TypeSet.isDefinitelyAtomic(source.getTypeSet())) {
      return false;
    }

    if (subscript.getLength() != 1) {
      return false;
    }
    if (subscript.maybeNA()) {
      return false;
    }

    return true;
  }

  public GetElement(ValueBounds source, ValueBounds subscript) {
    assert accept(source, subscript);

    ValueBounds.Builder resultBounds = new ValueBounds.Builder();
    resultBounds.setTypeSet(TypeSet.elementOf(source.getTypeSet()));
    resultBounds.setNA(source.getNA());
    resultBounds.setAttributeSetOpen(false);
    resultBounds.setLength(1);

    SEXP resultNames = namesBounds(source);
    if(resultNames != Null.INSTANCE) {
      resultBounds.setAttribute(Symbols.NAMES, null);
    }

    this.resultBounds = resultBounds.build();
    this.type = this.resultBounds.storageType();
  }

  private SEXP namesBounds(ValueBounds source) {

    // Names attribute either comes from either:
    // - the dimnames attribute of the source if length(dim(source)) == 1
    // - the names attribute of the source otherwise


    // So if both names and dimnames are null, then we're done.
    SEXP names = source.getAttributeIfConstant(Symbols.NAMES);
    SEXP dimnames = source.getAttributeIfConstant(Symbols.DIMNAMES);

    if(names == Null.INSTANCE && dimnames == Null.INSTANCE) {
      return Null.INSTANCE;
    }

    SEXP dim = source.getAttributeIfConstant(Symbols.DIM);
    if(dim != null) {
      if(dim.length() == 1) {
        if(dimnames == Null.INSTANCE) {
          return Null.INSTANCE;
        }
      } else {
        if(names == Null.INSTANCE) {
          return Null.INSTANCE;
        }
      }
    }

    // varying
    return null;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    // Load the source
    Expression sourceArgument = arguments.get(0).getExpression();
    sourceArgument.load(emitContext, mv);
    emitContext.convert(mv, sourceArgument.getType(), Type.getType(AtomicVector.class));

    // Load the index
    Expression indexArgument = arguments.get(0).getExpression();
    indexArgument.load(emitContext, mv);
    emitContext.convert(mv, indexArgument.getType(), Type.INT_TYPE);

    switch (resultBounds.getTypeSet()) {
      case TypeSet.INT:
        mv.invokeinterface(Type.getInternalName(AtomicVector.class), "getElementAsInt",
            Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE));
        break;
      case TypeSet.DOUBLE:
        mv.invokeinterface(Type.getInternalName(AtomicVector.class), "getElementAsDouble",
            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.INT_TYPE));
        break;

      default:
        throw new UnsupportedOperationException("type: " + TypeSet.toString(resultBounds.getTypeSet()));

    }
  }
}