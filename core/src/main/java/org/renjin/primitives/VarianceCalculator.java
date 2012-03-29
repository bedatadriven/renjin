package org.renjin.primitives;

import java.util.BitSet;

import org.renjin.eval.EvalException;
import org.renjin.primitives.matrix.DoubleMatrixBuilder;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;


public class VarianceCalculator {

  public class VariableSet {
    private AtomicVector vector;
    private int variables; 
    private int observations;

    public VariableSet(AtomicVector vector) {
      this.vector = vector;
      Vector dim = (Vector) vector.getAttribute(Symbols.DIM);
      if(dim == Null.INSTANCE) {
        this.observations = vector.length();
        this.variables = 1;
      } else {
        if(dim.length() != 2) {
          throw new EvalException("must be vector or matrix, not higher-order array");
        }
        this.observations = dim.getElementAsInt(0);
        this.variables = dim.getElementAsInt(1);
      }    
    }
    
    public Variable getVariable(int i) {
      return new Variable(vector, i*observations, observations);
    }
    
    public boolean hasNA() {
      return vector.containsNA();
    }
  } 
  
  private class Variable {
    private Vector vector;
    private int start;
    private int observations;
    
    public Variable(Vector vector, int start, int observations) {
      super();
      this.vector = vector;
      this.start = start;
      this.observations = observations;
    }
    
    public final double get(int i) {
      return vector.getElementAsDouble(start+i);
    }
  }
  
  private interface Method {
    double calculate(Variable x, Variable y);
    double calculate(Variable x);
  }
  
  private class PearsonCorrelation implements Method {
    public double calculate(Variable x, Variable y) {
      double sum_xy = 0;
      double sum_x = 0;
      double sum_x2 = 0;
      double sum_y = 0;
      double sum_y2 = 0;
      double n = 0;
      
      for(int i=0;i!=x.observations;++i) {
        double x_i = x.get(i);
        double y_i = y.get(i);

        if(missingStrategy.use(x_i, y_i, i)) {
         
          sum_xy += (x_i * y_i);
          
          sum_x += (x_i);
          sum_x2 += (x_i * x_i);
          
          sum_y += (y_i);
          sum_y2 += (y_i * y_i);
          
          n += 1;
        }
      }
      return (sum_xy - ((sum_x*sum_y)/n)) / 
          Math.sqrt(sum_x2 - ((sum_x*sum_x) / n) ) /
          Math.sqrt(sum_y2 - ((sum_y*sum_y) / n) ); 
    }

    @Override
    public double calculate(Variable x) {
      return 1.0;
    }
  }
  
  private class SampleCovariance implements Method {

    @Override
    public double calculate(Variable x, Variable y) {
      
      // first pass, calculate means
      double sum_x = 0;
      double sum_y = 0;
      double n = 0;
      
      for(int i=0;i!=x.observations;++i) {
        double x_i = x.get(i);
        double y_i = y.get(i);
        if(missingStrategy.use(x_i, y_i, i)) {
          sum_x += x_i;
          sum_y += y_i;
          n++;
        }
      }
      
      double mean_x = sum_x / n;
      double mean_y = sum_y / n;
      
      // second pass, calculate sum of the products of the deviates
      double sum_deviates = 0;
      for(int i=0;i!=x.observations;++i) {
        double x_i = x.get(i);
        double y_i = y.get(i);
        if(missingStrategy.use(x_i, y_i, i)) {
          sum_deviates += (x_i-mean_x)*(y_i-mean_y);
        }
      }
      
      return sum_deviates / (n - 1d);
    }

    @Override
    public double calculate(Variable x) {
      return calculate(x, x);
    }
    
  }
  
  private interface MissingStrategy {
    boolean use(double x, double y, int observationIndex);
  }
  
  /**
   * the presence of missing observations
   *  will produce an error.
   *
   */
  private final class AllObs implements MissingStrategy {

    public AllObs() {
      if(x.hasNA() || (y!= null && y.hasNA())) {
        throw new EvalException("missing observation in cov/cor");
        
      }
    }
    
