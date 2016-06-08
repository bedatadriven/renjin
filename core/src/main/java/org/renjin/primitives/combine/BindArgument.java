package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.*;

/**
 * Bind vector/matrix dims and dimnames
 */
class BindArgument {
  final SEXP unevaluated;
  final Vector vector;
  final int rows;
  final int cols;
  final int deparseLevel;
  Context context;

  AtomicVector rowNames = Null.INSTANCE;
  AtomicVector colNames = Null.INSTANCE;

  /**
   * True if the argument is an actual matrix
   */
  final boolean matrix;
  String argName;

  public BindArgument(String argName, Vector vector, boolean defaultToRows, SEXP unevaluated, int deparseLevel, Context context) {
    this.argName = argName;
    this.unevaluated = unevaluated;
    this.deparseLevel = deparseLevel;
    SEXP dim = vector.getAttributes().getDim();
    this.vector = vector;
    if (dim == Null.INSTANCE || dim.length() != 2) {
      if (defaultToRows) {
        rows = 1;
        cols = vector.length();
        colNames = vector.getNames();
      } else {
        cols = 1;
        rows = vector.length();
        rowNames = vector.getNames();
      }
      matrix = false;

    } else {
      AtomicVector dimVector = (AtomicVector) dim;
      rows = dimVector.getElementAsInt(0);
      cols = dimVector.getElementAsInt(1);
      Vector dimNames = (Vector) this.vector.getAttribute(Symbols.DIMNAMES);
      if (dimNames instanceof ListVector && dimNames.length() == 2) {
        rowNames = dimNames.getElementAsSEXP(0);
        colNames = dimNames.getElementAsSEXP(1);
      }

      matrix = true;
    }
  }

  public Vector getClasses () {
    return (Vector) vector.getAttribute(Symbols.CLASS);
  }

  public String getArgName() {
    return this.argName;
  }

  public String getName() {
    String name;
    String typeName = this.unevaluated.getTypeName();
    SEXP expression = ((Promise) this.unevaluated).getExpression();
    String expressionType = expression.getTypeName();

    if (this.argName != null && this.argName.length() > 0) {
      name = this.argName;
    } else if (deparseLevel == 1 && typeName.equals("promise") && expressionType.equals("symbol")) {
      name = expression.asString();
    } else if (deparseLevel == 2) {
      if (typeName.equals("promise") && expressionType.equals("symbol")) {
        name = expression.asString();
      } else {
        name = Deparse.deparse(context, expression, 0, false, 0, 0);
      }
      if (name.length() > 10) {
        name = new String(name.substring(0,10)+"...");
      }
    } else {
      name = this.argName;
    }
    return name;
  }

  public boolean hasName() {
    boolean hasName;
    String typeName = this.unevaluated.getTypeName();
    SEXP expression = ((Promise) this.unevaluated).getExpression();
    String expressionType = expression.getTypeName();

    if (this.argName != null && this.argName.length() > 0) {
      hasName = true;
    } else if (deparseLevel == 1 && typeName.equals("promise") && expressionType.equals("symbol")) {
      hasName = (expression.asString().length() > 0);
    } else if (deparseLevel == 2) {
      hasName = true;
    } else {
      hasName = false;
    }
    return hasName;
  }

}
