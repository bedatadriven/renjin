/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import com.fasterxml.jackson.annotation.JsonSetter;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.*;

/**
 * Gimple Function Model
 */
public class GimpleFunction implements GimpleDecl {
  private int id;
  private String name;
  private String mangledName;
  private GimpleType returnType;
  private List<String> aliases = Lists.newArrayList();
  private GimpleCompilationUnit unit;
  private List<GimpleBasicBlock> basicBlocks = Lists.newArrayList();
  private List<GimpleParameter> parameters = Lists.newArrayList();
  private List<GimpleVarDecl> variableDeclarations = Lists.newArrayList();
  private boolean extern;
  private boolean weak;
  private boolean inline;
  
  public GimpleFunction() {

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

  public void setMangledName(String mangledName) {
    this.mangledName = mangledName;
  }

  public String getMangledName() {
    if(mangledName == null) {
      throw new IllegalStateException("Mangled name is null");
    }
    return mangledName;
  }
  
  public List<String> getMangledNames() {
    List<String> names = Lists.newArrayList();
    names.add(getMangledName());
    names.addAll(getAliases());
    return names;
  }

  public List<String> getAliases() {
    return aliases;
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

  public boolean isWeak() {
    return weak;
  }

  public void setWeak(boolean weak) {
    this.weak = weak;
  }

  public boolean isInline() {
    return inline;
  }

  public void setInline(boolean inline) {
    this.inline = inline;
  }

  public GimpleVarDecl addVarDecl(GimpleType type) {
    // find unused id
    Set<Integer> usedIds = usedIds();
    int newId = 1000;
    while(usedIds.contains(newId)) {
      newId++;
    }
    
    GimpleVarDecl decl = new GimpleVarDecl();
    decl.setId(id);
    decl.setType(type);
    variableDeclarations.add(decl);
    
    return decl;
  }
  
  private Set<Integer> usedIds() {
    Set<Integer> set = new HashSet<>();
    for (GimpleVarDecl variableDeclaration : variableDeclarations) {
      set.add(variableDeclaration.getId());
    }
    for (GimpleParameter parameter : parameters) {
      set.add(parameter.getId());
    }
    return set;
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

  @JsonSetter
  public void setBasicBlocks(List<GimpleBasicBlock> basicBlocks) {
    this.basicBlocks = basicBlocks;
  }
  
  public void setBasicBlocks(GimpleBasicBlock... blocks) {
    setBasicBlocks(Arrays.asList(blocks));
  }

  public void setParameters(List<GimpleParameter> parameters) {
    this.parameters = parameters;
  }

  public void accept(GimpleVisitor visitor) {
    for (GimpleBasicBlock bb : basicBlocks) {
      visitor.blockStart(bb);
      for (GimpleStatement ins : bb.getStatements()) {
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
    if(!mangledName.equals(name)) {
      sb.append(mangledName).append(": ");
    }
    sb.append(returnType);
    sb.append(" ");
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

  public GimpleType getReturnType() {
    return returnType;
  }
  
  public void setReturnType(GimpleType returnType) {
    this.returnType = returnType;
  }


  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    for (GimpleBasicBlock basicBlock : basicBlocks) {
      for (GimpleStatement ins : basicBlock.getStatements()) {
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

  public void removeVariable(GimpleVariableRef ref) {
    Iterator<GimpleVarDecl> it = variableDeclarations.iterator();
    while(it.hasNext()) {
      if(it.next().getId() == ref.getId()) {
        it.remove();
        return;
      }
    }
    throw new InternalCompilerException("No such variable: " + ref);
  }

  public GimpleBasicBlock getLastBasicBlock() {
    return basicBlocks.get(basicBlocks.size()-1);
  }
  
  public void accept(GimpleExprVisitor visitor) {
    for (GimpleBasicBlock basicBlock : basicBlocks) {
      basicBlock.accept(visitor);
    }
  }
}
