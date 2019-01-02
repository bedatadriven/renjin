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
package org.renjin.gcc;

import org.renjin.gcc.codegen.CodeGenerationContext;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.GlobalVarTransformer;
import org.renjin.repackaged.asm.MethodVisitor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class GimpleCompilerPlugin {

  /**
   *
   * @return a list of transformers to apply to global variables.
   */
  @Nonnull
  public List<GlobalVarTransformer> createGlobalVarTransformers() {
    return Collections.emptyList();
  }

  /**
   * Override to write additional classfiles during the compilation process.
   */
  public void writeClasses(CodeGenerationContext generationContext) throws IOException {
  }

  public void writeTrampolinePrelude(MethodVisitor mv, FunctionGenerator functionGenerator) {
  }
}
