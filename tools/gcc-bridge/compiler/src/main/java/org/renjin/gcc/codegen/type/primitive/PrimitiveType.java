/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nullable;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;
import static org.renjin.gcc.codegen.expr.Expressions.constantLong;

public enum PrimitiveType {

  REAL32 {
    @Override
    public GimpleRealType gimpleType() {
      return new GimpleRealType(32);
    }

    @Override
    public Type jvmType() {
      return Type.FLOAT_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "float";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toReal(32);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new RealExpr(gimpleType(), jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new RealExpr(gimpleType(), Expressions.constantFloat(((GimpleRealConstant) expr).getValue().floatValue()));
    }
  },
  REAL64 {

    @Override
    public GimpleRealType gimpleType() {
      return new GimpleRealType(64);
    }

    @Override
    public Type jvmType() {
      return Type.DOUBLE_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "double";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toReal(64);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new RealExpr(gimpleType(), jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new RealExpr(gimpleType(), Expressions.constantDouble(((GimpleRealConstant) expr).getValue()));
    }
  },
  REAL96 {

    @Override
    public GimpleRealType gimpleType() {
      return new GimpleRealType(96);
    }

    @Override
    public Type jvmType() {
      return Type.DOUBLE_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "double";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toReal(96);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new RealExpr(gimpleType(), jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new RealExpr(gimpleType(), Expressions.constantDouble(((GimpleRealConstant) expr).getValue()));
    }
  },
  BOOL {
    @Override
    public GimplePrimitiveType gimpleType() {
      return new GimpleBooleanType();
    }

    @Override
    public Type jvmType() {
      return Type.BOOLEAN_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "boolean";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toBooleanExpr();
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new BooleanExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      GimpleIntegerConstant constantInt = (GimpleIntegerConstant) expr;
      return new BooleanExpr(Expressions.constantInt(constantInt.getNumberValue().intValue()));
    }
  },
  INT8 {
    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.signed(8);
    }

    @Override
    public Type jvmType() {
      return Type.BYTE_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "byte";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toSignedInt(8);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new SignedByteExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new SignedByteExpr(Expressions.i2b(Expressions.constantInt((int) longValue(expr))));
    }
  },

  INT16 {
    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.signed(16);
    }

    @Override
    public Type jvmType() {
      return Type.SHORT_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "short";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toSignedInt(16);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new ShortExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      long longValue = longValue(expr);
      return new ShortExpr(Expressions.i2s(Expressions.constantInt((int) longValue)));
    }
  },

  INT32 {

    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.signed(32);
    }

    @Override
    public Type jvmType() {
      return Type.INT_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "int";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toSignedInt(32);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new SignedIntExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      int intValue = ((GimpleIntegerConstant) expr).getNumberValue().intValue();
      return new SignedIntExpr(constantInt(intValue));
    }
  },
  INT64 {
    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.signed(64);
    }

