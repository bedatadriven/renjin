package r.jvmi.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import r.base.BaseFrame;
import r.base.BaseFrame.Entry;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.generator.GeneratorStrategy;
import r.jvmi.wrapper.generator.PassThrough;
import r.jvmi.wrapper.generator.UnaryRecyclingStrategy;
import r.jvmi.wrapper.generator.SingleOverloadWithoutRecycling;
import r.jvmi.wrapper.generator.AnnotationBasedStrategy;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * 
 * Generates wrapper classes for primitive functions. 
 * 
 * 
 * 
 * 
 * @author alex
 *
 */
public class WrapperGenerator {
  
  
  public static void main(String[] args) throws IOException {
    
    File baseDir = new File("");

    WrapperGenerator generator = new WrapperGenerator(baseDir);
    
    if(args.length > 0) {
      generator.setSingleFunction(args[0]);
    }
    generator.generate();
   
    System.exit(generator.isSuccessfull() ? 0 : 1);
  }
  
  private File sourcesDir;
  private File outputDir;
  private String singleFunction;
  
  private boolean encounteredError = false;
  
  private List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
  
  public WrapperGenerator(File baseDir) {
    sourcesDir = new File(baseDir.getAbsoluteFile() + File.separator + "target" + File.separator + "generated-sources" + File.separator +
        "r-wrappers" + File.separator +
        "r" + File.separator + "base" + File.separator + "primitives");  
    sourcesDir.mkdirs();

    outputDir = new File(baseDir.getAbsoluteFile() + File.separator + "target" + File.separator + "classes");

  }

  public void setSingleFunction(String name) {
    this.singleFunction = name;
  }
  
  public void generate() throws IOException {
    generateSources();
    compile();
  }

  public boolean isSuccessfull() {
    return !encounteredError;
  }
  
  private void generateSources()
      throws IOException {
    
    List<GeneratorStrategy> strategies = Lists.newArrayList();
    strategies.add(new PassThrough());
  //  strategies.add(new SingleOverloadWithoutRecycling());
   // strategies.add(new UnaryRecyclingStrategy());
    strategies.add(new AnnotationBasedStrategy());

  
    List<Entry> entries = new BaseFrame().getEntries();
    for(Entry entry : entries) {
      if(singleFunction == null || singleFunction.equals(entry.name)) {
        List<JvmMethod> overloads = JvmMethod.findOverloads(entry.functionClass, entry.name, entry.methodName);
        
        if(overloads.isEmpty()) {
          System.out.println(entry.name + ": not implemented.");
        } else {
          
          GeneratorStrategy strategy = findStrategy(strategies, overloads);
          if(strategy != null) {
            System.out.println(entry.name + ": using " + strategy.getClass().getSimpleName() + " strategy" );
    
            generate(sourcesDir, entry, overloads, strategy);
          } else {
            System.out.println(entry.name + ": no generation strategy available");
          }
        }
      }
    }

  }


  private void generate(File sourcesDir, Entry entry,
      List<JvmMethod> overloads, GeneratorStrategy strategy) throws IOException {
    try {
      String className = toJavaName(entry.name);
      File sourceFile = new File(sourcesDir, className + ".java"); 
      WrapperSourceWriter writer = new WrapperSourceWriter(sourceFile, className);
      strategy.generate(writer, entry, overloads);
      compilationUnits.add(new WrapperSource(sourceFile));
      
    } catch(GeneratorDefinitionException e) {
      System.err.println("Error generatoring wrapper for '" + entry.name + "': " + e.getMessage());
      System.err.println("Overloads defined:");
      for(JvmMethod method : overloads) {
        System.err.println("  " + method.toString());
      }
      encounteredError = true;
    }
  }
    
  
  private static GeneratorStrategy findStrategy(
      List<GeneratorStrategy> strategies, List<JvmMethod> overloads) {
    
    for(GeneratorStrategy strategy : strategies) {
      if(strategy.accept(overloads)) {
        return strategy;
      }
    }
    return null;
  }
  
  private static class WrapperSource extends SimpleJavaFileObject {
    
    private File file;
    
    public WrapperSource(File file) {
      super(file.toURI(), JavaFileObject.Kind.SOURCE);
      this.file = file;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
        throws IOException {
      
      return Files.toString(file, Charsets.UTF_8);
      
    }
    
  }

  public void compile() throws IOException {

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    StandardJavaFileManager jfm = compiler.getStandardFileManager(diagnostics, null, null);

    jfm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir));

    JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, diagnostics, null, null, compilationUnits);

    boolean success = task.call();

    if(!success) {
      System.err.println("Compilation failed: ");
      for(Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
        System.err.println(d.toString());
      }
      encounteredError = true;
    }
    
    jfm.close();
  }

  public static String toJavaName(String rName) {
    StringBuilder sb = new StringBuilder();
    sb.append("R$primitive$");
    // for some readability, translate "." to $
    rName = rName.replace('.', '$');
    
    for(int i=0;i!=rName.length();++i) {
      int cp = rName.codePointAt(i);
      if(Character.isJavaIdentifierPart(cp)) {
        sb.appendCodePoint(cp);
      } else {
        sb.append("_$" + cp + "$_");
      }
    }
    return sb.toString();
  }
  
  public static String toFullJavaName(String rName) {
    return "r.base.primitives." + toJavaName(rName);
  }
}
