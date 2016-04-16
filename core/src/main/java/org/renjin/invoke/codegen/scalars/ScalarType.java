package org.renjin.invoke.codegen.scalars;

import com.sun.codemodel.*;
import org.renjin.invoke.annotations.DownCastComplex;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.Vector;


public abstract class ScalarType {

  public abstract Class getScalarType();

  public abstract String getConversionMethod();

  public abstract String getAccessorMethod();

  public abstract Class getVectorType();

  public abstract Class<? extends Vector.Builder<?>> getBuilderClass();

  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, Argument formal) {
    JClass vectorClass = codeModel.ref(Vector.class);
    JExpression vectorType =  codeModel.ref(getVectorType()).staticRef("VECTOR_TYPE");
    JExpression condition = sexpVariable._instanceof(vectorClass)
        .cand(vectorType.invoke("isWiderThanOrEqualTo").arg(JExpr.cast(vectorClass, sexpVariable)));

    if(formal.isAnnotatedWith(DownCastComplex.class)) {
      condition = condition.cor(sexpVariable._instanceof(codeModel.ref(ComplexVector.class)));
    }

    return condition;
  }

  public Vector.Type getVectorTypeInstance() {
    try {
      return (Vector.Type) getVectorType().getField("VECTOR_TYPE").get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
