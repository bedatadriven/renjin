package org.renjin.gcc.translate.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.*;
import org.renjin.gcc.runtime.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.field.PrimitiveFieldExpr;
import org.renjin.gcc.translate.var.PrimitiveHeapVar;
import org.renjin.gcc.translate.var.PrimitiveStackVar;
import org.renjin.gcc.translate.var.Variable;

import java.lang.reflect.Array;


public enum ImPrimitiveType implements ImType {

  DOUBLE {
    @Override
    public Class getPrimitiveClass() {
      return double.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      return DoublePtr.class;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.doubleConstant(((Number)value).doubleValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 8;
    }
  },
  FLOAT {
    @Override
    public Class getPrimitiveClass() {
      return float.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      throw new UnsupportedOperationException();
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.floatConstant(((Number)value).floatValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 4;
    }
  },
  INT {
    @Override
    public Class getPrimitiveClass() {
      return int.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      return IntPtr.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      return 4;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.integerConstant(((Number)value).intValue());
    }
  },
  LONG {
    @Override
    public Class getPrimitiveClass() {
      return long.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      return LongPtr.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      return 8;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.longConstant(((Number)value).intValue());
    }
  },
  BOOLEAN {
    @Override
    public Class getPrimitiveClass() {
      return boolean.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      return BooleanPtr.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      throw new UnsupportedOperationException("to check");
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      if(value instanceof Boolean) {
        return JimpleExpr.integerConstant( ((Boolean)value) ? 1 : 0 );
      } else {
        return literalExpr( ((Number)value).intValue() != 0 );
      }
    }
  },
  CHAR {
    @Override
    public Class getPrimitiveClass() {
      return char.class;
    }

    @Override
    public Class getPointerWrapperClass() {
      return CharPtr.class;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.integerConstant( ((Number)value).intValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 1;
    }
  };

  @Override
  public JimpleType paramType() {
    return asJimple();
  }

  @Override
  public JimpleType returnType() {
    return asJimple();
  }

  @Override
  public Type jvmReturnType() {
    return Type.getType(getPrimitiveClass());
  }

  @Override
  public Type jvmParamType() {
    return Type.getType(getPrimitiveClass());
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    JimpleFieldBuilder field = classBuilder.newField();
    field.setName(memberName);
    field.setType(asJimple());
    field.setModifiers(JimpleModifiers.STATIC, JimpleModifiers.PUBLIC);
  }

  public JimpleType asJimple() {
    return new RealJimpleType(getPrimitiveClass());
  }

  public abstract Class getPrimitiveClass();

  public abstract Class getPointerWrapperClass();

  
  
  public JimpleType getPointerWrapperType() {
    return new RealJimpleType(getPointerWrapperClass());
  }

  public Class getArrayClass() {
    return Array.newInstance(getPrimitiveClass(), 0).getClass();
  }

  public JimpleType jimpleArrayType() {
    return new RealJimpleType(getArrayClass());
  }

  @Override
  public ImPrimitivePtrType pointerType() {
    return new ImPrimitivePtrType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    return new ImPrimitiveArrayType(this, lowerBound, upperBound);
  }

  public abstract JimpleExpr literalExpr(Object value);

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    if (usage.isAddressed()) {
      return new PrimitiveHeapVar(functionContext, this, gimpleName);
    } else {
      return new PrimitiveStackVar(functionContext, this, gimpleName);
    }
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    return new PrimitiveFieldExpr(null, classType, memberName, this);
  }

  /**
   * @return  the storage size of the type, as understood by GCC. This is used
   * to convert pointer offsets, provided to us in bytes, to array index offsets.
   */
  public abstract int getStorageSizeInBytes();

  public JimpleExpr castIfNeeded(JimpleExpr expr, ImPrimitiveType type) {
    if(type != this) {
      return JimpleExpr.cast(expr, asJimple());
    } else {
      return expr;
    }
  }

  public static ImPrimitiveType valueOf(GimpleType type) {
    if (type instanceof GimpleRealType) {
      switch(((GimpleRealType) type).getPrecision()) {
        case 32:
          return FLOAT;
        case 64:
          return DOUBLE;
      }
    } else if (type instanceof GimpleIntegerType) {
      int precision = ((GimpleIntegerType) type).getPrecision();
      if(precision == 0) {
        precision = ((GimpleIntegerType) type).getSize();
      }
      switch(precision) {
        case 8:
          return CHAR;
        case 32:
          return INT;
        case 64:
          return LONG;
      }
    } else if (type instanceof GimpleBooleanType) {
      return BOOLEAN;
    }
    throw new UnsupportedOperationException("type:" + type);
  }

  public static ImPrimitiveType valueOf(JimpleType type) {
    for(ImPrimitiveType value : values()) {
      if(value.asJimple().equals(type)) {
        return value;
      }
    }
    throw new UnsupportedOperationException(type.toString());
  }


  public static ImPrimitiveType valueOf(Class type) {
    for(ImPrimitiveType value : values()) {
      if(value.getPrimitiveClass().equals(type)) {
        return value;
      }
    }
    throw new UnsupportedOperationException(type.toString());
  }



  @Override
  public String toString() {
    return name().toLowerCase();
  }

}
