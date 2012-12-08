package org.renjin.gcc;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Gcc {

	private GccEnvironment environment;
  private List<File> includeDirectories = Lists.newArrayList();

  private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

  public Gcc() {
    if(Strings.nullToEmpty(System.getProperty("os.name")).toLowerCase().contains("windows")) {
      environment = new CygwinEnvironment();
    } else {
      environment = new UnixEnvironment();
    }
  }
  
	public String compileToGimple(File source) throws IOException {
		
		List<String> arguments = Lists.newArrayList();
		arguments.add("-c"); // compile only, do not link
		arguments.add("-S"); // stop at assembly generation
//    command.add("-O9"); // highest optimization
		
		// Turn on dumping of gimple trees
		// See http://gcc.gnu.org/onlinedocs/gcc/Debugging-Options.html#Debugging-Options
		arguments.add("-fdump-tree-optimized");
		arguments.add("-fdump-tree-optimized-raw");
    arguments.add("-fdump-tree-ssa");
   // command.add("-fdump-tree-optimized-verbose");
   // command.add("-fdump-tree-optimized-lineno");

    for(File includeDir : includeDirectories) {
      arguments.add("-I");
      arguments.add(environment.toString(includeDir));
    }
	
		arguments.add(environment.toString(source));

    LOGGER.info("Executing " + Joiner.on(" ").join(arguments));

		
		Process gcc = environment.startGcc(arguments);
			
		try {
			gcc.waitFor();
		} catch (InterruptedException e) {
			throw new GccException("Compiler interrupted");
		}
    String stdout = new String(ByteStreams.toByteArray(gcc.getInputStream()));
    System.out.println(stdout);

    String stderr = new String(ByteStreams.toByteArray(gcc.getErrorStream()));

		if(gcc.exitValue() != 0) {
			throw new GccException("Compilation failed:\n" + stderr);
		} else {
      java.lang.System.err.println(stderr);
    }


		File gimple = findGimpleOutput(environment.getWorkingDirectory(), source);
		
		return Files.toString(gimple, Charsets.UTF_8);
	}

  private File findGimpleOutput(File dir, File source) throws FileNotFoundException {
    for(File file : dir.listFiles()) {
      if(file.getName().startsWith(source.getName()) && file.getName().endsWith("optimized")) {
        return file;
      }
    }
    throw new FileNotFoundException("Could not find gimple output in " + dir.getAbsolutePath() + ", files present: " +
            Arrays.toString(dir.listFiles()));
  }

  public void addIncludeDirectory(File path) {
    includeDirectories.add(path);
  }

}
