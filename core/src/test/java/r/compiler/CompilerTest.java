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

import org.junit.Test;
import r.compiler.runtime.Program;
import r.lang.*;

import java.io.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CompilerTest {

  @Test
  public void simpleTest() throws IOException {
    // open R source file from class path
    InputStream stream = getClass().getResourceAsStream("/simpleTest.R");
    InputStreamReader sourceReader = new InputStreamReader(stream);

    // Set up the compiler
    Compiler compiler = new Compiler();
    compiler.setClassOutputDir(new File(".").getCanonicalPath());
    compiler.setClassName("SimpleTest");
    compiler.addSource(sourceReader);

    // Generate the source file
    File source = compiler.writeSource();
    printSource(source);

    compiler.compile();
    compiler.load();
    Program program = compiler.load();

    GlobalContext context = new GlobalContext();
    EnvExp env = context.getGlobalEnvironment();
    program.evaluate(env);

    SymbolExp a = context.getSymbolTable().install("a");
    SymbolExp myfunc = context.getSymbolTable().install("myfunc");

    assertThat(env.findVariable(a), equalTo((SEXP)new RealExp(42)));

    ClosureExp myfuncClosureExp = (ClosureExp) env.findVariable(myfunc);
    assertThat(myfuncClosureExp, is(not(nullValue())));

  }

  private void printSource(File source) throws IOException {
    System.out.println("Source: " + source.getAbsolutePath());
    BufferedReader reader = new BufferedReader(new FileReader(source));
    String line;
    while( (line=reader.readLine()) != null) {
      System.out.println(line);
    }
  }

}
