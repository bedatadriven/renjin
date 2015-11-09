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
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

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

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${project.compileArtifacts}", readonly = true)
  private List<Artifact> compileDependencies;



  public void execute() throws MojoExecutionException {

    List<File> sourceFiles = new ArrayList<File>();
    
    if(fortranSourceDirectory.exists()) {
      File[] files = fortranSourceDirectory.listFiles();
      if(files != null) {
        for (File file : files) {
          if(isFortranSource(file)) {
            sourceFiles.add(file);
          }
        }
      }
    }
    
    if(cSourceDirectory.exists()) {
      File[] files = cSourceDirectory.listFiles();
      if(files != null) {
        for (File file : files) {
          if(isCSource(file)) {
            sourceFiles.add(file);
          }
        }
      }
    }
    
    List<GimpleCompilationUnit> units = compileToGimple(sourceFiles);
    
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

  private boolean isFortranSource(File file) {
    return file.getName().toLowerCase().endsWith(".f") || file.getName().toLowerCase().endsWith(".f77");
  }
  
  private boolean isCSource(File file) {
    return file.getName().toLowerCase().endsWith(".c");
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
