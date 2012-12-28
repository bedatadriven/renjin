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

package org.renjin.cli;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;

public class StandardConsole implements Console{

  private final Reader in;
  private final PrintStream out;
  private final PrintStream err;

  public StandardConsole() {
    this(new InputStreamReader(System.in),System.out,System.err);
  }
  
  public StandardConsole(Reader in,PrintStream out, PrintStream err){
    this.in = in;
    this.out = out;
    this.err = err;
  }

  @Override
  public Reader getIn() {
    return in;
  }

  @Override
  public PrintStream getOut() {
    return out;
  }

  @Override
  public PrintStream getErr() {
    return err;
  }

  @Override
  public void println(Object o) {
    out.println(o);
  }

  @Override
  public void print(Object o) {
    out.print(o);
  }

  @Override
  public void error(Object o) {
    err.println(o);
  }

  @Override
  public int getCharactersPerLine() {
    return 80;
  }

  public static void main(String[] args) throws FileSystemException {

    StandardConsole console = new StandardConsole();

    StandaloneContextFactory factory = new StandaloneContextFactory();
    Context topLevelContext = factory.create();
    topLevelContext.getSession().setStdOut(new PrintWriter(console.getOut()));
    topLevelContext.getSession().setSessionController(new CliSessionController(console));

    Interpreter interpreter = new Interpreter( console, topLevelContext );
    new Thread ( interpreter ).start();     
  }
}
