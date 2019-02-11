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
package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GimpleCompilationUnit {
  private File sourceFile;
  private String mainInputFilename;

  private final List<GimpleFunction> functions = Lists.newArrayList();
  private final List<GimpleRecordTypeDef> recordTypes = Lists.newArrayList();
  private final List<GimpleVarDecl> globalVariables = Lists.newArrayList();
  private final List<GimpleAlias> aliases = new ArrayList<>();
  


  /**
   * 
   * @return the name of the compilation unit, stripped of all extensions
   */
  public String getName() {
    if(sourceFile == null) {
      throw new IllegalStateException("sourceFile property is null");
    }

    String filename = sourceFile.getName();

    // First strip off the ".gimple" extension
    if(!filename.endsWith(".gimple")) {
      throw new IllegalStateException("Expected file name ending in .gimple");
    }
    filename = filename.substring(0, filename.length() - ".gimple".length());

    // Now remove the filetype extension like .c or .cc
    int firstDot = filename.indexOf('.');
    if(firstDot != -1) {
      filename = filename.substring(0, firstDot);
    }
    return filename;
  }

  public String getSourceName() {
    int nameStart = mainInputFilename.lastIndexOf('/');
    return mainInputFilename.substring(nameStart+1);
  }

  public String getMainInputFilename() {
    return mainInputFilename;
  }

  public void setMainInputFilename(String mainInputFilename) {
    this.mainInputFilename = mainInputFilename;
  }

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  public List<GimpleRecordTypeDef> getRecordTypes() {
    return recordTypes;
  }

  public List<GimpleVarDecl> getGlobalVariables() {
    return globalVariables;
  }
  
  public Iterable<GimpleDecl> getDeclarations() {
    return Iterables.concat(functions, globalVariables);
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public List<GimpleAlias> getAliases() {
    return aliases;
  }

  @Override
  public String toString() {
    return "GimpleCompilationUnit{" + getSourceName() + "}";
  }

  public void accept(GimpleExprVisitor visitor) {
    for (GimpleVarDecl globalVariable : globalVariables) {
      globalVariable.accept(visitor);
    }
    for (GimpleFunction function : functions) {
      function.accept(visitor);
    }
  }

  public GimpleFunction getFunction(String mangledName) {
    for (GimpleFunction function : functions) {
      if(function.getMangledName().equals(mangledName)) {
        return function;
      }
    }
    throw new IllegalArgumentException("No such function: " + mangledName);
  }
}
