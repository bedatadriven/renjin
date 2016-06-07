package org.renjin.primitives.combine;

import org.renjin.sexp.*;

/**
 * Bind vector/matrix dims and dimnames
 */
class BindArgument {
  final SEXP unevaluated;
  final Vector vector;
  final int rows;
  final int cols;

  AtomicVector rowNames = Null.INSTANCE;
  AtomicVector colNames = Null.INSTANCE;

  /**
   * True if the argument is an actual matrix
   */
  final boolean matrix;
  String argName;

  public BindArgument(String argName, Vector vector, boolean defaultToRows, SEXP unevaluated) {
    this.argName = argName;
    this.unevaluated = unevaluated;
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
    if (this.argName != null && this.argName.length() > 0) {
      name = this.argName;
    } else if (this.unevaluated.getTypeName().equals("promise")) {
      name = ((Promise) this.unevaluated).getExpression().asString();
    } else {
      name = this.argName;
    }
    return name;
  }

  public boolean hasName() {
    boolean hasName;
    if (this.argName != null && this.argName.length() > 0) {
      hasName = true;
    } else if (this.unevaluated.getTypeName().equals("promise")) {
      hasName = (((Promise) this.unevaluated).getExpression().asString().length() > 0);
    } else {
      hasName = false;
    }
    return hasName;
  }

}
