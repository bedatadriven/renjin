package org.renjin.gcc;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.shimple.ShimpleWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Compiles a set of Gimple functions to jvm class file
 * 
 */
public class GimpleCompiler {

	private File outputDirectory;
	private File sootDirectory;
	private String packageName;
	private String className;
	
	private ShimpleWriter writer;
	private int indent;

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());
	
	public void setPackageName(String name) {
		this.packageName = name;
	}
	
	public void setOutputDirectory(File directory) {
		this.outputDirectory = directory;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void compile(List<GimpleFunction> functions) throws Exception {
		
		File packageFolder = getPackageFolder();
		packageFolder.mkdirs();
		
		writeShimple(functions);

    System.out.println("outputDirectory = " + outputDirectory.getAbsolutePath());

    String separator = System.getProperty("file.separator");
    String classpath = System.getProperty("java.class.path");
    String path = System.getProperty("java.home")
            + separator + "bin" + separator + "java";
    ProcessBuilder processBuilder =
            new ProcessBuilder(path,
            "-cp",
            classpath,
            "soot.Main",
            "-v",
            "-pp",
            "--src-prec", "jimple",
            "-output-dir", outputDirectory.getAbsolutePath(),
            packageName + "." + className);

    Process process = processBuilder.start();
    process.waitFor();

    String err = new String(ByteStreams.toByteArray(process.getErrorStream()));
    System.out.println(err);

	}

	protected void writeShimple(List<GimpleFunction> functions)
			throws FileNotFoundException {
		outputDirectory.mkdirs();
		String fqClassName = packageName + "." + className;
		File shimpleSource = new File(outputDirectory, fqClassName + ".jimple");

    LOGGER.info("Writing shimple source to " + shimpleSource);

		this.writer = new ShimpleWriter(shimpleSource, fqClassName);
		
		// write default constructor
		writer.writeDefaultConstructor();

    for(GimpleFunction function : functions) {
      writer.writeFunction(function);

    }
		writer.closeBlock();
		writer.close();

    try {
      System.out.println(Files.toString(shimpleSource, Charsets.UTF_8));
    } catch (IOException e) {

    }
  }
	
	private File getPackageFolder() {
		return new File(outputDirectory, packageName.replace('.', File.separatorChar));
	}
	
	private File getSootPackageFolder() {
		return new File(getSootOutputDirectory(), packageName.replace('.', File.separatorChar));
	}

	protected File getSootOutputDirectory() {
		File sootRoot = sootDirectory;
		if(sootRoot == null ){
			sootRoot = new File(outputDirectory.getParentFile(), "soot");
		}
		return sootRoot;
	}
	
	private void println(String text) {
		writer.println(text);
	}
	
}
