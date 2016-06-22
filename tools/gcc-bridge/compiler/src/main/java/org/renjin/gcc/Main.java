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
