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
package org.renjin.gnur;

import org.renjin.gcc.codegen.CodeGenerationContext;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * "Allocates" global variables that need to be scoped to the
 * current Renjin session. This ensures that C code that uses global variables
 * can safely run concurrently in multiple R sessions in the same JVM/process.
 */
public class ContextVarAllocator {

  private final Type contextClass;
  private List<ContextField> fields = new ArrayList<>();
  private Multimap<GimpleVarDecl, ContextField> varFields = HashMultimap.create();

  public ContextVarAllocator(Type contextClass) {
    this.contextClass = contextClass;
  }


  public JLValue reserve(GimpleCompilationUnit unit, GimpleVarDecl decl, String name, Type type, Optional<JExpr> initialValue) {

    String varName = variableName(unit, name);
    ContextField var = new ContextField(contextClass, varName, type, initialValue);

    fields.add(var);
    varFields.put(decl, var);

    return var.jvalue();
  }

  public static String variableName(GimpleCompilationUnit unit, String name) {
    return VarAllocator.toJavaSafeName(unit.getName()) + "$" + VarAllocator.toJavaSafeName(name);
  }

  public VarAllocator forUnit(GimpleCompilationUnit unit, GimpleVarDecl decl) {
    return new VarAllocator() {
      @Override
      public JLValue reserve(String name, Type type) {
        return ContextVarAllocator.this.reserve(unit, decl, name, type, Optional.empty());
      }

      @Override
      public JLValue reserve(String name, Type type, JExpr initialValue) {
        return ContextVarAllocator.this.reserve(unit, decl, name, type, Optional.of(initialValue));
      }
    };
  }

  public List<ContextField> getContextFields() {
    return fields;
  }


  public void writeSymbols(CodeGenerationContext generationContext) throws IOException {
    for (GimpleVarDecl globalVar : varFields.keySet()) {
      if(globalVar.isPublic()) {
        Collection<ContextField> contextFields = varFields.get(globalVar);
        if(contextFields.size() == 1) {
          writeSymbol(generationContext, globalVar, contextFields.iterator().next());
        }
      }
    }
  }

  private void writeSymbol(CodeGenerationContext generationContext, GimpleVarDecl globalVar, ContextField field) throws IOException {
    String getterName = field.getGetterName();
    LinkSymbol linkSymbol = LinkSymbol.forGetter(contextClass, globalVar.getMangledName(), getterName, field.getType());

    generationContext.writeResourceFile(
        linkSymbol.getMetadataName(),
        linkSymbol.toProperties().getBytes(Charsets.UTF_8));
  }
}
