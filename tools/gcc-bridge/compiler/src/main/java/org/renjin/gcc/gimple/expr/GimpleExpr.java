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
package org.renjin.gcc.gimple.expr;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleType;
import java.util.function.Predicate;

import java.util.List;

/**
 * A Gimple Expression node. 
 *
 * @see <a href="https://gcc.gnu.org/onlinedocs/gccint/Expression-trees.html#Expression-trees">Expression trees</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "code")
@JsonSubTypes({
    @Type(value = GimpleMemRef.class, name = "mem_ref"),
    @Type(value = GimpleVariableRef.class, name = "var_decl"),
    @Type(value = GimpleFieldRef.class, name = "field_decl"),
    @Type(value = GimpleParamRef.class, name = "parm_decl"),
    @Type(value = GimpleArrayRef.class, name = "array_ref"),
    @Type(value = GimpleObjectTypeRef.class, name = "obj_type_ref"),
    @Type(value = GimpleAddressOf.class, name = "addr_expr"),
    @Type(value = GimpleIntegerConstant.class, name = "integer_cst"),
    @Type(value = GimpleRealConstant.class, name = "real_cst"),
    @Type(value = GimpleStringConstant.class, name = "string_cst"),
    @Type(value = GimpleFunctionRef.class, name = "function_decl"),
    @Type(value = GimpleConstantRef.class, name = "const_decl"),
    @Type(value = GimpleComponentRef.class, name = "component_ref"),
    @Type(value = GimpleConstructor.class, name = "constructor"),
    @Type(value = GimpleRealPartExpr.class, name = "realpart_expr"),
    @Type(value = GimpleImPartExpr.class, name = "imagpart_expr"),
    @Type(value = GimpleComplexConstant.class, name = "complex_cst"),
    @Type(value = GimpleResultDecl.class, name = "result_decl"),
    @Type(value = GimpleNopExpr.class, name = "nop_expr"),
    @Type(value = GimpleSsaName.class, name = "ssa_name"),
    @Type(value = GimpleBitFieldRefExpr.class, name = "bit_field_ref"),
    @Type(value = GimpleCompoundLiteral.class, name = "compound_literal_expr"),
    @Type(value = GimplePointerPlus.class, name = "pointer_plus_expr"),
    @Type(value = GimpleTreeList.class, name = "tree_list")
    })
public abstract class GimpleExpr {

  private Integer line;

  private GimpleType type;


  public final void setLine(Integer line) {
    this.line = line;
  }

  public final Integer getLine() {
    return line;
  }

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
  }
  
  public final void findOrDescend(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(this, predicate, results);
  }
  
  protected final void findOrDescend(GimpleExpr child, Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    if(predicate.test(child)) {
      results.add(child);
    } else {
      child.find(predicate, results);
    }
  }
  


  protected final void findOrDescend(Iterable<GimpleExpr> children, Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    for (GimpleExpr child : children) {
      findOrDescend(child, predicate, results);
    }
  }

  public abstract void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr);

  protected final GimpleExpr replaceOrDescend(GimpleExpr child, Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.test(child)) {
      return newExpr;
    } else {
      child.replaceAll(predicate, newExpr);
      return child;
    }
  }
  
  public abstract void accept(GimpleExprVisitor visitor);

}
