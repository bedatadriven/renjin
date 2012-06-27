package org.renjin.primitives.models;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.matrix.DoubleMatrixBuilder;
import org.renjin.primitives.models.ModelFrame.Variable;
import org.renjin.primitives.models.TermsObject.Term;
import org.renjin.sexp.*;

import java.util.Arrays;

/**
 * Constructs a model matrix from a {@link TermsObject} and a
 * {@link ModelFrame}
 */
public class ModelMatrixBuilder {

  private Context context;

  private TermsObject terms;
  private ModelFrame modelFrame;

  private int nc;

  private int[] count;

  private DoubleMatrixBuilder matrix;


  public ModelMatrixBuilder(Context context, SEXP termsObject, ListVector frame) {
    this.context = context;
    this.terms = new TermsObject(termsObject);
    this.modelFrame = new ModelFrame(frame);


    computeNumColumns();
    computeMatrix();

    matrix.setColNames(columnNames());
    matrix.setRowNames(modelFrame.getRowNames());

  }

  public DoubleVector build() {
    return matrix.build();
  }

  private void adjustForMissingIntercept() {
    /* If there is no intercept we look through the factor pattern */
    /* matrix and adjust the code for the first factor found so that */
    /* it will be coded by dummy variables rather than contrasts. */

    //    if (!intrcept) {
    //        for (j = 0; j < nterms; j++) {
    //            for (i = risponse; i < nVar; i++) {
    //                if (INTEGER(nlevs)[i] > 1
    //                    && INTEGER(factors)[i + j * nVar] > 0) {
    //                    INTEGER(factors)[i + j * nVar] = 2;
    //                    goto alldone;
    //                }
    //            }
    //        }
    //    }
  }

  private void computeContrasts() {


    /* Compute the required contrast or dummy variable matrices. */
    /* We set up a symbolic expression to evaluate these, substituting */
    /* the required arguments at call time.  The calls have the following */
    /* form: (contrast.type nlevs contrasts) */


    //    PROTECT(contr1 = allocVector(VECSXP, nVar));
    //    PROTECT(contr2 = allocVector(VECSXP, nVar));
    //
    //    FunctionCall expr = new 
    //    
    //    
    //    PROTECT(expr = allocList(3));
    //    SET_TYPEOF(expr, LANGSXP);
    //    SETCAR(expr, install("contrasts"));
    //    SETCADDR(expr, allocVector(LGLSXP, 1));
    //
    //    /* FIXME: We need to allow a third argument to this function */
    //    /* which allows us to specify contrasts directly.  That argument */
    //    /* would be used here in exactly the same way as the below. */
    //    /* I.e. we would search the list of constrast specs before */
    //    /* we try the evaluation below. */
    //
    //    for (i = 0; i < nVar; i++) {
    //        if (INTEGER(nlevs)[i]) {
    //            k = 0;
    //            for (j = 0; j < nterms; j++) {
    //                if (INTEGER(factors)[i + j * nVar] == 1)
    //                    k |= 1;
    //                else if (INTEGER(factors)[i + j * nVar] == 2)
    //                    k |= 2;
    //            }
    //            SETCADR(expr, VECTOR_ELT(variable, i));
    //            if (k & 1) {
    //                LOGICAL(CADDR(expr))[0] = 1;
    //                SET_VECTOR_ELT(contr1, i, eval(expr, rho));
    //            }
    //            if (k & 2) {
    //                LOGICAL(CADDR(expr))[0] = 0;
    //                SET_VECTOR_ELT(contr2, i, eval(expr, rho));
    //            }
    //        }
    //    }

  }

  private void computeNumColumns() {
    /* The first step is to compute the matrix size and to allocate it. */
    /* Note that "count" holds a count of how many columns there are */
    /* for each term in the model and "nc" gives the total column count. */


    this.count = new int[terms.getNumTerms()];
    int dnc;
    double dk;
    if(terms.hasIntercept()) {
      dnc = 1;
    } else {
      dnc = 0;
    }

    for(Term term : terms.getTerms()) {
      if(!term.isResponse()) {

        dk = 1; /* accumulate in a double to detect overflow */
        for(Integer variableIndex : term.variableIndexes()) {
          Variable variable = modelFrame.getVariable(variableIndex);
          if(variable.isFactor()) {
            throw new UnsupportedOperationException("factors are not yet implemented"); 
          } else {
            dk *= variable.getNumColumns();
          } 
        }
        if (dk > Integer.MAX_VALUE) {
          throw new EvalException("term %d would require %.0g columns");
        }
        this.count[term.getTermIndex()] = (int)dk;
        dnc = dnc + (int)dk;
      }
    }

    this.nc = dnc;
  }

