package org.renjin.packaging;


import com.google.common.base.Function;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStream;

public class SerializedPromise extends Promise {

  private Function<String, InputStream> resourceProvider;
  private String name;

  public SerializedPromise(Function<String, InputStream> resourceProvider, String name) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.resourceProvider = resourceProvider;
    this.name = name;
  }

  @Override
  protected SEXP doEval(Context context) {
    try {
      RDataReader reader = new RDataReader(context, resourceProvider.apply(name + ".RData"));
      return reader.readFile();
    } catch (IOException e) {
      throw new EvalException(e);
    }
  }
}
