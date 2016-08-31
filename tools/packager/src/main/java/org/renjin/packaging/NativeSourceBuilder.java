package org.renjin.packaging;

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gnur.GnurSourcesCompiler;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.LineProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builds the native library part of a GNU R package, using {@code make} and any
 * provided {@code Makevars} file.
 */
public class NativeSourceBuilder {

  private static final List<String> SOURCE_EXTENSIONS = Lists.newArrayList("c", "f", "f77", "cpp", "cxx");

  private PackageSource source;
  private BuildContext buildContext;

  private List<String> entryPoints;


  public NativeSourceBuilder(PackageSource source, BuildContext buildContext) {
    this.source = source;
    this.buildContext = buildContext;
  }

  public void build() throws IOException, InterruptedException {

    make();
    compileGimple();
    buildContext.getLogger().info("Compilation of GNU R sources succeeded.");
//    archiveHeaders();
  }

  private void make() throws IOException, InterruptedException {

    File makeconfFile = new File(buildContext.getGnuRHomeDir().getAbsolutePath() + "/etc/Makeconf");
    File shlibMk = new File(buildContext.getGnuRHomeDir().getAbsolutePath() + "/share/make/shlib.mk");

    List<String> commandLine = Lists.newArrayList();
    commandLine.add("make");

    // Combine R's default Makefile with package-specific Makevars if present
    File makevars = new File(source.getNativeSourceDir(), "Makevars");
    if (makevars.exists()) {
      commandLine.add("-f");
      commandLine.add("Makevars");
    }

    // Makeconf file
    commandLine.add("-f");
    commandLine.add(makeconfFile.getAbsolutePath());

    commandLine.add("-f");
    commandLine.add(shlibMk.getAbsolutePath());

    commandLine.add("SHLIB='dummy.so'");

    if(!objectsDefinedByMakeVars(makevars)) {
      commandLine.add("OBJECTS=" + findObjectFiles());
    }
    commandLine.add("BRIDGE_PLUGIN=" + buildContext.getGccBridgePlugin().getAbsolutePath());

    buildContext.getLogger().debug("Executing " + Joiner.on(" ").join(commandLine));

    // Setup process
    ProcessBuilder builder = new ProcessBuilder()
        .command(commandLine)
        .directory(source.getNativeSourceDir())
        .inheritIO();

    builder.environment().put("R_VERSION", "3.2.0");
    builder.environment().put("R_HOME", buildContext.getGnuRHomeDir().getAbsolutePath());
    builder.environment().put("R_INCLUDE_DIR", buildContext.getGnuRHomeDir().getAbsolutePath() + "/include");
    builder.environment().put("R_SHARE_DIR", buildContext.getGnuRHomeDir().getAbsolutePath() + "/share");

    builder.environment().put("R_PACKAGE_NAME", source.getPackageName());
    builder.environment().put("R_INSTALL_PACKAGE", source.getPackageName());
    builder.environment().put("R_PACKAGE_DIR", buildContext.getPackageOutputDir().getAbsolutePath());

    builder.environment().put("CLINK_CPPFLAGS", "-I\"" + buildContext.getUnpackedIncludesDir().getAbsolutePath() + "\"");

    // Provide sensible defaults for locations of system tools
    if(!builder.environment().containsKey("MAKE")) {
      builder.environment().put("MAKE", "make");
    }

    if(!builder.environment().containsKey("R_UNZIPCMD")) {
      builder.environment().put("R_UNZIPCMD", "/usr/bin/unzip");
    }

    if(!builder.environment().containsKey("R_GZIPCMD")) {
      builder.environment().put("R_GZIPCMD", "/usr/bin/gzip");
    }

    int exitCode = builder.start().waitFor();
    if (exitCode != 0) {
      throw new InternalCompilerException("Failed to execute Makefile");
    }
  }

  private void compileGimple() {

    List<GimpleCompilationUnit> gimpleFiles = Lists.newArrayList();
    collectGimple(source.getNativeSourceDir(), gimpleFiles);

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setLinkClassLoader(buildContext.getClassLoader());
    compiler.setOutputDirectory(buildContext.getOutputDir());
    compiler.setPackageName(source.getGroupId() + "." + source.getPackageName());
    compiler.setClassName(source.getPackageName());

    if(entryPoints != null && !entryPoints.isEmpty()) {
      compiler.setEntryPointPredicate(new Predicate<GimpleFunction>() {
        @Override
        public boolean apply(GimpleFunction input) {
          return entryPoints.contains(input.getMangledName());
        }
      });
    }

    try {
      GnurSourcesCompiler.setupCompiler(compiler);
    } catch (ClassNotFoundException e) {
      throw new BuildException("Failed to setup Gimple Compiler", e);
    }

    try {
      compiler.compile(gimpleFiles);
    } catch (Exception e) {
      throw new BuildException("Failed to compile Gimple", e);
    }
  }

//  private void archiveHeaders() throws MojoExecutionException {
//    File instDir = new File(project.getBasedir(), "inst");
//    File includeDir = new File(instDir, "include");
//
//    // Some packages copy or create headers here as part of the 
//    // build process
//    File stagingIncludes = new File(stagingDir, "include");
//
//    if(includeDir.exists() || stagingIncludes.exists()) {
//      GccBridgeHelper.archiveHeaders(log, project, includeDir, stagingIncludes);
//    }
//  }


  private boolean objectsDefinedByMakeVars(File makevars) throws IOException {
    if(!makevars.exists()) {
      return false;
    }

    final Pattern definitionRegexp = Pattern.compile("^OBJECTS\\s*=");

    return Files.readLines(makevars, Charsets.UTF_8, new LineProcessor<Boolean>() {

      private boolean defined = false;

      @Override
      public boolean processLine(String line) throws IOException {
        if(definitionRegexp.matcher(line).find()) {
          defined = true;
          return false;
        }
        // Keep processing
        return true;
      }

      @Override
      public Boolean getResult() {
        return defined;
      }
    });
  }

  private String findObjectFiles() {
    List<String> objectFiles = new ArrayList<>();
    File[] files = source.getNativeSourceDir().listFiles();
    if(files != null)  {
      for (File file : files) {
        String extension = Files.getFileExtension(file.getName());
        if(SOURCE_EXTENSIONS.contains(extension)) {
          String baseName = Files.getNameWithoutExtension(file.getName());
          objectFiles.add(baseName + ".o");
        }
      }
    }
    return Joiner.on(" ").join(objectFiles);
  }

  private void collectGimple(File dir, List<GimpleCompilationUnit> gimpleFiles) {

    GimpleParser parser = new GimpleParser();

    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().endsWith(".gimple")) {
          buildContext.getLogger().debug("Reading " + 
              file.getAbsolutePath().substring(source.getNativeSourceDir().getAbsolutePath().length()));
          try {
            gimpleFiles.add(parser.parse(file));
          } catch (IOException e) {
            throw new BuildException("Failed to parse gimple file " + file, e);
          }
        } else if(file.isDirectory()) {
          collectGimple(file, gimpleFiles);
        }
      }
    }
  }

}
