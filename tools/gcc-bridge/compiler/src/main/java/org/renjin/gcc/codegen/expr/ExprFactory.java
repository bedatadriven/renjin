/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunPtrCallGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.ConstConditionGenerator;
import org.renjin.gcc.codegen.condition.NullCheckGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexCmpGenerator;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.primitive.*;
import org.renjin.gcc.codegen.type.record.RecordExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.symbols.SymbolTable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Creates code-generating {@link GExpr}s from {@code GimpleExpr}s
 */
public class ExprFactory {
  private final TypeOracle typeOracle;
  private final SymbolTable symbolTable;
  private MethodGenerator mv;
  private Optional<VPtrExpr> varArgsPtr;

  public ExprFactory(TypeOracle typeOracle, SymbolTable symbolTable, MethodGenerator mv, Optional<VPtrExpr> varArgsPtr) {
    this.typeOracle = typeOracle;
    this.symbolTable = symbolTable;
    this.mv = mv;
    this.varArgsPtr = varArgsPtr;
  }

  public ExprFactory(TypeOracle typeOracle, SymbolTable symbolTable, MethodGenerator mv) {
    this(typeOracle, symbolTable, mv, Optional.empty());
  }

  public Optional<VPtrExpr> getVarArgsPtr() {
    return varArgsPtr;
  }

  public GExpr findGenerator(GimpleExpr expr, GimpleType expectedType) {
    return maybeCast(findGenerator(expr), expectedType, expr.getType());
  }

  public GExpr maybeCast(GExpr rhs, GimpleType lhsType, GimpleType rhsType) {
    
    if(lhsType.equals(rhsType)) {
      return rhs;
    }
    
    TypeStrategy leftStrategy = typeOracle.forType(lhsType);
    TypeStrategy rightStrategy = typeOracle.forType(rhsType);
    
    try {
      return leftStrategy.cast(mv, rhs);
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException(String.format("Unsupported cast to %s [%s] from %s [%s]",
          lhsType, leftStrategy.getClass().getSimpleName(),
          rhsType, rightStrategy.getClass().getSimpleName()), e);
    }
  }

