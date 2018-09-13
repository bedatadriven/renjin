/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.script;

import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.special.ForFunction;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Stopwatch;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbol;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MicrobenchmarkHarness {

  public static void main(String[] args) throws IOException {

    ForFunction.FAIL_ON_COMPILATION_ERROR = true;

//    File script = new File("/home/alex/dev/renjin-benchmarks/microbenchmarks/r_vs_cpp/leibniz/leibniz.R");
    File script = new File("/home/alex/dev/renjin-benchmarks/microbenchmarks/r_vs_cpp/dmvnorm/dmvnorm.R");
//    File script = new File("/home/alex/dev/renjin-benchmarks/microbenchmarks/r_vs_cpp/fuzzycluster/fuzzycluster.R");
//    File script = new File("/home/alex/dev/renjin-benchmarks/microbenchmarks/r_vs_cpp/jensen-shannon/jensen-shannon.R");

    ForFunction.COMPILE_LOOPS = true;
    ForFunction.COMPILE_LOOPS_VERBOSE = true;

    Session session = new SessionBuilder()
        .withDefaultPackages()
        .build();
    session.setWorkingDirectory(script.getParentFile());
    session.getTopLevelContext().evaluate(RParser.parseSource(Files.asCharSource(script, Charsets.UTF_8), Null.INSTANCE));

    FunctionCall call = FunctionCall.newCall(Symbol.get("run"));

    for (int i = 0; i < 5; i++) {
      session.getTopLevelContext().evaluate(call);
    }

    Stopwatch started = Stopwatch.createStarted();

    for (int i = 0; i < 10; i++) {
      session.getTopLevelContext().evaluate(call);
    }

    System.out.println(started.elapsed(TimeUnit.MILLISECONDS) / 100d);

    session.printWarnings();


  }
}
