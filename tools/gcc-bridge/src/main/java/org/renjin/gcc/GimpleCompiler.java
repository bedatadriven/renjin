package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.translate.GimpleFunctionTranslator;
import org.renjin.gcc.translate.MethodTable;
import org.renjin.gcc.translate.TranslationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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

  private static Logger LOGGER = Logger.getLogger(GimpleCompiler.class.getName());

  private MethodTable methodTable = new MethodTable();

	public void setPackageName(String name) {
		this.packageName = name;
	}
	
	public void setOutputDirectory(File directory) {
		this.outputDirectory = directory;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

  public MethodTable getMethodTable() {
    return methodTable;
  }

  public void compile(List<GimpleFunction> functions) throws Exception {
		
		File packageFolder = getPackageFolder();
		packageFolder.mkdirs();

    JimpleOutput output = translate(functions);

    System.out.println("outputDirectory = " + outputDirectory.getAbsolutePath());
    output.write(outputDirectory);

    compileJimple(output.getClassNames());
	}

  private void compileJimple(Set<String> classNames) throws IOException, InterruptedException {
    List<String> options = Lists.newArrayList();
    options.add("-v");
    options.add("-pp");
    options.add("-src-prec");
    options.add("jimple");
    options.add("-output-dir");
    options.add(outputDirectory.getAbsolutePath());
    options.addAll(classNames);

//    soot.Main.main(options.toArray(new String[0]));

    compileJimpleNewProcess(options);
  }

  private void compileJimpleNewProcess(List<String> sootOptions) throws IOException, InterruptedException {
    LOGGER.info("Compiling " + className);
    String separator = System.getProperty("file.separator");
    String classpath = System.getProperty("java.class.path");
    String path = System.getProperty("java.home")
            + separator + "bin" + separator + "java";

    List<String> cmd = Lists.newArrayList();
    cmd.add(path);
    cmd.add("-cp");
    cmd.add(classpath);
    cmd.add("soot.Main");
    cmd.addAll(sootOptions);


    ProcessBuilder processBuilder =
            new ProcessBuilder(cmd);

    Process process = processBuilder.start();
    int exitCode = process.waitFor();

    if(exitCode != 0) {
      throw new RuntimeException(new String(ByteStreams.toByteArray(process.getErrorStream())));
    }
  }

  protected JimpleOutput translate(List<GimpleFunction> functions)
          throws IOException {

    JimpleOutput jimple = new JimpleOutput();

    JimpleClassBuilder mainClass = jimple.newClass();
    mainClass.setClassName(className);
    mainClass.setPackageName(packageName);

    TranslationContext context = new TranslationContext(mainClass, methodTable, functions);
    for(GimpleFunction function : functions) {
      GimpleFunctionTranslator translator = new GimpleFunctionTranslator(context);
      translator.translate(function);
    }
    return jimple;
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

}
