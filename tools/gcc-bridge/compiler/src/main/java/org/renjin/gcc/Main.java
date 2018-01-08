/**
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
package org.renjin.gcc;


import io.airlift.command.Cli;
import io.airlift.command.Help;
import io.airlift.command.ParseException;

/**
 * Main class for invoking the gcc bridge compiler from the command line
 */
public class Main {

  public static void main(String[] args) {


    Cli.CliBuilder<Runnable> builder = Cli.buildCli("gcc-bridge", Runnable.class)
            .withDescription("C/Fortran compiler targeting the JVM with help from GCC")
            .withDefaultCommand(CompileCommand.class)
            .withCommands(Help.class,
                    CompileCommand.class);

    Cli<Runnable> gitParser = builder.build();

    try {
      gitParser.parse(args).run();
    } catch(ParseException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch(GccException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }
}
