package org.renjin.gcc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.translate.FunctionTranslator;
import org.renjin.gcc.translate.MethodTable;
import org.renjin.gcc.translate.TranslationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * Compiles a set of Gimple functions to jvm class file
 * 
 */
public class GimpleCompiler {

	private File outputDirectory;
  private String packageName;
	private String className;
  private boolean verbose;

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

    output.write(outputDirectory);

    compileJimple(output.getClassNames());
	}

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  private void compileJimple(Set<String> classNames) throws IOException, InterruptedException {
    List<String> options = Lists.newArrayList();
    if(verbose) {
      options.add("-v");
    }
    options.add("-pp");
    options.add("-src-prec");
    options.add("jimple");
    options.add("-keep-line-number");
    options.add("-output-dir");
    options.add(outputDirectory.getAbsolutePath());
    options.addAll(classNames);

    LOGGER.info("Running Soot " + Joiner.on(" ").join(options));

    //soot.Main.main(options.toArray(new String[0]));

    compileJimpleNewProcess(options);
  }

  private void compileJimpleNewProcess(List<String> sootOptions) throws IOException, InterruptedException {
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

    OutputPrinter outputPrinter = new OutputPrinter(process.getInputStream());
    outputPrinter.start();

    int exitCode = process.waitFor();

    outputPrinter.join();

    LOGGER.severe("Soot finished with exit code " + exitCode);

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
      FunctionTranslator translator = new FunctionTranslator(context);
      translator.translate(function);
    }
    return jimple;
  }
	
	private File getPackageFolder() {
		return new File(outputDirectory, packageName.replace('.', File.separatorChar));
	}

  private class OutputPrinter extends Thread {

    private final Reader reader;
    private final StringBuilder line = new StringBuilder();

    private OutputPrinter(InputStream in) {
      this.reader = new InputStreamReader(in);
    }

    @Override
    public void run() {
      while(true) {
        try {
          int cp = reader.read();
          if(cp == -1) {
            return;
          }
          if(cp == '\n') {
            if(verbose) {
              System.err.println(line.toString());
            }
            line.setLength(0);
          } else {
            line.appendCodePoint(cp);
          }

        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Caught exception while reading stdout from Soot", e);
        }
      }
    }
  }
}
