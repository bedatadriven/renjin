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
package org.renjin.packaging;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStream;

public class SerializedPromise3 extends Promise {

  private Function<String, InputStream> resourceProvider;
  private String resourceName;

  public SerializedPromise3(Function<String, InputStream> resourceProvider, String resourceName) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.resourceProvider = resourceProvider;
    this.resourceName = resourceName;
  }

  @Override
  protected SEXP doEval(Context context, boolean allowMissing) {
    try(RDataReader reader = new RDataReader(context, resourceProvider.apply(resourceName))) {
      return reader.readFile();
    } catch (IOException e) {
      throw new EvalException(e);
    }
  }
}
