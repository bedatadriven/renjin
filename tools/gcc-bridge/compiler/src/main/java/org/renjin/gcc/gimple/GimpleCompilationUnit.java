package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
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
  private String mainInputFilename;
  
  
  
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

  @Override
  public String toString() {
    return Joiner.on("\n").join(functions);
  }

  public void accept(GimpleExprVisitor visitor) {
    for (GimpleVarDecl globalVariable : globalVariables) {
      globalVariable.accept(visitor);
    }
    for (GimpleFunction function : functions) {
      function.accept(visitor);
    }
  }
}
