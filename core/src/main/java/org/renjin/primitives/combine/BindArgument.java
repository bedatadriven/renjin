package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.*;

/**
 * Bind vector/matrix dims and dimnames
 */
class BindArgument {
  private final SEXP uneval;
  private final Vector vector;
  private final int rows;
  private final int cols;
  private final int deparseLevel;
  Context context;
  private SEXP expression;

  private AtomicVector rowNames = Null.INSTANCE;
  private AtomicVector colNames = Null.INSTANCE;

  /**
   * True if the argument is an actual matrix
   */
  private final boolean matrix;
  private String argName;
  private String computedName;

  public BindArgument(String argName, Vector vector, boolean defaultToRows, SEXP uneval, int deparseLevel, Context context) {
    this.argName = argName;
    this.uneval = uneval;
    this.deparseLevel = deparseLevel;
    SEXP dim = vector.getAttributes().getDim();
    this.vector = vector;
    this.expression = uneval instanceof Promise ? ((Promise) uneval).getExpression() : uneval;
    if (dim == Null.INSTANCE || dim.length() != 2) {
      if (defaultToRows) {
        this.rows = 1;
        this.cols = vector.length();
        this.colNames = vector.getNames();
      } else {
        this.cols = 1;
        this.rows = vector.length();
        this.rowNames = vector.getNames();
      }
      matrix = false;

    } else {
      AtomicVector dimVector = (AtomicVector) dim;
      this.rows = dimVector.getElementAsInt(0);
      this.cols = dimVector.getElementAsInt(1);
      Vector dimNames = (Vector) this.vector.getAttribute(Symbols.DIMNAMES);
      if (dimNames instanceof ListVector && dimNames.length() == 2) {
        rowNames = dimNames.getElementAsSEXP(0);
        colNames = dimNames.getElementAsSEXP(1);
      }

      matrix = true;
    }

    if (this.argName != null && this.argName.length() > 0) {
      this.computedName = this.argName;
    } else if (this.deparseLevel == 1 && this.expression instanceof Symbol) {
      this.computedName = this.expression.asString();
    } else if (this.deparseLevel == 2) {
      this.computedName = Deparse.deparse(context, this.expression, 0, false, 0, 0);
      if (this.computedName.length() > 10) {
        this.computedName = this.computedName.substring(0,10) + "...";
      }
    } else {
      this.computedName = "";
    }

  }

  public Vector getClasses () {
    return (Vector) vector.getAttribute(Symbols.CLASS);
  }

  public String getArgName() {
    return this.argName;
  }

  public String getName() {
    return this.computedName;
  }

  public boolean hasNoName() {
    return this.computedName.isEmpty();
  }

  public Promise repromise() {
    return new Promise(this.expression, (SEXP) this.vector);
  }

  public Vector getVector() {
    return this.vector;
  }

  public SEXP getExpression() {
    return this.expression;
  }

  public int getRows() {
    return this.rows;
  }

  public int getCols() {
    return this.cols;
  }

  public AtomicVector getRowNames() {
    return this.rowNames;
  }

  public AtomicVector getColNames() {
    return this.colNames;
  }

  public boolean isMatrix() {
    return this.matrix;
  }

  public SEXP getUneval() {
    return this.uneval;
  }

  public boolean isZeroLength() {
    return vector.length() == 0;
  }

  public boolean isZeroLengthVector() {
    return !matrix && vector.length() == 0;
  }
  
  public boolean isNull() {
    return vector == Null.INSTANCE;
  }
}