    @Override
    public Type jvmType() {
      return Type.LONG_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "long";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toSignedInt(64);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new SignedLongExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new SignedLongExpr(constantLong(((GimpleIntegerConstant) expr).getNumberValue().longValue()));
    }
  },

  UINT8 {
    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.unsigned(8);
    }

    @Override
    public Type jvmType() {
      return Type.BYTE_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "byte";
    }

    @Override

    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toUnsignedInt(8);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new UnsignedSmallIntExpr(8, jExpr, address);
    }

    @Override
    public PrimitiveExpr fromNonStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return fromStackValue(new TruncatedByteExpr(jExpr), address);
    }

    @Override
    public Type localVariableType() {
      return Type.INT_TYPE;
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new UnsignedSmallIntExpr(8, Expressions.constantInt((int) PrimitiveType.longValue(expr)));
    }
  },
  UINT16 {

    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.unsigned(16);
    }

    @Override
    public Type jvmType() {
      return Type.CHAR_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "char";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toUnsignedInt(16);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new UnsignedSmallIntExpr(16, jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new UnsignedSmallIntExpr(8, Expressions.constantInt((int) longValue(expr)));
    }
  },
  UINT32 {

    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.unsigned(32);
    }

    @Override
    public Type jvmType() {
      return Type.INT_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "int";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toUnsignedInt(32);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new UnsignedIntExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new UnsignedIntExpr(constantInt((int) longValue(expr)));
    }
  },
  UINT64 {

    @Override
    public GimpleIntegerType gimpleType() {
      return GimpleIntegerType.unsigned(64);
    }

    @Override
    public Type jvmType() {
      return Type.LONG_TYPE;
    }

    @Override
    public String javaTypeName() {
      return "long";
    }

    @Override
    public PrimitiveExpr cast(PrimitiveExpr x) {
      return x.toUnsignedInt(64);
    }

    @Override
    public PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address) {
      return new UnsignedLongExpr(jExpr, address);
    }

    @Override
    public GExpr constantExpr(GimpleConstant expr) {
      return new SignedLongExpr(constantLong(((GimpleIntegerConstant) expr).getNumberValue().longValue()));
    }
  };

  private static long longValue(GimpleConstant expr) {
    GimpleIntegerConstant integerConstant = (GimpleIntegerConstant) expr;
    return integerConstant.getValue().longValue();
  }

  public abstract GimplePrimitiveType gimpleType();

  public abstract Type jvmType();

  public abstract String javaTypeName();

  public abstract PrimitiveExpr cast(PrimitiveExpr x);

  public Type localVariableType() {
    return jvmType();
  }

  public abstract PrimitiveExpr fromStackValue(JExpr jExpr, @Nullable PtrExpr address);

  public final PrimitiveExpr fromStackValue(JExpr jExpr) {
    return fromStackValue(jExpr, null);
  }

  public PrimitiveExpr fromNonStackValue(JExpr jExpr, PtrExpr address) {
    return fromStackValue(jExpr, address);
  }

  public final PrimitiveExpr fromNonStackValue(JExpr jExpr) {
    return fromNonStackValue(jExpr, null);
  }
  public abstract GExpr constantExpr(GimpleConstant expr);

  public JExpr fieldPointer(Type declaringType, String fieldName) {
    String capitalizedJavaTypeName = javaTypeName().substring(0, 1).toUpperCase() + javaTypeName().substring(1);
    Type ptrClassName = Type.getType("Lorg/renjin/gcc/runtime/" + capitalizedJavaTypeName + "FieldPtr;");
    String addressOfDescriptor = Type.getMethodDescriptor(Type.getType(Ptr.class), Type.getType(Class.class), Type.getType(String.class));
    return Expressions.staticMethodCall(ptrClassName, "addressOf", addressOfDescriptor,
          Expressions.constantClass(declaringType), Expressions.constantString(fieldName));
  }

  public static PrimitiveType of(GimplePrimitiveType type) {
    if(type instanceof GimpleRealType) {
      switch (((GimpleRealType) type).getPrecision()) {
        case 32:
          return REAL32;
        case 64:
          return REAL64;
        case 96:
          return REAL96;
      }
    } else if(type instanceof GimpleIntegerType) {
      GimpleIntegerType integerType = (GimpleIntegerType) type;
      if(integerType.isUnsigned()) {
        switch (integerType.getPrecision()) {
          case 8:
            return UINT8;
          case 16:
            return UINT16;
          case 32:
            return UINT32;
          case 64:
            return UINT64;
        }
      } else {
        switch (integerType.getPrecision()) {
          case 8:
            return INT8;
          case 16:
            return INT16;
          case 32:
            return INT32;
          case 64:
            return INT64;
        }
      }
    } else if(type instanceof GimpleBooleanType) {
      return BOOL;
    }
    throw new UnsupportedOperationException("type: " + type);
  }

}
