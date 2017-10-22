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
package org.renjin.gcc.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.HtmlTreeLogger;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.cpp.CppSymbolLibrary;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Compiles Fortran and C sources
 */
@ThreadSafe
@Mojo(name = "compile",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class GccBridgeMojo extends AbstractMojo {
  
  @Parameter( defaultValue = "src/main/fortran")
  private File fortranSourceDirectory;
  
  @Parameter( defaultValue = "src/main/c")
  private File cSourceDirectory;
  
  @Parameter( defaultValue = "${project.build.outputDirectory}")
  private File outputDirectory;

  @Parameter(defaultValue = "false")
  private boolean generateJavadoc;

  @Parameter (defaultValue = "${project.build.directory}/gcc-bridge-javadoc")
  private File javadocOutputDirectory;
  
  @Parameter(required = true, defaultValue = "${project.groupId}")
  private String packageName;
  
  @Parameter
  private String mainClass;
  
  @Parameter(defaultValue = "${project.build.directory}/gimple")
  private File gimpleOutputDirectory;
  
  @Parameter
  private List<String> importClasses;

  @Parameter
  private List<String> symbolLibraries;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${project.compileArtifacts}", readonly = true)
  private List<Artifact> compileDependencies;

  @Parameter
  private List<File> sourceFiles;
  
  @Parameter
  private List<File> includeDirectories;

  @Parameter(defaultValue = "${project.build.directory}/include", readonly = true)
  private File unpackedIncludeDir;
  
  @Parameter
  private List<String> cFlags;

  @Parameter(defaultValue = "true")
  private boolean pruneUnusedSymbols = true;


  public void execute() throws MojoExecutionException {

    GccBridgeHelper.unpackHeaders(getLog(), unpackedIncludeDir, project.getCompileArtifacts());

    List<GimpleCompilationUnit> units;

    if(this.sourceFiles == null) {
      List<File> sourceFiles = new ArrayList<File>();

      SourceInclusionScanner scanner = getSourceInclusionScanner( ".f",".f77" );
      
      if (fortranSourceDirectory.exists()) {
        try {
          sourceFiles.addAll( scanner.getIncludedSources( fortranSourceDirectory, null ) );
        } catch ( InclusionScanException e ) {
          throw new MojoExecutionException(
              "Error scanning source root: \'" + fortranSourceDirectory + "\' for stale files to recompile.", e );
        }
      }

      scanner = getSourceInclusionScanner( ".c", ".cpp" );

      if (cSourceDirectory.exists()) {
        try {
          sourceFiles.addAll( scanner.getIncludedSources( cSourceDirectory, null ) );
        } catch ( InclusionScanException e ) {
          throw new MojoExecutionException(
            "Error scanning source root: \'" + cSourceDirectory + "\' for stale files to recompile.", e );
        }
      }
      units = compileToGimple(sourceFiles);
    } else {
      units = compileToGimple(this.sourceFiles);
    }
    
    compile(units);
  }

  private List<GimpleCompilationUnit> compileToGimple(List<File> sourceFiles) throws MojoExecutionException {
    
    File workingDir = new File("target/gcc-work");
    workingDir.mkdirs();

    Gcc gcc = new Gcc(workingDir);
    if(Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      try {
        gcc.extractPlugin();
      } catch (IOException e) {
        throw new MojoExecutionException("Could not extract GCC Plugin", e);
      }
    } else {
      gcc.setPluginLibrary(new File(System.getProperty("gcc.bridge.plugin")));
    }
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(gimpleOutputDirectory);

    if(cFlags != null) {
      gcc.addCFlags(cFlags);
    }
    
    
    if(includeDirectories != null) {
      for (File includeDirectory : includeDirectories) {
        gcc.addIncludeDirectory(includeDirectory);
      }
    }
    if(unpackedIncludeDir.exists()) {
      gcc.addIncludeDirectory(unpackedIncludeDir);
    }

    List<GimpleCompilationUnit> units = Lists.newArrayList();

    for (File sourceFile : sourceFiles) {
      getLog().debug("Compiling " + sourceFile.getName() + "...");
      GimpleCompilationUnit unit;
      try {
        unit = gcc.compileToGimple(sourceFile);
      } catch (IOException e) {
        throw new MojoExecutionException("Exception compiling " + sourceFile.getName() + ".", e);
      }
      units.add(unit);
    }
    return units;
  }

  protected SourceInclusionScanner getSourceInclusionScanner( String... inputFileEndings ) {
    Set<String> includes = new LinkedHashSet<String>();
    for(String inputFileEnding : inputFileEndings) {
      // it's not defined if we get the ending with or without the dot '.'
      String defaultIncludePattern = "**/*" + ( inputFileEnding.startsWith( "." ) ? "" : "." ) + inputFileEnding;
      includes.add(defaultIncludePattern);
    }
    SourceInclusionScanner scanner = new SimpleSourceInclusionScanner( includes, Collections.<String>emptySet() );
    for(String inputFileEnding : inputFileEndings) {
      scanner.addSourceMapping(getSourceMapping(inputFileEnding));
    }
    return scanner;
  }

  protected SourceMapping getSourceMapping(String inputFileEnding) {
    return new SuffixMapping( inputFileEnding, ".o" );
  }

  private void compile(List<GimpleCompilationUnit> units) throws MojoExecutionException {
    File logDir = new File("target/gcc-bridge-logs");
    logDir.mkdirs();

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.addMathLibrary();
    compiler.setPackageName(packageName);
    compiler.setClassName(mainClass);
    compiler.setVerbose(true);
    compiler.setPruneUnusedSymbols(pruneUnusedSymbols);
    compiler.addMathLibrary();
    compiler.setOutputDirectory(outputDirectory);
    compiler.setLinkClassLoader(getLinkClassLoader());
    compiler.setLogger(new HtmlTreeLogger(logDir));
    
    ClassLoader classLoader = createClassLoader();
    
    if(importClasses != null) {
      for (String importClass : importClasses) {
        Class<?> importedClass = null;
        try {
          importedClass = classLoader.loadClass(importClass);
        } catch (ClassNotFoundException e) {
          throw new MojoExecutionException("Could not load imported class " + importClass, e);
        }
        compiler.addReferenceClass(importedClass);
      }
    }
    
    if(symbolLibraries != null) {
      for(String lib : symbolLibraries) {
        if("cpp".equals(lib)) {
          compiler.addLibrary(new CppSymbolLibrary());
        } else {
          Class<?> libClass = null;
          try {
            libClass = classLoader.loadClass(lib);
            compiler.addLibrary((SymbolLibrary) libClass.newInstance());
          } catch (ClassCastException e) {
            throw new MojoExecutionException(lib + " is not a symbol library", e);
          } catch (ReflectiveOperationException e) {
            throw new MojoExecutionException("Could not load symbol library " + lib, e);
          }
        }
      }
    }

    if(generateJavadoc) {
      compiler.setJavadocOutputDirectory(javadocOutputDirectory);
    }
    
    try {
      compiler.compile(units);
    } catch (Exception e) {
      throw new MojoExecutionException("Exception compile to class files", e);
    }
  }


  private ClassLoader createClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("GCC Bridge Compile Classpath: ");

      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());

      for(Object artifactObject : compileDependencies) {
        Artifact artifact = (Artifact) artifactObject;
        getLog().debug("  "  + artifact.getFile());

        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      return new URLClassLoader( classpathURLs.toArray( new URL[ classpathURLs.size() ] ) );
    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }

  private ClassLoader getLinkClassLoader() throws MojoExecutionException  {
    try {
      getLog().debug("GCC-Bridge Link Classpath: ");

      List<URL> classpathURLs = Lists.newArrayList();
      classpathURLs.add( new File(project.getBuild().getOutputDirectory()).toURI().toURL() );

      for(Artifact artifact : (List<Artifact>)project.getCompileArtifacts()) {
        getLog().debug("  "  + artifact.getFile());

        classpathURLs.add(artifact.getFile().toURI().toURL());
      }

      return new URLClassLoader( classpathURLs.toArray( new URL[ classpathURLs.size() ] ), getClass().getClassLoader());
    } catch(MalformedURLException e) {
      throw new MojoExecutionException("Exception resolving classpath", e);
    }
  }
}