  private void linkColumnsToTerms() {
    /* Record which columns of the design matrix are associated */
    /* with which model terms. */

    IntArrayVector.Builder assign = new IntArrayVector.Builder(nc);
    int k = 0;
    if( terms.hasIntercept()) {
      assign.add(0);
    }
    for(int j=0;j<terms.getNumTerms();++j) {
      if(count[j] <= 0) {
        // warn
      }
      for(int i=0;i<count[j];++j) {
        assign.add(j+1);
      }
    }

  }

  /**
   *  Create column labels for the matrix columns. 
   */
  private StringVector columnNames() {
    StringVector.Builder xnames = new StringVector.Builder();


    /* Here we loop over the terms in the model and, within each */
    /* term, loop over the corresponding columns of the design */
    /* matrix, assembling the names. */

    /* FIXME : The body within these two loops should be embedded */
    /* in its own function. */

    int k = 0;
    if (terms.hasIntercept()) {
      xnames.add("(Intercept)");
    }
    for(Term term : terms.getTerms()) {
      if(!term.isResponse()) {

        for (int kk = 0; kk < count[term.getTermIndex()]; kk++) {
          int indx = kk;
          StringBuilder bufp = new StringBuilder();
          int colsInTerm;
          boolean first = true;
          for(Integer variableIndex : term.variableIndexes()) {
            Variable var = modelFrame.getVariable(variableIndex);
            if (!first) {
              bufp.append(":");
            } else {
              first = false;
            }
            if(var.isFactor() || var.isLogical()) {
              Vector x = Null.INSTANCE;
              throw new UnsupportedOperationException("factors nyi");
              //                 if(term.getContrastType(variableIndex) == 1) {
              //                   x = ColumnNames(VECTOR_ELT(contr1, i));
              //                   colsInTerm = ncols(VECTOR_ELT(contr1, i));
              //                 } else {
              //
              //                   x = ColumnNames(VECTOR_ELT(contr2, i));
              //                   colsInTerm = ncols(VECTOR_ELT(contr2, i));
              //                 }

              //                 bufp.append(var.getName());

              //                 if (x == Null.INSTANCE) {
              //                   bufp.append(indx % colsInTerm + 1);
              //                 } else {
              //                   bufp.append(x.getElementAsString(indx % colsInTerm));
              //                 }
            } else if (var.isComplex()) {
              throw new EvalException("complex variables are not currently allowed in model matrices");
            } else if(var.isNumeric()) {
              //Vector x = ColumnNames(var.getVector());
              //int colsInTerm = ncols(var.getVector());
              Vector x = Null.INSTANCE;
              colsInTerm = 1;
              bufp.append(var.getName());

              if (colsInTerm > 1) {
                if (x == Null.INSTANCE) {
                  bufp.append(indx % colsInTerm + 1);
                } else {
                  bufp.append(indx % colsInTerm);
                }
              }
            } else {
              throw new EvalException("variables of type '%s' are not allowed in model matrices",
                  var.getVector().getTypeName());
            }
            indx /= colsInTerm;
          }
          xnames.add(bufp.toString());
        }
      }
    }
    return xnames.build();
  }

  private void computeMatrix() {

    matrix = new DoubleMatrixBuilder(
        modelFrame.getNumRows(), nc);


    /* a) Begin with a column of 1s for the intercept. */

    int columnIndex = 0;
    if(terms.hasIntercept()) {
      for (int i = 0; i < matrix.getRows(); i++) {
        matrix.set(i, 0, 1);
      }
      columnIndex++;
    }

    /* b) Now loop over the model terms */

    SEXP contrast;
    for (Term k : terms.getTerms()) {
      if(!k.isResponse()) {

        double [] column = new double[modelFrame.getNumRows()];
        Arrays.fill(column, 1.0);

        for(Integer variableId : k.variableIndexes()) {
          for(int row=0;row!=column.length;++row) {
            column[row] *= modelFrame.getVariable(variableId).getVector().getElementAsDouble(row);
          }
        }

        for(int row=0;row!=column.length;++row) {
          matrix.set(row, columnIndex, column[row]);
        }

        columnIndex++;
      }
    } 
  }
}
