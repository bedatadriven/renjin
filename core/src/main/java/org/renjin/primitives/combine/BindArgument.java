package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.*;

/**
 * Bind vector/matrix dims and dimnames
 */
class BindArgument {
  final SEXP uneval;
  final Vector vector;
  final int rows;
  final int cols;
  final int deparseLevel;
  Context context;
  SEXP expression;

  AtomicVector rowNames = Null.INSTANCE;
  AtomicVector colNames = Null.INSTANCE;

  /**
   * True if the argument is an actual matrix
   */
  final boolean matrix;
  String argName;

  public BindArgument(String argName, Vector vector, boolean defaultToRows, SEXP uneval, int deparseLevel, Context context) {
    this.argName = argName;
    this.uneval = uneval;
    this.deparseLevel = deparseLevel;
    SEXP dim = vector.getAttributes().getDim();
    this.vector = vector;
    this.expression = uneval instanceof Promise ? ((Promise) uneval).getExpression() : uneval;
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
    if (this.argName != null && this.argName.length() > 0) {
      name = this.argName;
    } else if (deparseLevel == 1 && this.expression instanceof Symbol) {
      name = this.expression.asString();
    } else if (deparseLevel == 2) {
      if (this.expression instanceof Symbol) {
        name = this.expression.asString();
      } else {
        name = Deparse.deparse(context, this.expression, 0, false, 0, 0);
      }
      if (name.length() > 10) {
        name = name.substring(0,10) + "...";
      }
    } else {
      name = this.argName;
    }
    return name;
  }

  public boolean hasName() {
    boolean hasName;
    if (this.argName != null && this.argName.length() > 0) {
      hasName = true;
    } else if (deparseLevel == 1 && this.expression instanceof Symbol) {
      hasName = (this.expression.asString().length() > 0);
    } else if (deparseLevel == 2) {
      hasName = true;
    } else {
      hasName = false;
    }
    return hasName;
  }

  public Promise repromise() {
    return new Promise(this.expression, (SEXP) this.vector);
  }

}
