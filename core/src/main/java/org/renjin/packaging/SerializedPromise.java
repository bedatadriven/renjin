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
    try(RDataReader reader = new RDataReader(context, resourceProvider.apply(resourceName(name)))) {
      return reader.readFile();
    } catch (IOException e) {
      throw new EvalException(e);
    }
  }

  /**
   * Composes a file name for a serialized symbol.
   * 
   * In general, this {symbolName}.RData, but there is a very special case where symbols beginning with "._" will
   * be excluded from the JAR file by the maven-jar-plugin, with no apparent way of overriding.
   * 
   * @param symbolName the name of the symbol to serialize
   * @return the resource name
   */
  public static String resourceName(String symbolName) {
    // maven-jar-plugin ultimately relies on org.codehaus.plexus.util.AbstractScanner
    // which has a default exclude pattern of "._*"
    // https://github.com/sonatype/plexus-utils/blob/aa6739dc2871e01d6d0ca4564a3a66bcf044c84a/src/main/java/org/codehaus/plexus/util/AbstractScanner.java#L53
    if(symbolName.startsWith("._")) {
      return "$$" + symbolName + ".RData";
    } else {
      return symbolName + ".RData";
    }
  }
}
