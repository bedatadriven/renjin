/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.Comparison;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.InverseConditionGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nullable;

import static org.renjin.gcc.codegen.expr.Expressions.staticMethodCall;
import static org.renjin.gcc.codegen.expr.Expressions.zero;

/**
 * 32-bit and 64-bit floating point values.
 */
public class RealExpr extends AbstractPrimitiveExpr implements NumericExpr {

  private GimpleRealType realType;
  private Type jvmType;

  public RealExpr(GimpleRealType realType, JExpr expr, @Nullable GExpr address) {
    super(expr, address);
    this.realType = realType;
    this.jvmType = realType.jvmType();
  }

  public RealExpr(GimpleRealType realType, JExpr expr) {
    this(realType, expr, null);
  }

  public int getPrecision() {
    return realType.getPrecision();
  }

  @Override
  public RealExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  @Override
  public RealExpr minus(GExpr operand) {
    return lift(Expressions.difference(jexpr(), jexpr(operand)));
  }

  @Override
  public RealExpr multiply(GExpr operand) {
    return lift(Expressions.product(jexpr(), jexpr(operand)));
  }

  @Override
  public RealExpr divide(GExpr operand) {
    return lift(Expressions.divide(jexpr(), jexpr(operand)));
  }

  @Override
  public RealExpr negative() {
    return lift(Expressions.negative(jexpr()));
  }


  @Override
  public RealExpr min(GExpr operand) {
    return lift(staticMethodCall(Math.class, "min", minMaxSignature(), jexpr(), jexpr(operand)));
  }
  @Override
  public RealExpr max(GExpr operand) {
    return lift(staticMethodCall(Math.class, "max", minMaxSignature(), jexpr(), jexpr(operand)));
  }

  @Override
  public RealExpr absoluteValue() {
    return lift(staticMethodCall(Math.class, "abs", absSignature(), jexpr()));
  }

  private String absSignature() {
    return Type.getMethodDescriptor(jvmType, jvmType);
  }

  private String minMaxSignature() {
    return Type.getMethodDescriptor(jvmType, jvmType, jvmType);
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toReal(getPrecision()).jexpr();
  }

  public ConditionGenerator ordered(GExpr operand) {

    final JExpr x = jexpr();
    final JExpr y = jexpr(operand);

    return new ConditionGenerator() {
      @Override
      public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
        x.load(mv);
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, x.getType()));
        mv.ifne(falseLabel);
        y.load(mv);
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, y.getType()));
        mv.ifne(falseLabel);
        mv.goTo(trueLabel);
      }
    };
  }

  public ConditionGenerator unordered(GExpr operand) {
    return new InverseConditionGenerator(ordered(operand));
  }

  private RealExpr lift(JExpr expr) {
    return new RealExpr(realType, expr);
  }

  public IntExpr toSignedInt() {
    switch (getPrecision()) {
      case 32:
        return new SignedIntExpr(Expressions.f2i(jexpr()));
      case 64:
        return new SignedLongExpr(Expressions.d2l(jexpr()));
    }
    throw new IllegalStateException("precision: " + getPrecision());
  }

  @Override
  public IntExpr toIntExpr() {
    return toSignedInt();
  }

  @Override
  public RealExpr toRealExpr() {
    return this;
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return toIntExpr().toBooleanExpr();
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    boolean isDouble = jvmType.equals(Type.DOUBLE_TYPE);
    switch (precision) {
      case 64:
        return new SignedLongExpr(isDouble ? Expressions.d2l(jexpr()) : Expressions.f2l(jexpr()));
      case 8:
      case 16:
      case 32:
        return new SignedIntExpr(isDouble ? Expressions.d2i(jexpr()) : Expressions.f2i(jexpr())).toSignedInt(precision);

    }
    return toSignedInt().toSignedInt(precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    // Always start with conversion to 64-bit long to avoid loss of magnitude
    // for unsigned 32-bit integers.
    return new UnsignedLongExpr(Expressions.d2l(jexpr())).toUnsignedInt(precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    if(this.getPrecision() == precision) {
      return this;
    }
    return new RealExpr(new GimpleRealType(precision), jexpr(precision));
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {

    // Branching on floating point comparisons requires two steps:
    // First we have to do the actual comparison, using DCMPG/DCMPL/FCMPL/FCMPG,
    // which compares the two operands and pushes -1, 0, or 1 onto the stack

    // Then we can compare this value to zero and branch on the result
    // using the Comparison condition generator.

    // But because we have floating points, we need to be mindful of NaN values.

    //            CMPG:     CMPL
    // x <  y       -1        -1
    // y == 0        0         0
    // x >  y        1         1
    // NaN           1        -1

    // So if we're interested in whether x is less than y, we need to use
    // CMPL to make sure that our condition is false if either x or y is NaN

    switch (op) {
      case ORDERED_EXPR:
        return ordered(operand);
      case UNORDERED_EXPR:
        return unordered(operand);
      case UNGT_EXPR:
      case LT_EXPR:
      case LE_EXPR:
        return new Comparison(op, Expressions.cmpg(jexpr(), jexpr(operand)));
      default:
        return new Comparison(op, Expressions.cmpl(jexpr(), jexpr(operand)));
    }
  }

  private JExpr jexpr(int targetPrecision) {
    if(this.getPrecision() == targetPrecision) {
      return jexpr();
    } else if(this.getPrecision() == 32 && targetPrecision == 64) {
      return Expressions.f2d(jexpr());
    } else if(this.getPrecision() == 64 && targetPrecision == 32) {
      return Expressions.d2f(jexpr());
    } else {
      throw new IllegalArgumentException(getPrecision() + "=>" + targetPrecision);
    }
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  @Override
  public NumericExpr toNumericExpr() {
    return this;
  }

  @Override
  public ComplexExpr toComplexExpr() {
    return new ComplexExpr(jexpr(), zero(jexpr().getType()));
  }
}
