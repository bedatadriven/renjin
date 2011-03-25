/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import r.lang.Context;
import r.lang.Environment;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Compiles the base package
 */
public class BaseCompiler {

  private static final Logger logger = Logger.getLogger(BaseCompiler.class.getName());

  private Set<String> excluded = new HashSet<String>();

  /**
   * Called by maven to build the Base Package class
   *
   * @param args
   */
  public static void main(String [] args) throws IOException {

    BaseCompiler compiler = new BaseCompiler();
    compiler.compile(new File(args[0]), args[1]);

  }

  public BaseCompiler() {
    excluded.add("zdynvars.R");
    excluded.add("zdatetime.R");
    excluded.add("zzz.R");
  }

  void compile(File sourceFolder, String outputFolder) throws IOException {

    Context context = Context.newTopLevelContext();
    Environment rho = context.getEnvironment();

    // parse and evaluate all files into a temporary environment
    for(File source : sourceFolder.listFiles()) {
      if(source.getName().endsWith(".R") && !excluded.contains(source.getName())) {
        System.out.println("Evaluating " + source.getPath());
        SEXP script = RParser.parseSource(new FileReader(source));
        script.evaluate(context, rho);
      }
    }

    for(Symbol name : rho.getSymbolNames()) {
      System.out.println("Symbol '" + name.getPrintName() + "' (" +
          rho.getVariable(name).getClass().getSimpleName() + ")");
    }

    // Define a subclass of Base
    ClassNode classNode = new ClassNode();
    classNode.name = "r/base/CompiledBaseFrame";
    classNode.superName = "r/base/BaseFrame";
    classNode.access = Opcodes.ACC_PUBLIC;


    MethodNode loaderMethod = new MethodNode();
    loaderMethod.access = Opcodes.ACC_PROTECTED;
    loaderMethod.name = "installLoaded";
    loaderMethod.desc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {});


    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    classNode.accept(writer);

    FileOutputStream fos = new FileOutputStream(outputFolder + "/r/base/CompiledBaseFrame.class");
    fos.write(writer.toByteArray());
    fos.close();



  }

}
