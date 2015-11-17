package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.ins.GimpleIns;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public class GimpleFunction {
  private int id;
  private String name;
  private CallingConvention callingConvention;
  private GimpleType returnType;
  private GimpleCompilationUnit unit;
  private List<GimpleBasicBlock> basicBlocks = Lists.newArrayList();
  private List<GimpleParameter> parameters = Lists.newArrayList();
  private List<GimpleVarDecl> variableDeclarations = Lists.newArrayList();
  private boolean extern;

  public GimpleFunction() {

  }

  public void setCallingConvention(CallingConvention callingConvention) {
    this.callingConvention = callingConvention;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }


  public String getMangledName() {
    return callingConvention.mangleFunctionName(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<GimpleVarDecl> getVariableDeclarations() {
    return variableDeclarations;
  }

  public GimpleCompilationUnit getUnit() {
    return unit;
  }

  public void setUnit(GimpleCompilationUnit unit) {
    this.unit = unit;
  }

  public GimpleVarDecl addVarDecl(GimpleType type) {
    // find unused id
    int id = 1000;
    while(isIdInUse(id)) {
      id++;
    }
    
    GimpleVarDecl decl = new GimpleVarDecl();
    decl.setId(id);
    decl.setType(type);
    variableDeclarations.add(decl);
    
    return decl;
  }
  
  private boolean isIdInUse(int varDeclId) {
    for (GimpleVarDecl variableDeclaration : variableDeclarations) {
      if(variableDeclaration.getId() == varDeclId) {
        return true;
      }
    }
    for (GimpleParameter gimpleParameter : getParameters()) {
      if(gimpleParameter.getId() == varDeclId) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return true if this function has external linkage, that is, it is visible 
   * from outside of the compilation unit.
   */
  public boolean isExtern() {
    return extern;
  }

  public void setExtern(boolean extern) {
    this.extern = extern;
  }

  public List<GimpleParameter> getParameters() {
    return parameters;
  }

  public void setBasicBlocks(List<GimpleBasicBlock> basicBlocks) {
    this.basicBlocks = basicBlocks;
  }

  public void setParameters(List<GimpleParameter> parameters) {
    this.parameters = parameters;
  }

  public void visitIns(GimpleVisitor visitor) {
    for (GimpleBasicBlock bb : basicBlocks) {
      visitor.blockStart(bb);
      for (GimpleIns ins : bb.getInstructions()) {
        ins.visit(visitor);
      }
    }
  }


  public List<GimpleBasicBlock> getBasicBlocks() {
    return basicBlocks;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append(" (");
    Joiner.on(", ").appendTo(sb, parameters);
    sb.append(")\n");
    sb.append("{\n");
    for (GimpleVarDecl decl : variableDeclarations) {
      sb.append(decl).append("\n");
    }
    for (GimpleBasicBlock bb : basicBlocks) {
      sb.append(bb.toString());
    }
    sb.append("}\n");
    return sb.toString();
  }

  public CallingConvention getCallingConvention() {
    return callingConvention;
  }

  public GimpleType getReturnType() {
    return returnType;
  }
  
  public void setReturnType(GimpleType returnType) {
    this.returnType = returnType;
  }


  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    for (GimpleBasicBlock basicBlock : basicBlocks) {
      for (GimpleIns ins : basicBlock.getInstructions()) {
        if(ins.lhsMatches(predicate)) {
          return true;
        }
      }
    }
    return false;
  }

  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (GimpleBasicBlock basicBlock : basicBlocks) {
      basicBlock.replaceAll(predicate, newExpr);
    }  
  }
}
