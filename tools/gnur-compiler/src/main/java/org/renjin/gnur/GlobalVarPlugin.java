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
package org.renjin.gnur;

import org.renjin.gcc.GimpleCompilerPlugin;
import org.renjin.gcc.codegen.CodeGenerationContext;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.GlobalVarTransformer;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Rewrites global variables in native C, C++, and Fortran code to use a session-scoped
 * copy of global variables so that a package can be used concurrently by multiple Renjin
 * sessions in the same JVM/process.
 *
 * <p>This is done in several steps:
 *
 * <p>Our {@link GlobalVarRewriter} replaces read/writes to mutable global variables with calls
 * to static getter/setter methods.
 *
 * <p>For each global variable, we declare an <strong>instance</strong> field on a new "Context" class,
 *  such as org.renjin.grDevices.Context.
 *
 * <p>We implement these getter/setters using a static {@link ThreadLocal} field that holds a reference
 *  to a Context class instance.
 *
 * <p>We augment the trampoline methods by initializing our ThreadLocal to the package's session-level
 * state object held by the corresponding {@link org.renjin.primitives.packaging.Namespace} instance.</p>
 *
 */
public class GlobalVarPlugin extends GimpleCompilerPlugin {


  private final Type contextClass;

  private GlobalVarRewriter globalVarRewriter;


  public GlobalVarPlugin(String packageName) {
    this.contextClass = Type.getType("L" + packageName.replace('.', '/') + "/Context;");
    this.globalVarRewriter = new GlobalVarRewriter(contextClass);
  }

  @Nonnull
  @Override
  public List<GlobalVarTransformer> createGlobalVarTransformers() {
    return Collections.singletonList(globalVarRewriter);
  }

  @Override
  public void writeTrampolinePrelude(MethodVisitor mv, FunctionGenerator functionGenerator) {
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, contextClass.getInternalName(), "enter",
        ContextClassWriter.ENTER_METHOD_DESCRIPTOR, false);
    mv.visitInsn(Opcodes.POP);
  }

  @Override
  public void writeClasses(CodeGenerationContext generationContext) throws IOException {
    ContextClassWriter writer = new ContextClassWriter(contextClass);
    writer.writeFields(globalVarRewriter.getContextFields());
    writer.writeConstructor(generationContext,
        globalVarRewriter.getContextFields(),
        globalVarRewriter.getContextVars());

    writer.writeTo(generationContext);
  }
}