    @Override
    public boolean use(double x, double y, int observationIndex) {
      // already checked
      return true;
    }
    
  }
  
  private final class CompleteObs implements MissingStrategy {
    
    @Override
    public boolean use(double x, double y, int observationIndex) {
      throw new UnsupportedOperationException("nyi");
    }
    
  }
  
  private final class PairwiseCompleteObs implements MissingStrategy {

    @Override
    public boolean use(double x, double y, int observationIndex) {
      return !DoubleVector.isNA(x) && !DoubleVector.isNA(y);
    }
    
  }
  
  /**
   * NA’s will propagate conceptually,
   *  i.e., a resulting value will be ‘NA’ whenever one of its
   *  contributing observations is ‘NA’.
   *
   */
  private final class Everything implements MissingStrategy {

    @Override
    public boolean use(double x, double y, int observationIndex) {
      return true;
    }
    
  }
  
  private final class NaOrComplete implements MissingStrategy {

    private BitSet incomplete;
    
    public NaOrComplete(VariableSet x, VariableSet y) {
      incomplete = new BitSet(x.observations);
      markMissing(x);
      markMissing(y);
    }
    
    private void markMissing(VariableSet x) {
      if(x != null) {
        for(int i=0;i!=x.variables;++i) {
          Variable variable = x.getVariable(i);
          for(int j=0;j!=x.observations;++j) {
            if(DoubleVector.isNA(variable.get(j))) {
              incomplete.set(j, false);
            }
          }
        }
      }
    }
    
    @Override
    public boolean use(double x, double y, int observationIndex) {
      return !incomplete.get(observationIndex);
    }
  }
  
  private VariableSet x;
  private VariableSet y;
  private DoubleMatrixBuilder result;
  private Method method;
  private MissingStrategy missingStrategy;
  
  public VarianceCalculator(AtomicVector x, AtomicVector y, int missingStrategy) {
    this.x = new VariableSet(x);
    
    if(y == Null.INSTANCE) {
      this.y = null;
    } else {
      this.y = new VariableSet(y);
      if(this.x.observations != this.y.observations) {
        throw new EvalException("dimensions not compatible");
      }
    }
    this.missingStrategy = createMissingStrategy(missingStrategy);
  }
  

  public VarianceCalculator withCovarianceMethod() {
    this.method = new SampleCovariance();
    return this;
  }

  public VarianceCalculator withPearsonCorrelation() {
    this.method = new PearsonCorrelation();
    return this;
  }
  
  public DoubleVector calculate() {
    if(y == null) {
      return selfCalculate();
    } else {
      return crossCalculate();
    }
  }
  
  private DoubleVector selfCalculate() {
    result = new DoubleMatrixBuilder(this.x.variables, this.x.variables);
    int nVars = x.variables;
    for(int i=0;i!=nVars;++i) {
      result.set(i, i, method.calculate(x.getVariable(i)));
      for(int j=i+1;j<nVars;++j) {
        double value = method.calculate(x.getVariable(i), x.getVariable(j));
        result.setValue(i, j, value);
        result.setValue(j, i, value);
      }
    }  
    return result.build();
    
  }
  
  /**
   * Computes the cov/cor between the variables in x against the
   * variables in y.
   */
  private DoubleVector crossCalculate() {
    result = new DoubleMatrixBuilder(this.x.variables, this.y.variables);

    for(int i=0;i!=x.variables;++i) {
      for(int j=0;j!=y.variables;++j) {
        double value = method.calculate(x.getVariable(i), y.getVariable(j));
        result.setValue(i, j, value);
      }
    }  
    return result.build();
  }

  private MissingStrategy createMissingStrategy(int index) {
    switch(index) {
      case 1:
        return new AllObs();
      case 2:
        return new CompleteObs();
      case 3:
        return new PairwiseCompleteObs();
      case 4:
        return new Everything();
      case 5:
        return new NaOrComplete(x, y);
      default:
          throw new IllegalArgumentException("missingStrategy = " + index);
    }
  }

}
