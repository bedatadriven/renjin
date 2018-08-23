package org.renjin.stats.nls;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.matrix.DoubleMatrixBuilder;
import org.renjin.sexp.*;

import java.io.IOException;

/**
 * Implementation of GNU R NLS support routines in Java.
 */
public class NonlinearLeastSquares {
  
  private enum StopReason {
    CONVERGED,
    SINGULAR_GRADIENT,
    MIN_FACTOR_REACHED,
    MAX_ITERATIONS_EXCEEDED
  }

  /**
   * Fits a set of <i>m</i> observations with a model that is non-linear in <i>n</i>
   * unknown parameters (m > n).
   *
   * @param context the R evaluation expression
   * @param modelExpression an R nlsModel object
   * @param controlExpression an R nlsControl object
   * @param doTrace true if we should periodically trace the status of the iterations
   * @return
   * @throws IOException
   */
  @Internal
  public static SEXP iterate(@Current Context context, ListVector modelExpression, 
                             ListVector controlExpression, boolean doTrace) throws IOException {
    double newDev;
    int j;

    NlsModel model = new NlsModel(context, modelExpression);
    NlsControl control = new NlsControl(controlExpression);
   
    AtomicVector parameters = model.getParameterValues();
    int numParameters = parameters.length();

    double dev = model.calculateDeviation();
    if(doTrace) {
      model.trace();
    }

    double factor = 1.0;
    boolean hasConverged = false;

    double newParameters[] = new double[numParameters];

    int totalEvaluationCount = 1;

    int iterationNumber;
    double newConvergence = Double.POSITIVE_INFINITY;
    for (iterationNumber = 0; iterationNumber < control.getMaxIterations(); iterationNumber++) {
      int evaluationCount = -1;
      newConvergence = model.getConvergence();
      if (newConvergence < control.getTolerance()) {
        hasConverged = true;
        break;
      }
      DoubleVector newIncrement = model.calculateIncrements();

      evaluationCount = 1;

      while (factor >= control.getMinFactor()) {

        if (control.isPrintEval()) {
          context.getSession().getStdOut().printf("  It. %3d, fac= %11.6f, eval (no.,total): (%2d,%3d):\n",
                  iterationNumber + 1, factor, evaluationCount, totalEvaluationCount);
          evaluationCount++;
          totalEvaluationCount++;
        }

        for (j = 0; j < numParameters; j++) {
          newParameters[j] = parameters.getElementAsDouble(j) +
                  factor * newIncrement.getElementAsDouble(j);
        }
        
        if(model.updateParameters(newParameters)) {
          return convergenceResult(control,
                "singular gradient",
                iterationNumber,
                StopReason.SINGULAR_GRADIENT,
                newConvergence);
        }

        newDev = model.calculateDeviation();
        if (control.isPrintEval()) {
          context.getSession().getStdOut().printf(" new dev = %f\n", newDev);
        }

        if (newDev <= dev) {
          dev = newDev;
          factor = Math.min(2d * factor, 1d);
          parameters = new DoubleArrayVector(newParameters);
          break;
        }
        factor /= 2.;
      }

      if (factor < control.getMinFactor()) {
        return convergenceResult(control,
                String.format("step factor %f reduced below 'minFactor' of %f", factor, control.getMinFactor()),
                iterationNumber,
                StopReason.MIN_FACTOR_REACHED,
                newConvergence);
      }
      if (doTrace) {
        model.trace();
      }
    }

    if (!hasConverged) {
      return convergenceResult(control,
              String.format("number of iterations exceeded maximum of %d", control.getMaxIterations()),
              iterationNumber,
              StopReason.MAX_ITERATIONS_EXCEEDED,
              newConvergence);
    }

    return convergenceResult(control, "converged", iterationNumber, StopReason.CONVERGED, newConvergence);
  }


