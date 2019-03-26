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

import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.gimple.GimpleFunction;

/**
 * Thrown because of a problem or unfinished implementation in the 
 * GimpleCompiler
 */
public class InternalCompilerException extends RuntimeException {

  public InternalCompilerException() {
  }

  public InternalCompilerException(String message) {
    super(message);
  }

  public InternalCompilerException(String message, Throwable cause) {
    super(message, cause);
  }


  public InternalCompilerException(GimpleFunction function, Exception e) {
    super(String.format("Exception compiling function %s [%s] in unit %s", 
        function.getName(), 
        function.getMangledName(),
        function.getUnit().getSourceFile().getName()), e);
  }

  public InternalCompilerException(FunctionGenerator functionGenerator, Exception e) {
    this(functionGenerator.getFunction(), e);
  }

  public InternalCompilerException(Exception e) {
    super(e);
  }
}
