/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.embed;


import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.primitives.special.ForFunction;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.rosuda.JRI.RConsoleOutputStream;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXPReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class Renjin {

  private final Session session;

  private final Rengine rengine;
  private final Wrapper wrapper;
  private final FrameWrapper globalFrame;

  public Renjin() {
    rengine = Rengine.getMainEngine();
    try {
      globalFrame = new FrameWrapper(rengine, rengine.rniSpecialObject(Rengine.SO_GlobalEnv));
      session = new SessionBuilder()
          .setGlobalFrame(globalFrame)
          .withDefaultPackages()
          .build();

      wrapper = new Wrapper(session);
      globalFrame.setWrapper(wrapper);

      session.setStdOut(new PrintWriter(new RConsoleOutputStream(rengine, 0)));
      session.setStdErr(new PrintWriter(new RConsoleOutputStream(rengine, 1)));
    } catch (Exception e) {
      dumpStackTrace(e);
      throw e;
    }
  }

  private void dumpStackTrace(Exception e) {
    RConsoleOutputStream out = new RConsoleOutputStream(rengine, 0);
    PrintWriter pw = new PrintWriter(out);
    e.printStackTrace(pw);
    pw.flush();
  }

  public byte[] evalSerialized(byte[] data) throws IOException {
    RDataReader reader = new RDataReader(session.getTopLevelContext(), new ByteArrayInputStream(data));
    SEXP sexp = reader.readFile();

    Context context = session.getTopLevelContext();
    SEXP result = context.materialize(context.evaluate(sexp));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(session.getTopLevelContext(), out);
    writer.serialize(result);
    return out.toByteArray();
  }

  public void eval(REXPReference ref, REXPReference resultEnv, REXPReference environment, boolean compile) {


    ForFunction.COMPILE_LOOPS = compile;

    try {
      synchronized (rengine) {

        session.clearWarnings();
        wrapper.resetCache();
        globalFrame.clearCache();

        SEXP exp = wrapper.wrap(ref);

        // Create the read-only "host" environment, which will be used to look up symbols
        long hostEnvPtr = (Long)environment.getHandle();
        Environment hostEnv = Environment.createChildEnvironment(session.getGlobalEnvironment(),
            new HostFrame(rengine, wrapper, hostEnvPtr)).build();

        // Now create the evaluation environment
        Environment evalEnvironment = Environment.createChildEnvironment(hostEnv).build();

        SEXP result = session.getTopLevelContext().evaluate(exp, evalEnvironment);

        long resultPointer = wrapper.unwrap(result);
        rengine.rniAssign("result", resultPointer, (Long) resultEnv.getHandle());

        session.printWarnings();
        rengine.jriFlushConsole();

      }
    } catch (Exception e) {
      dumpStackTrace(e);
      throw e;
    } finally {
      wrapper.clear();
    }
  }

}