  private static SEXP convergenceResult(NlsControl control, String msg, int iterationNumber,
                                        StopReason stopReason,
                                        double newConvergence) {
    
    if(!control.isWarnOnly() && stopReason != StopReason.CONVERGED) {
      throw new EvalException(msg);
    }
    
    ListVector.NamedBuilder result = ListVector.newNamedBuilder();
    result.add("isConv", stopReason == StopReason.CONVERGED);
    result.add("finIter", iterationNumber);
    result.add("finTol", newConvergence);
    result.add("stopCode", stopReason.ordinal());
    result.add("stopMessage", msg);
    return result.build();
  }

  /**
   * Numerically computes the derivative of a non-linear model with 
   * respect to its parameters, specified in theta
   *
   * @param context  the R evaluation context
   * @param modelExpr the model function call
   * @param theta vector containing the names of parameters
   * @param rho the environment in which to evaluate the model
   * @param dir the direction 
   * @return
   */
  public static SEXP numericDerivative(@Current Context context,
                                       SEXP modelExpr,
                                       StringVector theta,
                                       Environment rho,
                                       DoubleVector dir) {

    if(dir.length() != theta.length()) {
      throw new EvalException("'dir' is not a numeric vector of the correct length");
    }

    SEXP responseExpr = context.evaluate(modelExpr, rho);
    if(!(responseExpr instanceof Vector)) {
      throw new EvalException("Expected numeric response from model");
    }
    Vector response = (Vector)responseExpr;
    for(int i=0;i!=response.length();++i) {
      if(!DoubleVector.isFinite(response.getElementAsDouble(i))) {
        throw new EvalException("Missing value or an infinity produced when evaluating the model");
      }
    }

    double parameterValues[][] = new double[theta.length()][];
    int totalParameterValueCount = 0;
    for (int i = 0; i < theta.length(); i++) {
      String parameterName = theta.getElementAsString(i);
      SEXP parameterValue = rho.findVariable(context, Symbol.get(parameterName));

      if (!(parameterValue instanceof AtomicVector)) {
        throw new EvalException("variable '%s' is not numeric", parameterName);
      }
      parameterValues[i] = ((AtomicVector) parameterValue).toDoubleArray();
      totalParameterValueCount += parameterValues[i].length;
    }

    double eps = Math.sqrt(DoubleVector.EPSILON);

    DoubleMatrixBuilder gradient = new DoubleMatrixBuilder(response.length(), totalParameterValueCount);
    int gradientColumn = 0;
    for (int parameterIndex = 0; parameterIndex < theta.length(); parameterIndex++) {

      double direction = dir.getElementAsDouble(parameterIndex);

      // Parameters can be multivariate, not just single values, so loop over each
      // element in this parameter
      for (int parameterValueIndex = 0; parameterValueIndex < parameterValues[parameterIndex].length;
           parameterValueIndex++) {

        // update this individual parameter value
        double startingParameterValue = parameterValues[parameterIndex][parameterValueIndex];
        double absoluteParameterValue = Math.abs(startingParameterValue);
        double delta = (absoluteParameterValue == 0) ? eps : absoluteParameterValue * eps;

        parameterValues[parameterIndex][parameterValueIndex] +=
                direction * delta;

        // update this parameter in the model's environment
        rho.setVariable(context, theta.getElementAsString(parameterIndex), new DoubleArrayVector(parameterValues[parameterIndex]));

        // compute the new response given this updated parameter
        DoubleVector responseDelta = (DoubleVector) context.evaluate(modelExpr, rho);

        for (int k = 0; k < response.length(); k++) {
          if (!DoubleVector.isFinite(responseDelta.getElementAsDouble(k))) {
            throw new EvalException("Missing value or an infinity produced when evaluating the model");
          }
          double difference = responseDelta.getElementAsDouble(k) - response.getElementAsDouble(k);
          double relativeDifference = difference / delta;
          gradient.set(k, gradientColumn, direction * relativeDifference);
        }

        // restore this parameter back to the starting value
        parameterValues[parameterIndex][parameterValueIndex] = startingParameterValue;

        gradientColumn++;
      }

      // Now that we're done manipulating this variable, restore it to its starting
      // value in the model's environment
      rho.setVariable(context, theta.getElementAsString(parameterIndex),
              new DoubleArrayVector(parameterValues[parameterIndex]));
    }

    return response.setAttribute(Symbol.get("gradient"), gradient.build());
  }

}
