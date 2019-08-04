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
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

import java.io.IOException;

/**
 * A promise that loads a dataset object from the package
 * when forced.
 *
 */
public class DatasetObjectPromise extends Promise {

  private Dataset dataset;
  private String objectName;
  
  protected DatasetObjectPromise(Dataset dataset, String name) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.dataset = dataset;
    this.objectName = name;
  }

  @Override
  protected SEXP doEval(Context context) {
    try {
      return dataset.loadObject(context, objectName);
    } catch (IOException e) {
      throw new EvalException("Exception loading '%s' from dataset '%s'", objectName, dataset.getName());
    }
  }
}
