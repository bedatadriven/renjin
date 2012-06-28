package org.renjin.primitives.annotations.processor.scalars;

import com.sun.codemodel.*;
import org.renjin.primitives.annotations.CastStyle;
import org.renjin.sexp.Vector;


public abstract class ScalarType {
  
  public abstract Class getScalarType();

  public abstract String getConversionMethod();
  
  public abstract String getAccessorMethod();

  public Class getStorageType() {
    return getScalarType();
  }

  public abstract Class getVectorType();
  
  public String convertToStorageTypeExpression(String valueExpression) {
    return valueExpression;
  }

  public abstract String getNALiteral();

  public abstract Class<? extends Vector.Builder<?>> getBuilderClass();

  public String testExpr(String expr, CastStyle castStyle) {
    switch(castStyle) {
    case IMPLICIT:
      return expr + " instanceof Vector && " + getVectorType().getName() + ".VECTOR_TYPE.isWiderThanOrEqualTo((Vector)" + expr + ")";
    case EXPLICIT:
      return expr + " instanceof " + getVectorType().getName();
    }
    throw new IllegalArgumentException("castStyle:" + castStyle);
  }

  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable) {
    JClass vectorClass = codeModel.ref(getVectorType());
    JClass vectorTypeClass = codeModel.ref(vectorClass.name() + ".VECTOR_TYPE");
    return sexpVariable._instanceof(vectorClass)
            .cand(vectorTypeClass.staticInvoke("isWiderThanOrEqualTo").arg(JExpr.cast(vectorClass, sexpVariable)));
  }
}
