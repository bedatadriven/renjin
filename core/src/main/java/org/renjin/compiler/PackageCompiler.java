package org.renjin.compiler;

import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRFunction;
import org.renjin.compiler.ir.tac.IRFunctionTable;
import org.renjin.compiler.ir.tac.expressions.IRThunk;
import org.renjin.eval.Context;
import org.renjin.packaging.PackagingUtils;
import org.renjin.parser.RParser;
import org.renjin.primitives.annotations.processor.WrapperGenerator2;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackageCompiler {

  private static final Logger LOGGER = Logger.getLogger(PackageCompiler.class.getName());
  private File rootDir;
  private File outDir;
  private String packageName = "org/renjin/base";
  private Environment packageEnvironment;
  private File packageDir;
  private boolean base;
  private boolean compile;
  
  public PackageCompiler(File rootDir) throws IOException {
    this.rootDir = rootDir;
  }
  
  public void setOutputDir(File outputDir) {
    this.outDir = outputDir;
  }

  public void compile() throws FileNotFoundException, IOException {

    this.packageDir = new File(outDir, packageName);
    packageDir.mkdirs();

    evaluatePackageBody();
    if(compile) {
      compileClosures();  
      compileLoader(); 
    } else {
      createDatabase();
    }
  }

  private void createDatabase() {
    
  }

  private void compileClosures() throws FileNotFoundException, IOException {
    for(Symbol name : packageEnvironment.getSymbolNames()) {
      SEXP value = packageEnvironment.getVariable(name);
      if(value instanceof Closure) {
        try { 
          compileClosure(packageMethodClassName(name), (Closure)value);
        } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "Error compiling closure '" + name + "'", e);
          throw new RuntimeException("Error compiling closure '" + name + "'", e);
        }
      }
    }
  }

  private String packageMethodClassName(Symbol name) {
    return WrapperGenerator2.toJavaName("", name.getPrintName());
  }

  private void compileClosure(String name, Closure closure) throws IOException {
    IRBodyBuilder builder = new IRBodyBuilder(new IRFunctionTable());
    IRBody body = builder.build(closure.getBody());
    IRFunction function = new IRFunction(closure.getFormals(), closure.getBody(), body);
    compileFunction(packageName + "/" + name, function);
  }
  
  private void compileFunction(String fullClassName, IRFunction closure)
      throws FileNotFoundException, IOException {
   
    ClosureCompiler closureCompiler = new ClosureCompiler(fullClassName);
    writeClassFile(fullClassName, closureCompiler.doCompile(closure));
    
    // need to compile any nested closures we encountered
    for(Entry<String, IRFunction> entry : closureCompiler.getNestedClosures()) {
      compileFunction(entry.getKey(), entry.getValue());
    }
    
    // and also thunks (unevalauted arguments) 
    ThunkMap thunkMap = closureCompiler.getThunkMap();
    Set<IRThunk> compiledThunks = Sets.newHashSet();
    Set<IRThunk> toCompile = Sets.newHashSet(thunkMap.keySet());

    do {
      for(IRThunk thunk : toCompile) {
        String className = thunkMap.getClassName(thunk);
        compileThunk(thunkMap, className, thunk);
        compiledThunks.add(thunk);
      }
      toCompile = Sets.newHashSet(thunkMap.keySet());
      toCompile.removeAll(compiledThunks);
    } while(!toCompile.isEmpty());
  }

  private void compileThunk(ThunkMap thunkMap, String className, IRThunk thunk) throws FileNotFoundException, IOException {
    ThunkCompiler compiler = new ThunkCompiler(thunkMap, className);
    writeClassFile(className, compiler.doCompile(thunk));
  }

  private void writeClassFile(String className, byte[] content)
      throws FileNotFoundException, IOException {
    File classFile = new File(outDir, className + ".class");
    FileOutputStream fos = new FileOutputStream(classFile);
    fos.write(content);
    fos.close();
  }
  
  private void compileLoader() throws IOException {
    File classFile = new File(packageDir, "Loader.class");
    FileOutputStream fos = new FileOutputStream(classFile);
    fos.write(PackageLoaderCompiler.compile(packageName, packageEnvironment));
    fos.close();
  }
  
  private void evaluatePackageBody() throws FileNotFoundException,
      IOException {

    Context context = Context.newTopLevelContext();
    if(!base) {
      context.init();
    }    
    List<File> srcFiles = PackagingUtils.findSourceFiles(rootDir);

    for(File srcFile : srcFiles) {
      System.out.println("Evaluating " + srcFile.getName());
      Reader reader = new InputStreamReader(new FileInputStream(srcFile));
      String source = CharStreams.toString(reader);
      SEXP srcBody = RParser.parseSource(source + "\n");
      reader.close();
      context.evaluate(srcBody);
    }
    this.packageEnvironment = context.getEnvironment();
    
    for(Symbol symbol : packageEnvironment.getSymbolNames()) {
      System.out.println(symbol + "=>" + packageEnvironment.getVariable(symbol).getTypeName());
    }
  }

  
  public static void main(String[] args) throws IOException {
    PackageCompiler compiler = new PackageCompiler(new File(args[0]));
    compiler.setOutputDir(new File(args[1]));
    compiler.compile();
  }
  
}
