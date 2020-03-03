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

import org.renjin.sexp.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Transforms an input NAMESPACE file by qualifying, if possible, the package names that are referenced.
 */
public class NamespaceQualifier {

  private Map<String, String> groupMap;

  public NamespaceQualifier(Map<String, String> groupMap) {
    this.groupMap = groupMap;
  }

  public ExpressionVector qualify(ExpressionVector vector) {
    ExpressionVector.Builder result = new ExpressionVector.Builder();
    for (SEXP statement : vector) {
      if(statement instanceof FunctionCall) {
        result.add(qualifyStatement((FunctionCall)statement));
      } else {
        result.add(statement);
      }
    }
    return result.build();
  }

  private SEXP qualifyStatement(FunctionCall statement) {
    if(statement.getFunction() == Symbol.get("import") || statement.getFunction() == Symbol.get("importFrom")) {
      return qualifyImport(statement);
    } else {
      return statement;
    }
  }

  private SEXP qualifyImport(FunctionCall statement) {
    FunctionCall.Builder qualified = new FunctionCall.Builder();
    qualified.add(statement.getFunction());

    Iterator<PairList.Node> argIt = statement.getArguments().nodes().iterator();
    if(!argIt.hasNext()) {
      return statement;
    }
    qualified.add(qualifyPackage(argIt.next().getValue()));
    while(argIt.hasNext()) {
      PairList.Node node = argIt.next();
      qualified.add(node.getRawTag(), node.getValue());
    }
    return qualified.build();
  }

  private SEXP qualifyPackage(SEXP packageSexp) {
    if(packageSexp instanceof Symbol) {
      return qualifyPackage( ((Symbol) packageSexp).getPrintName() );

    } else if(packageSexp instanceof StringVector && packageSexp.length() == 1) {
      return qualifyPackage( ((StringVector) packageSexp).getElementAsString(0) );

    } else {
      // Malformed package name, abort
      return packageSexp;
    }
  }

  private StringVector qualifyPackage(String packageName) {
    if(!isQualified(packageName)) {
      if(groupMap.containsKey(packageName)) {
        packageName = groupMap.get(packageName) + ":" + packageName;
      }
    }
    return StringVector.valueOf(packageName);
  }

  private boolean isQualified(String packageName) {
    return packageName.contains(":");
  }

  public static ExpressionVector qualify(BuildContext buildContext, ExpressionVector vector) {
    return new NamespaceQualifier(buildContext.getPackageGroupMap()).qualify(vector);
  }

}
