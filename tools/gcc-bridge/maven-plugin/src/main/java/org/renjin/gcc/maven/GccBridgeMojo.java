package org.renjin.gcc.maven;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.cpp.CppSymbolLibrary;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Compiles Fortran and C sources
 */
@Mojo(name = "compile",  requiresDependencyCollection = ResolutionScope.COMPILE)
public class GccBridgeMojo extends AbstractMojo {
  
  @Parameter( defaultValue = "src/main/fortran")
  private File fortranSourceDirectory;
  
  @Parameter( defaultValue = "src/main/c")
  private File cSourceDirectory;
  
  @Parameter( defaultValue = "${project.build.outputDirectory}")
  private File outputDirectory;
  
  @Parameter(required = true, defaultValue = "${project.groupId}")
  private String packageName;
  
  @Parameter(required = true, defaultValue = "${project.artifactId}")
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

  public void execute() throws MojoExecutionException {

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
    
    if(includeDirectories != null) {
      for (File includeDirectory : includeDirectories) {
        gcc.addIncludeDirectory(includeDirectory);
      }
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

      CallingConvention callingConvention = CallingConventions.fromFile(sourceFile);
      for (GimpleFunction function : unit.getFunctions()) {
        function.setCallingConvention(callingConvention);
      }
      units.add(unit);
    }
    return units;
  }

  protected SourceInclusionScanner getSourceInclusionScanner( String... inputFileEndings )
  {
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
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.addMathLibrary();
    compiler.setPackageName(packageName);
    compiler.setClassName(mainClass);
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.setOutputDirectory(outputDirectory);
    
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
}
