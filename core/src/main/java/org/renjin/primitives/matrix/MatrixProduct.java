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
package org.renjin.primitives.matrix;

import com.github.fommil.netlib.BLAS;
import org.renjin.eval.EvalException;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.sexp.*;


class MatrixProduct {

  public static final int PROD = 0;
  public static final int CROSSPROD = 1;
  public static final int TCROSSPROD = 2;

  private static final int ROWS = 0;
  private static final int COLS = 1;

  private int operation;
  private boolean symmetrical;

  private AtomicVector x;
  private AtomicVector y;

  private int nrx=0;
  private int ncx=0;
  private int nry=0;
  private int ncy=0;
  private int ldx;
  private int ldy;

  private Vector[] operands;

  public MatrixProduct(int operation, AtomicVector x, AtomicVector y) {
    super();
    this.x = x;
    this.y = y;

    symmetrical = (y == Null.INSTANCE);
    if (symmetrical && (operation > 0)) {
      this.y = x;
    }

    this.operation = operation;

    if (symmetrical) {
      operands = new Vector[] { x };
    } else {
      operands = new Vector[] { x, y };
    }

    computeMatrixDims();
  }

  private void computeMatrixDims() {

    Vector xdims = x.getAttributes().getDim();
    Vector ydims = y.getAttributes().getDim();
    ldx = xdims.length();
    ldy = ydims.length();

    if (ldx != 2 && ldy != 2) {

      /* x and y non-matrices */

      if (operation == PROD) {

        nrx = 1;
        ncx = x.length();

      } else {
        nrx = x.length();
        ncx = 1;
      }

      nry = y.length();
      ncy = 1;

    } else if (ldx != 2) {

      /* x not a matrix */

      nry = ydims.getElementAsInt(0);
      ncy = ydims.getElementAsInt(1);
      nrx = 0;
      ncx = 0;

      switch (operation) {
        case PROD:
          if (x.length() == nry) {
            /* x as row vector */
            nrx = 1;
            ncx = nry; /* == x.length() */
          }
          else if (nry == 1) {
            /* x as col vector */
            nrx = x.length();
            ncx = 1;
          }
          break;

        case CROSSPROD:

          if (x.length() == nry) {
          /* x is a col vector */
            nrx = nry; /* == x.length() */
            ncx = 1;
          }
          break;

        case TCROSSPROD:
          if (x.length() == ncy) {
            /* x as row vector */
            nrx = 1;
            ncx = ncy; /* == x.length() */

          } else if (ncy == 1) {
            /* x as col vector */
            nrx = x.length();
            ncx = 1;
          }
          break;
      }
    } else if (ldy != 2) {

      /* y not a matrix */

      nrx = xdims.getElementAsInt(0);
      ncx = xdims.getElementAsInt(1);
      nry = 0;
      ncy = 0;

      switch (operation) {
        case PROD:
          if (y.length() == ncx) {
            /* y as col vector */
            nry = ncx;
            ncy = 1;

          } else if (ncx == 1) {
            /* y as row vector */
            nry = 1;
            ncy = y.length();
          }
          break;

        case CROSSPROD:
          if (y.length() == nrx) {
            /* y is a col vector */
            nry = nrx;
            ncy = 1;
          }
          break;

        case TCROSSPROD:
          nry = y.length();
          ncy = 1;
          break;
      }
    } else {

      /* x and y matrices */

      nrx = xdims.getElementAsInt(ROWS);
      ncx = xdims.getElementAsInt(COLS);
      nry = ydims.getElementAsInt(ROWS);
      ncy = ydims.getElementAsInt(COLS);
    }

    if (((operation == 0) && (ncx != nry)) ||
        ((operation == 1) && (nrx != nry)) ||
        ((operation == 2) && (ncx != ncy)) ) {
      throw new EvalException("non-conformable arguments");
    }
  }

  public String getName() {
    switch (operation) {
      default:
      case PROD:
        return "%*%";
      case CROSSPROD:
        return "crossprod";
      case TCROSSPROD:
        return "tcrossprod";
    }
  }

  public Vector[] getOperands() {
    return operands;
  }

  public boolean isNonZero() {
    return (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0);
  }

  public int computeLength() {
    switch (operation) {
      default:
      case PROD:
        return nrx*ncy;
      case CROSSPROD:
        return ncx * ncy;
      case TCROSSPROD:
        return nrx * nry;
    }
  }

  public AttributeMap computeAttributes() {
    AttributeMap.Builder attributes = new AttributeMap.Builder();

    switch (operation) {
      case PROD:
        attributes.setDim(nrx, ncy);
        attributes.setDimNames(computeDimensionNames(ROWS, COLS));
        break;

      case CROSSPROD:
        attributes.setDim(ncx, ncy);
        attributes.setDimNames(computeDimensionNames(COLS, COLS));
        break;

      case TCROSSPROD:
        attributes.setDim(nrx, nry);
        attributes.setDimNames(computeDimensionNames(ROWS, ROWS));
        break;
    }
    return attributes.build();
  }

  public Vector compute() {

    if(!isNonZero()) {
      return (Vector)RepDoubleVector.createConstantVector(0, computeLength())
          .setAttributes(computeAttributes());
    }

    if(x.isDeferred() || y.isDeferred() || computeLength() > 500) {
      return new DeferredMatrixProduct(this);
    }

    return DoubleArrayVector.unsafe(computeResult(), computeAttributes());
  }

