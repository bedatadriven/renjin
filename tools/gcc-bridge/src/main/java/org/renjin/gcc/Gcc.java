package org.renjin.gcc;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Gcc {

	private String gccBinary = "gcc";
  private List<String> includeDirectories = Lists.newArrayList();

  private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

	public String compileToGimple(File source) throws IOException {
		
		List<String> command = Lists.newArrayList();
		command.add(gccBinary);
		command.add("-c"); // compile only, do not link
		command.add("-S"); // stop at assembly generation
		
		// Turn on dumping of gimple trees
		// See http://gcc.gnu.org/onlinedocs/gcc/Debugging-Options.html#Debugging-Options
		command.add("-fdump-tree-optimized");
		command.add("-fdump-tree-optimized-raw");

    for(String includeDir : includeDirectories) {
      command.add("-I");
      command.add(includeDir);
    }
	
		command.add(source.getAbsolutePath());

    LOGGER.info("Executing " + Joiner.on(" ").join(command));

		File tempDir = createTempDirectory();
		
		Process gcc = new ProcessBuilder(command)
			.directory(tempDir)
			.start();
			
		try {
			gcc.waitFor();
		} catch (InterruptedException e) {
			throw new GccException("Compiler interrupted");
		}
		
    String stderr = new String(ByteStreams.toByteArray(gcc.getErrorStream()));

		if(gcc.exitValue() != 0) {
			throw new GccException("Compilation failed:\n" + stderr);
		} else {
      java.lang.System.err.println(stderr);
    }
		
		File gimple = new File(tempDir, source.getName() + ".143t.optimized");
		
		return Files.toString(gimple, Charsets.UTF_8);
	}

  public void addIncludeDirectory(File path) {
    includeDirectories.add(path.getAbsolutePath());
  }

	public File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if(!(temp.delete())) {
			throw new GccException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if(!(temp.mkdir())) {
			throw new GccException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}
}
