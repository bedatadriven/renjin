package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.util.List;

public class GimpleCompilationUnit {

  private final List<GimpleFunction> functions = Lists.newArrayList();
  private final List<GimpleRecordTypeDef> recordTypes = Lists.newArrayList();
  private final List<GimpleVarDecl> globalVariables = Lists.newArrayList();
  private File sourceFile;

  /**
   * 
   * @return the name of the compilation unit, stripped of all extensions
   */
  public String getName() {
    if(sourceFile == null) {
      throw new IllegalStateException("sourceFile property is null");
    }
    String filename = sourceFile.getName();
    int firstDot = filename.indexOf('.');
    if(firstDot == -1) {
      throw new IllegalStateException("Expected file name ending in .xx.gimple");
    }
    return filename.substring(0, firstDot);
  }

  /**
   * 
   * @return the original source file name, for example "cmatrix.c"
   */
  public String getSourceName() {
    Preconditions.checkState(sourceFile.getName().endsWith(".gimple"), "Source file must end in .gimple");
    return sourceFile.getName().substring(0, sourceFile.getName().length() - ".gimple".length());
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

  @Override
  public String toString() {
    return Joiner.on("\n").join(functions);
  }

}
