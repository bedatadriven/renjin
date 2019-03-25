/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.aot;

import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

/**
 * Compiles a closure to a java method
 */
public class ClosureCompiler {

  private Session session;
  private AotBuffer buffer = new AotBuffer("org.renjin.test");
  private final AotHandle handle;

  public ClosureCompiler(Session session, Closure closure) {
    this.session = session;
    RuntimeState runtimeState = new RuntimeState(session.getTopLevelContext(), session.getGlobalEnvironment(), rho -> false);
    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.build(closure.getBody(), false);
    System.out.println(body);

    ClosureEmitContext emitContext = new ClosureEmitContext(closure.getFormals());

    String methodDescriptor = Type.getMethodDescriptor(Type.getType(SEXP.class),
        Type.getType(Context.class),
        Type.getType(Environment.class),
        Type.getType("[Lorg/renjin/sexp/SEXP;"));


    handle = buffer.newFunction("test.R", "scale", methodDescriptor, mv -> {
      for (Statement statement : body.getStatements()) {
        statement.emit(emitContext, mv);
      }

      mv.visitMaxs(0, emitContext.getLocalVarAllocator().getCount());
      mv.visitEnd();
    });

  }

  public AotHandle getHandle() {
    return handle;
  }
}
