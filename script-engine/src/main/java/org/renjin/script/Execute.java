/*
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
package org.renjin.script;

import org.renjin.eval.Context;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.sexp.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A java main class that will start a new Renjin session and call the "execute" function.
 *
 *
 */
public class Execute {

  public static void main(String[] args) throws IOException {


    String namespace = getNamespace();

    System.err.println("Namespace = " + namespace);

    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine scriptEngine = factory.getScriptEngine();

    Context context = scriptEngine.getTopLevelContext();
    Namespace executableNamespace = scriptEngine.getSession().getNamespaceRegistry().getNamespace(context, namespace);
    SEXP execute = executableNamespace.getNamespaceEnvironment().getVariable(context, Symbol.get("execute"));
    if(execute instanceof Closure) {
      Closure closure = (Closure) execute;
      FunctionCall call;
      try {
        call = new FunctionCall(execute, prepareArguments(closure, args));
      } catch (IllegalArgumentException e) {
        printHelpAndExit(closure, e.getMessage());
        throw new Error();
      }
      context.evaluate(call);
    }

  }

  private static PairList prepareArguments(Closure function, String[] args) {
    Iterator<String> argumentIterator = Arrays.asList(args).iterator();
    Iterator<PairList.Node> formalIterator = function.getFormals().nodes().iterator();

    PairList.Builder builder = new PairList.Builder();

    while(formalIterator.hasNext()) {
      PairList.Node formal = formalIterator.next();

      if(!argumentIterator.hasNext()) {
        throw new IllegalArgumentException("Not enough arguments");
      }

      builder.add(formal.getTag(), convertArgument(argumentIterator.next(), formal.getValue()));
    }

    return builder.build();

  }

  private static SEXP convertArgument(String argument, SEXP formalDefaultValue) {
    if(isNumber(formalDefaultValue)) {
      return DoubleVector.valueOf(Double.parseDouble(argument));
    } else {
      return StringVector.valueOf(argument);
    }
  }

  private static boolean isNumber(SEXP formalDefaultValue) {
    if(formalDefaultValue instanceof DoubleVector || formalDefaultValue instanceof IntVector) {
      return true;
    }
    if(formalDefaultValue instanceof FunctionCall) {
      SEXP function = ((FunctionCall) formalDefaultValue).getFunction();
      if(function instanceof Symbol) {
        if(((Symbol) function).getPrintName().startsWith("number")) {
          return true;
        }
      }
    }
    return false;
  }

  private static void printHelpAndExit(Closure function, String message) {
    System.err.println("ERROR: " + message);
    System.err.println(helpFromClosure(function));
    System.exit(-1);
  }

  private static String helpFromClosure(Closure function) {
    StringBuilder s = new StringBuilder();
    s.append("Usage: java -jar <jarfile>");
    for (PairList.Node formal : function.getFormals().nodes()) {
      s.append(" <");
      s.append(formal.getTag().getPrintName());
      s.append(">");
    }
    s.append('\n');
    s.append('\n');
    s.append("Arguments:\n");

    for (PairList.Node formal : function.getFormals().nodes()) {
      s.append("  ");
      s.append(Strings.padEnd(formal.getTag().getPrintName(), 20, ' '));
      s.append('\n');
    }

    return s.toString();

  }

  public static String getNamespace() throws IOException {
    URL namespaceResource;
    try {
      namespaceResource = Resources.getResource("META-INF/org.renjin.execute.namespace");
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Cannot locate META-INF/org.renjin.execute.namespace resource.");
    }
    return Resources.toString(namespaceResource, Charsets.UTF_8);
  }
}