  private Vector computeDimensionNames(int rowNamesDim, int colNamesDim) {
    Vector xdims = x.getAttributes().getDimNames();
    Vector ydims = y.getAttributes().getDimNames();

    ListVector.NamedBuilder dimNames = new ListVector.NamedBuilder(2);
    dimNames.set(ROWS, Null.INSTANCE);
    dimNames.set(COLS, Null.INSTANCE);

    boolean hasNames = false;

    if(xdims != Null.INSTANCE) {
      if (ldx == 2) {
        SEXP rowNames = xdims.getElementAsSEXP(rowNamesDim);
        if(rowNames != Null.INSTANCE) {
          hasNames = true;
        }
        if(rowNames != Null.INSTANCE || xdims.hasNames()) {
          dimNames.set(ROWS, rowNames);
          dimNames.setName(ROWS, xdims.getName(rowNamesDim));
        }
      }
    }
    if(ydims != Null.INSTANCE) {
      if (ldy == 2) {
        SEXP rowNames = ydims.getElementAsSEXP(colNamesDim);
        if(rowNames != Null.INSTANCE) {
          hasNames = true;
        }
        if(rowNames != Null.INSTANCE || ydims.hasNames()) {
          dimNames.set(COLS, rowNames);
          dimNames.setName(COLS, ydims.getName(colNamesDim));
        }
      }
    }

    if(hasNames) {
      return dimNames.build();
    } else {
      return Null.INSTANCE;
    }
  }

  private double[] getXArray() {
    return x.toDoubleArray();
  }

  private double[] getYArray() {
    return y.toDoubleArray();
  }

  Vector computeResultVector(AttributeMap attributes) {
    return DoubleArrayVector.unsafe(computeResult(), attributes);
  }

  double[] computeResult() {
    switch (operation) {
      case CROSSPROD:
        if(symmetrical) {
          return computeSymmetricalCrossProduct();
        } else {
          return computeCrossProduct();
        }
      case TCROSSPROD:
        if(symmetrical) {
          return computeTransposeSymmetricalCrossProduct();
        } else {
          return computeTransposeCrossProduct();
        }
      default:
      case PROD:
        return computeMatrixProduct();
    }
  }

  private double[] computeMatrixProduct() {
    String transa = "N";
    String transb = "N";
    int i,  j, k;
    double one = 1.0, zero = 0.0;
    double sum;
    boolean haveNA = false;

    double x[] = getXArray();
    double y[] = getYArray();
    double z[] = new double[nrx*ncy];

    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {

      /* Don't trust the BLAS to handle NA/NaNs correctly: PR#4582
       * The test is only O(n) here
       */
      for (i = 0; i < nrx*ncx; i++) {
        if (Double.isNaN(x[i])) {
          haveNA = true;
          break;
        }
      }

      if (!haveNA) {
        for (i = 0; i < nry*ncy; i++) {
          if (Double.isNaN(y[i])) {
            haveNA = true;
            break;
          }
        }
      }

      if (haveNA) {
        for (i = 0; i < nrx; i++) {
          for (k = 0; k < ncy; k++) {
            sum = 0.0;
            for (j = 0; j < ncx; j++) {
              sum +=  x[i + j * nrx] * y[j + k * nry];
            }
            z[i + k * nrx] = sum;
          }
        }
      } else {
        BLAS.getInstance().dgemm(transa, transb, nrx, ncy, ncx, one,
            x, nrx, y, nry, zero, z, nrx);
      }
    }
    return z;
  }


  private double[] computeCrossProduct() {
    double[] x = this.getXArray();
    double[] y = this.getYArray();
    double[] z = new double[ncx * ncy];

    String transa = "T";
    String transb = "N";
    double one = 1.0, zero = 0.0;
    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {
      BLAS.getInstance().dgemm(transa, transb, ncx, ncy, nrx, one,
          x, nrx, y, nry, zero, z, ncx);
    }

    return z;
  }

  private double[] computeTransposeCrossProduct() {
    double[] x = getXArray();
    double[] y = getYArray();
    double[] z = new double[nrx * nry];

    String transa = "N";
    String transb = "T";
    double one = 1.0, zero = 0.0;
    if (nrx > 0 && ncx > 0 && nry > 0 && ncy > 0) {
      BLAS.getInstance().dgemm(transa, transb, nrx, nry, ncx, one,
          x, nrx, y, nry, zero, z, nrx);
    }
    return z;
  }


  private double[] computeSymmetricalCrossProduct() {
    String trans = "T";
    String uplo = "U";
    double one = 1.0, zero = 0.0;

    double[] x = this.getXArray();
    double[] z = new double[ncx * ncy];

    int i, j;
    if (nrx > 0 && ncx > 0) {
      BLAS.getInstance().dsyrk(uplo, trans, ncx, nrx, one, x, nrx, zero, z, ncx);

      for (i = 1; i < ncx; i++) {
        for (j = 0; j < i; j++) {
          z[i + ncx * j] = z[j + ncx * i];
        }
      }
    }
    return z;
  }

  private double[] computeTransposeSymmetricalCrossProduct() {

    double[] x = this.getXArray();
    double[] z = new double[nrx * nry];

    String trans = "N";
    String uplo = "U";
    double one = 1.0, zero = 0.0;
    int i, j;
    if (nrx > 0 && ncx > 0) {
      BLAS.getInstance().dsyrk(uplo, trans, nrx, ncx, one, x, nrx, zero, z, nrx);
      for (i = 1; i < nrx; i++) {
        for (j = 0; j < i; j++) {
          z[i + nrx *j] = z[j + nrx * i];
        }
      }
    }
    return z;
  }

}
