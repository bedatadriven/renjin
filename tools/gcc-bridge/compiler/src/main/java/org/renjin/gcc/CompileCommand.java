/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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


import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Command line "compile" command
 */
@Command(name = "compile", description = "Compile C/Fortran files to a JVM class file")
public class CompileCommand implements Runnable {

  @Option(name = "-o", description = "Output directory for class files", required = true)
  public File outputDirectory;

  @Option(name = "--class-name", description = "The class name of the output class", required = true)
  public String className;

  @Option(name = "--package-name", description = "The package name of the output class", required = true)
  public String packageName;


  @Option(name = "--plugin-path", description = "Path to the gcc-bridge.so binary")
  public File pluginPath;

  @Option(name = "-v", description = "Verbose mode")
  public boolean verbose;

  @Option(name = "-I", description = "Add include directory for GCC")
  public List<String> includeDirs = Lists.newArrayList();

  @Option(name = "-d", description = "Compile all sources in the given directory")
  public List<String> directories = Lists.newArrayList();
  
  @Arguments(description = "Sources files to compile")
  public List<String> sourceFiles = Lists.newArrayList();
  
  

  @Override
  public void run() {

    List<GimpleCompilationUnit> units = compileToGimple();

    if(units.isEmpty()) {
      System.err.println("Nothing to compile");
      return;
    }
    
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(outputDirectory);
    compiler.setVerbose(verbose);
    compiler.setPackageName(packageName);
    compiler.setClassName(className);
    
    try {
      compiler.compile(units);
    } catch (Exception e) {
      throw new RuntimeException("Failed to translate or compile gimple", e);
    }
  }

  private List<GimpleCompilationUnit> compileToGimple() {

    List<GimpleCompilationUnit> units = Lists.newArrayList();

    try {
      Gcc gcc = new Gcc();

      gcc.checkVersion();
      
      if(pluginPath == null) {
        gcc.extractPlugin();
      } else {
        if(!pluginPath.exists()) {
          throw new GccException("Plugin binary not found at: " + pluginPath.getAbsolutePath());
        }
        gcc.setPluginLibrary(pluginPath);
      }
        
      for(String includeDir : includeDirs) {
        gcc.addIncludeDirectory(new File(includeDir));
      }

      for(String sourceFile : sourceFiles) {
        if(verbose) {
          System.out.println("Compiling " + sourceFile + " to gimple...");
        }
        units.add( gcc.compileToGimple(new File(sourceFile)) );
      }

      for(String dirName : directories) {
        if(verbose) {
          System.out.println("Looking for sources in " + dirName);
        }
        File dir = new File(dirName);
        if(dir.exists() && dir.listFiles() != null) {
          for(File file : dir.listFiles()) {
            if (file.getName().toLowerCase().endsWith(".f") ||
                file.getName().endsWith(".f77") ||
                file.getName().endsWith(".c")) {

              if(verbose) {
                System.out.println("Compiling " + file.getAbsolutePath() + " to gimple...");
              }
              units.add( gcc.compileToGimple(file) );
            }
          }
        }

      }
    } catch(GccException e) {
      System.err.println("GCC Compilation FAILED:");
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return units;

  }
}
