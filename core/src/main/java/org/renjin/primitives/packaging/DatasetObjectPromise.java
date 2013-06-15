package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

/**
 * A promise that loads a dataset object from the package
 * when forced.
 *
 */
public class DatasetObjectPromise extends Promise {

  private Dataset dataset;
  private String objectName;
  
  protected DatasetObjectPromise(Dataset dataset, String name) {
    super(null, Null.INSTANCE);
    this.dataset = dataset;
    this.objectName = name;
  }

  @Override
  protected SEXP doEval(Context context) {
    try {
      return dataset.loadObject(objectName);
    } catch (IOException e) {
      throw new EvalException("Exception loading '%s' from dataset '%s'", objectName, dataset.getName());
    }
  }
}
