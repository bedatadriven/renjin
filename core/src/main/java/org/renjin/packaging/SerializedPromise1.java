package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


public class SerializedPromise1 extends Promise {

  private byte[] bytes;

  public SerializedPromise1(byte[] bytes) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.bytes = bytes;
  }

  @Override
  protected SEXP doEval(Context context) {
    try(RDataReader reader = new RDataReader(context,
          new GZIPInputStream(
              new ByteArrayInputStream(bytes)))) {
      return reader.readFile();
    } catch (IOException e) {
      throw new EvalException(e);
    }
  }
}