  public GExpr findGenerator(GimpleExpr expr) {
    if(expr instanceof GimpleSymbolRef) {
      GExpr variable = symbolTable.getVariable((GimpleSymbolRef) expr);
      if(variable == null) {
        throw new InternalCompilerException("No such variable: " + expr);
      }
      return variable;

    } else if(expr instanceof GimpleConstant) {
      return forConstant((GimpleConstant) expr);

    } else if(expr instanceof GimpleConstructor) {
      return forConstructor((GimpleConstructor) expr);

    } else if(expr instanceof GimpleNopExpr) {
      return findGenerator(((GimpleNopExpr) expr).getValue(), expr.getType());

    } else if(expr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) expr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef functionRef = (GimpleFunctionRef) addressOf.getValue();
        return new FunPtrExpr(symbolTable.findHandle(functionRef));

      } else if(addressOf.getValue() instanceof GimplePrimitiveConstant) {
        // Exceptionally, gimple often contains to address of constants when
        // passing them to functions

        JExpr value = findPrimitiveGenerator(addressOf.getValue());
        PrimitiveType primitiveType = PrimitiveType.of((GimplePrimitiveType) addressOf.getValue().getType());
        return new FatPtrPair(new PrimitiveValueFunction(primitiveType),
            Expressions.newArray(primitiveType.jvmType(), Collections.singletonList(value)));

      } else  {

        // Try to simplify expressions in the form &*x to x
        if(addressOf.getValue() instanceof GimpleMemRef) {
          GimpleMemRef memRef = (GimpleMemRef) addressOf.getValue();
          if(memRef.isOffsetZero()) {
            return findGenerator(memRef.getPointer());
          } else {
            return pointerPlus(memRef.getPointer(), memRef.getOffset(), memRef.getPointer().getType());
          }
        }

        // Otherwise delegate addressOf operation to expr generator
        GExpr value = findGenerator(addressOf.getValue());
        try {
          return value.addressOf();
        } catch (ClassCastException | UnsupportedOperationException ignored) {
          throw new InternalCompilerException(addressOf.getValue() + " [" + value.getClass().getName() + "] is not addressable");
        }
      }

    } else if(expr instanceof GimpleMemRef) {
      GimpleMemRef memRefExpr = (GimpleMemRef) expr;
      return memRef(memRefExpr, memRefExpr.getType());

    } else if(expr instanceof GimpleArrayRef) {
      GimpleArrayRef arrayRef = (GimpleArrayRef) expr;
      ArrayExpr array = (ArrayExpr)findGenerator(arrayRef.getArray());
      GExpr index = findGenerator(arrayRef.getIndex());
      JExpr jvmIndex = index.toPrimitiveExpr().toSignedInt(32).jexpr();

      return array.elementAt(expr.getType(), jvmIndex);

    } else if(expr instanceof GimpleConstantRef) {
      GimpleConstant constant = ((GimpleConstantRef) expr).getValue();
      JExpr constantValue = findPrimitiveGenerator(constant);
      PrimitiveType primitiveType = PrimitiveType.of((GimplePrimitiveType) constant.getType());
      FatPtrPair address = new FatPtrPair(
          new PrimitiveValueFunction(primitiveType),
          Expressions.newArray(primitiveType.jvmType(), Collections.singletonList(constantValue)));
      
      return PrimitiveType.of((GimplePrimitiveType) expr.getType()).fromStackValue(constantValue, address);

    } else if(expr instanceof GimpleComplexPartExpr) {
      GimpleExpr complexExpr = ((GimpleComplexPartExpr) expr).getComplexValue();
      ComplexExpr complexGenerator = (ComplexExpr) findGenerator(complexExpr);
      if (expr instanceof GimpleRealPartExpr) {
        return complexGenerator.getRealGExpr();
      } else {
        return complexGenerator.getImaginaryGExpr();
      }
    } else if (expr instanceof GimpleComponentRef) {
      GimpleComponentRef ref = (GimpleComponentRef) expr;
      RecordExpr record = (RecordExpr)findGenerator(((GimpleComponentRef) expr).getValue());

      return record.memberOf(mv, ref.getMember().getOffset(), ref.getMember().getSize(), expr.getType());

    } else if (expr instanceof GimpleBitFieldRefExpr) {
      GimpleBitFieldRefExpr ref = (GimpleBitFieldRefExpr) expr;
      RecordExpr record = (RecordExpr)findGenerator(ref.getValue());

      return record.memberOf(mv, ref.getOffset(), ref.getSize(), expr.getType());

    } else if(expr instanceof GimpleCompoundLiteral) {
      return findGenerator(((GimpleCompoundLiteral) expr).getDecl());
    
    } else if(expr instanceof GimpleObjectTypeRef) {
      GimpleObjectTypeRef typeRef = (GimpleObjectTypeRef) expr;
      
      return findGenerator(typeRef.getExpr());
    
    } else if(expr instanceof GimplePointerPlus) {
      GimplePointerPlus pointerPlus = (GimplePointerPlus) expr;
      return pointerPlus(pointerPlus.getPointer(), pointerPlus.getOffset(), pointerPlus.getType());
    }
    
    throw new UnsupportedOperationException(expr + " [" + expr.getClass().getSimpleName() + "]");
  }

  private GExpr forConstructor(GimpleConstructor expr) {
    return typeOracle.forType(expr.getType()).constructorExpr(this, mv, expr);
  }

  public CallGenerator findCallGenerator(GimpleExpr functionExpr) {
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return symbolTable.findCallGenerator(ref);
      }
      GimpleAddressOf address = (GimpleAddressOf) functionExpr;
      throw new UnsupportedOperationException("function ref: " + address.getValue() +
          " [" + address.getValue().getClass().getSimpleName() + "]");

    }

    // Assume this is a function pointer ptr expression  
    FunPtrExpr expr = findGenerator(functionExpr).toFunPtr();
    return new FunPtrCallGenerator(typeOracle, (GimpleFunctionType) functionExpr.getType().getBaseType(), expr.jexpr());
  }

  public ConditionGenerator findConditionGenerator(GimpleOp op, List<GimpleExpr> operands) {
    if(operands.size() == 2) {
      return findComparisonGenerator(op, operands.get(0), operands.get(1));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private ConditionGenerator findComparisonGenerator(GimpleOp op, GimpleExpr x, GimpleExpr y) {

    if(x.getType() instanceof org.renjin.gcc.gimple.type.GimpleComplexType) {
      return new ComplexCmpGenerator(op, findComplexGenerator(x), findComplexGenerator(y));

    } else if(x.getType() instanceof GimplePrimitiveType) {
      return findGenerator(x).toPrimitiveExpr().compareTo(op, findGenerator(y));

    } else if(x.getType() instanceof GimpleIndirectType) {
      return comparePointers(op, x, y);
      
    } else {
      throw new UnsupportedOperationException("Unsupported comparison " + op + " between types " +
          x.getType() + " and " + y.getType());
    }
  }

  private ConditionGenerator comparePointers(GimpleOp op, GimpleExpr x, GimpleExpr y) {
    
    // First see if this is a null check
    if(isNull(x) && isNull(y)) {
      switch (op) {
        case EQ_EXPR:
        case GE_EXPR:
        case LE_EXPR:
          return new ConstConditionGenerator(true);
        case NE_EXPR:
        case LT_EXPR:
        case GT_EXPR:
          return new ConstConditionGenerator(false);
        default:
          throw new UnsupportedOperationException("op: " + op);
      }
    } else if(isNull(x)) {
      return new NullCheckGenerator(op, (PtrExpr) findGenerator(y));
    } else if(isNull(y)) {
      return new NullCheckGenerator(op, (PtrExpr) findGenerator(x));
    }
    
    // Shouldn't matter which we pointer we cast to the other, but if we have a choice,
    // cast away from a void* to a concrete pointer type
    GimpleType commonType;

    if(x.getType().isPointerTo(GimpleVoidType.class)) {
      commonType = y.getType();
    } else {
      commonType = x.getType();
    }

    PtrExpr ptrX = (PtrExpr) findGenerator(x, commonType);
    PtrExpr ptrY = (PtrExpr) findGenerator(y, commonType);

    return ptrX.comparePointer(mv, op, ptrY);
  }

  private boolean isNull(GimpleExpr expr) {
    return expr instanceof GimpleConstant && ((GimpleConstant) expr).isNull();
  }

  public GExpr findGenerator(GimpleOp op, List<GimpleExpr> operands, GimpleType expectedType) {


    switch (op) {

      case NEGATE_EXPR:
        return findGenerator(operands.get(0)).toNumericExpr().negative();

      case BIT_NOT_EXPR:
        return findGenerator(operands.get(0)).toPrimitiveExpr().toIntExpr().bitwiseNot();

      case COMPLEX_EXPR:
        return new ComplexExpr(findPrimitiveGenerator(operands.get(0)));

      case PLUS_EXPR:
      case MINUS_EXPR:
      case MULT_EXPR:
      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
      case TRUNC_MOD_EXPR:
      case BIT_IOR_EXPR:
      case BIT_XOR_EXPR:
      case BIT_AND_EXPR:
      case LSHIFT_EXPR:
      case RSHIFT_EXPR:
      case LROTATE_EXPR:
        return findBinaryGenerator(op, operands);

      case POINTER_PLUS_EXPR:
        return pointerPlus(operands.get(0), operands.get(1), expectedType);

      case MEM_REF:
        // Cast the pointer type first, then dereference
        return memRef((GimpleMemRef) operands.get(0), expectedType);

      case CONSTRUCTOR:
      case CONVERT_EXPR:
      case FIX_TRUNC_EXPR:
      case FLOAT_EXPR:
      case PAREN_EXPR:
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
      case INTEGER_CST:
      case REAL_CST:
      case STRING_CST:
      case COMPLEX_CST:
      case ADDR_EXPR:
      case ARRAY_REF:
      case COMPONENT_REF:
      case BIT_FIELD_REF:
      case REALPART_EXPR:
      case IMAGPART_EXPR:
        return maybeCast(findGenerator(operands.get(0)), expectedType, operands.get(0).getType());

      case TRUTH_NOT_EXPR:
        return findGenerator(operands.get(0)).toPrimitiveExpr().toBooleanExpr().bitwiseNot();

      case TRUTH_AND_EXPR:
        return findGenerator(operands.get(0)).toPrimitiveExpr().toBooleanExpr().bitwiseAnd(findGenerator(operands.get(1)));

      case TRUTH_OR_EXPR:
        return findGenerator(operands.get(0)).toPrimitiveExpr().toBooleanExpr().bitwiseOr(findGenerator(operands.get(1)));

      case TRUTH_XOR_EXPR:
        return findGenerator(operands.get(0)).toPrimitiveExpr().toBooleanExpr().bitwiseXor(findGenerator(operands.get(1)));

      case ORDERED_EXPR:
      case UNORDERED_EXPR:
      case EQ_EXPR:
      case LT_EXPR:
      case LE_EXPR:
      case NE_EXPR:
      case GT_EXPR:
      case GE_EXPR:
      case UNEQ_EXPR:
      case UNLT_EXPR:
      case UNLE_EXPR:
      case UNGT_EXPR:
      case UNGE_EXPR:
        return booleanValue(findComparisonGenerator(op,operands.get(0), operands.get(1)));

      case MAX_EXPR:
        return findGenerator(operands.get(0)).toNumericExpr().max(findGenerator(operands.get(1)));

      case MIN_EXPR:
        return findGenerator(operands.get(0)).toNumericExpr().min(findGenerator(operands.get(1)));


      case ABS_EXPR:
        return findGenerator(operands.get(0)).toNumericExpr().absoluteValue();


      case CONJ_EXPR:
        return findComplexGenerator(operands.get(0)).conjugate();


      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private GExpr findBinaryGenerator(GimpleOp op, List<GimpleExpr> operands) {
    GExpr x = findGenerator(operands.get(0));
    GExpr y = findGenerator(operands.get(1));

    // Fixup for integers that "carry" pointers.
    // If the second argument is a PtrCarryingExpr but the first is not,
    // lift the first argument into a PtrCarryingExpr

    if (  y instanceof PtrCarryingExpr &&
        !(x instanceof PtrCarryingExpr) &&
          x instanceof NumericIntExpr) {

      x = new PtrCarryingExpr(((NumericIntExpr) x), ((PtrCarryingExpr) y).getPointerExpr());
    }

    switch (op) {
      case PLUS_EXPR:
        return x.toNumericExpr().plus(y);

      case MINUS_EXPR:
        return x.toNumericExpr().minus(y);

      case MULT_EXPR:
        return x.toNumericExpr().multiply(y);

      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
        return x.toNumericExpr().divide(y);

      case TRUNC_MOD_EXPR:
        return x.toPrimitiveExpr().toNumericExpr().remainder(y);

      case BIT_IOR_EXPR:
        return x.toPrimitiveExpr().toIntExpr().bitwiseOr(y);

      case BIT_XOR_EXPR:
        return x.toPrimitiveExpr().toIntExpr().bitwiseXor(y);

      case BIT_AND_EXPR:
        return x.toPrimitiveExpr().toIntExpr().bitwiseAnd(y);

      case LSHIFT_EXPR:
        return x.toPrimitiveExpr().toIntExpr().shiftLeft(y);

      case RSHIFT_EXPR:
        return x.toPrimitiveExpr().toIntExpr().shiftRight(y);

      case LROTATE_EXPR:
        return x.toPrimitiveExpr().toIntExpr().rotateLeft(y);

    }
    return null;
  }

  private GExpr booleanValue(ConditionGenerator condition) {
    return new BooleanExpr(new ConditionExpr(condition));
  }

  private GExpr memRef(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    
    // Case of *&x, which can be simplified to x
    if(pointer instanceof GimpleAddressOf && gimpleExpr.isOffsetZero()) {
      GimpleAddressOf addressOf = (GimpleAddressOf) pointer;
      return findGenerator(addressOf.getValue());
    }
    
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    
    if(pointerType.getBaseType() instanceof GimpleVoidType) {
      // We can't dereference a null pointer, so cast the pointer first, THEN dereference
      return castThenDereference(gimpleExpr, expectedType);
    
    } else {
      return dereferenceThenCast(gimpleExpr, expectedType);
    }
  }

  private GExpr castThenDereference(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    GimpleIndirectType pointerType = (GimpleIndirectType) pointer.getType();
    GimpleIndirectType expectedPointerType = expectedType.pointerTo();
    
    // Cast from the void pointer type to the "expected" pointer type
    PtrExpr ptrExpr = (PtrExpr) maybeCast(findGenerator(pointer), expectedPointerType, pointerType);

    if(!gimpleExpr.isOffsetZero()) {
      JExpr offsetInBytes = findPrimitiveGenerator(gimpleExpr.getOffset());

      ptrExpr =  ptrExpr.pointerPlus(mv, offsetInBytes);
    }

    return ptrExpr.valueOf(expectedType);
  }
  
  private GExpr dereferenceThenCast(GimpleMemRef gimpleExpr, GimpleType expectedType) {
    GimpleExpr pointer = gimpleExpr.getPointer();
    PtrExpr ptrExpr = (PtrExpr) findGenerator(pointer);

    if(!gimpleExpr.isOffsetZero()) {
      JExpr offsetInBytes = findPrimitiveGenerator(gimpleExpr.getOffset());
      ptrExpr =  ptrExpr.pointerPlus(mv, offsetInBytes);
    }
    
    return ptrExpr.valueOf(expectedType);
  }

  private GExpr pointerPlus(GimpleExpr pointerExpr, GimpleExpr offsetExpr, GimpleType expectedType) {
    PtrExpr pointer = (PtrExpr) findGenerator(pointerExpr);
    JExpr offsetInBytes = findPrimitiveGenerator(offsetExpr);

    GimpleType pointerType = pointerExpr.getType();
    GExpr result = pointer.pointerPlus(mv, offsetInBytes);
    
    return maybeCast(result, expectedType, pointerType);
  }

  private <T extends GExpr> T findGenerator(GimpleExpr gimpleExpr, Class<T> exprClass) {
    GExpr expr = findGenerator(gimpleExpr);
    if(exprClass.isAssignableFrom(expr.getClass())) {
      return exprClass.cast(expr);
    } else {
      throw new InternalCompilerException(String.format("Expected %s for expr %s, found: %s", 
          exprClass.getSimpleName(), 
          gimpleExpr, 
          expr.getClass().getName()));
    }
  }

  public JExpr findPrimitiveGenerator(GimpleExpr gimpleExpr) {
    // When looking specifically for a value generator, treat a null pointer as zero integer
    if(gimpleExpr instanceof GimplePrimitiveConstant && gimpleExpr.getType() instanceof GimpleIndirectType) {
      return Expressions.constantInt(((GimplePrimitiveConstant) gimpleExpr).getValue().intValue());
    }
    PrimitiveExpr primitive = findGenerator(gimpleExpr, PrimitiveExpr.class);
    return primitive.jexpr();
  }


  private ComplexExpr findComplexGenerator(GimpleExpr gimpleExpr) {
    return findGenerator(gimpleExpr, ComplexExpr.class);
  }


  public TypeStrategy strategyFor(GimpleType type) {
    return typeOracle.forType(type);
  }


  public GExpr forConstant(GimpleConstant constant) {
    if (constant.getType() instanceof GimpleIndirectType) {
      // TODO: Treat all pointer constants as null
      return typeOracle.forPointerType(constant.getType()).nullPointer();

    } else if (constant instanceof GimplePrimitiveConstant) {

      GimplePrimitiveType gimplePrimitiveType = (GimplePrimitiveType) constant.getType();
      PrimitiveType primitiveType = PrimitiveType.of(gimplePrimitiveType);
      return primitiveType.constantExpr(constant);

    } else if (constant instanceof GimpleComplexConstant) {
      GimpleComplexConstant complexConstant = (GimpleComplexConstant) constant;
      return new ComplexExpr(
          forConstant(complexConstant.getReal()),
          forConstant(complexConstant.getIm()));

    } else if (constant instanceof GimpleStringConstant) {
      StringConstant array = new StringConstant(((GimpleStringConstant) constant).getValue());
      return new FatArrayExpr(
          (GimpleArrayType) constant.getType(),
          new PrimitiveValueFunction(PrimitiveType.UINT8),
          array.getLength(),
          array);

    } else {
      throw new UnsupportedOperationException("constant: " + constant);
    }
  }
}
