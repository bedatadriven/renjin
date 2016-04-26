package org.renjin.gcc.link;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

/**
 * A named function or global variable.
 * 
 * <p>The LinkSymbol class provides an analogy for the linking stage of a native
 * build process.</p>
 * 
 * <p>When Gimple sources are compiled, we write a property file for each exported symbol
 * to {@code META-INF/org.renjin.gcc.symbols/SYMBOL_NAME} that points to the class in which
 * the function is implemented.</p>
 * 
 */
public class LinkSymbol {



  /**
   * The type of Symbol
   */
  public enum SymbolType {

    /**
     * References a static field
     */
    FIELD,

    /**
     * References a static method
     */
    METHOD,

    /**
     * References a class that implements {@link org.renjin.gcc.codegen.call.CallGenerator}
     */
    CALL_GENERATOR
  }
  
  private String name;
  private SymbolType type;
  private String className;
  private String memberName;
  private String descriptor;
  
  private LinkSymbol() {
  }

  public static LinkSymbol forFunction(String name, Handle methodHandle) {
    LinkSymbol symbol = new LinkSymbol();
    symbol.type = SymbolType.METHOD;
    symbol.name = name;
    symbol.className = methodHandle.getOwner();
    symbol.memberName = methodHandle.getName();
    symbol.descriptor = methodHandle.getDesc();
    return symbol;
  }

  public String getName() {
    return name;
  }

  public SymbolType getType() {
    return type;
  }

  public String getClassName() {
    return className;
  }

  public String getMemberName() {
    return memberName;
  }


  
  public Method loadMethod() {
    return loadMethod(getClass().getClassLoader());
  }
  
  /**
   * Loads the {@code java.lang.reflect.Method} object referenced by this 
   * {@code LinkSymbol}.
   * @param classLoader
   * @throws IllegalStateException if this LinkSymbol does not refer to a symbol.
   * @return
   */
  public Method loadMethod(ClassLoader classLoader) {
    if(type != LinkSymbol.SymbolType.METHOD) {
      throw new IllegalStateException(
          String.format("Invalid link: Tried to link name '%s' to function, found symbol of type %s", name, type));
    }

    Class<?> owner;
    try {
      owner = classLoader.loadClass(className.replace("/", "."));
    } catch (ClassNotFoundException e) {
      throw new InternalCompilerException(String.format("Could not load class %s referenced by symbol '%s'",
          className, type));
    }

    for (java.lang.reflect.Method method : owner.getMethods()) {
      if(method.getName().equals(memberName) &&
          Type.getMethodDescriptor(method).equals(descriptor)) {
        
        if(!Modifier.isPublic(method.getModifiers())) {
          throw new InternalCompilerException(
              String.format("Symbol '%s' references non-public method %s", name, method));
        }

        if(!Modifier.isStatic(method.getModifiers())) {
          throw new InternalCompilerException(
              String.format("Symbol '%s' references non-static method %s", name, method));
        }

        return method;
      }
    }

    throw new InternalCompilerException(String.format("Symbol '%s' references non-existant method %s.%s (%s)",
        name, className, memberName, descriptor));
  }
  

  /**
   * Writes this symbol out to an output.
   * 
   * <p>By convention, the path of the symbol will be 
   * {@code $outputDir/META-INF/org.renjin.gcc.symbols/$name}</p>
   * 
   * @param outputDir the root of a class output 
   */
  public void write(File outputDir) throws IOException {
    File metaInfDir = new File(outputDir, "META-INF");
    File symbolsDir = new File(metaInfDir, "org.renjin.gcc.symbols");
    File symbolFile = new File(symbolsDir, name);
    
    if(!symbolsDir.exists()) {
      boolean created = symbolsDir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory " + symbolsDir.getAbsolutePath());
      }
    }

    Properties properties = new Properties();
    properties.setProperty("type", type.name());
    properties.setProperty("class", className);
    
    if(type == SymbolType.FIELD || type == SymbolType.METHOD) {
      properties.setProperty("member", memberName);
      properties.setProperty("descriptor", descriptor);
    }
    
    try(Writer out = new OutputStreamWriter(new FileOutputStream(symbolFile), Charsets.UTF_8)) {
      properties.store(out, name);      
    }
  }
  
  public static LinkSymbol fromDescriptor(String symbolName, Properties properties) {

    LinkSymbol symbol = new LinkSymbol();
    symbol.name = symbolName;
    symbol.type = SymbolType.valueOf(properties.getProperty("type"));
    symbol.className = properties.getProperty("class");
    
    if(symbol.type == SymbolType.FIELD || symbol.type == SymbolType.METHOD) {
      symbol.memberName = properties.getProperty("member");
      symbol.descriptor = properties.getProperty("descriptor");
    }
    
    return symbol;
  }

  /**
   * Looks up a LinkSymbol from the classpath.
   */
  public static Optional<LinkSymbol> lookup(ClassLoader classLoader, String name) throws IOException {
    InputStream in = classLoader.getResourceAsStream("META-INF/org.renjin.gcc.symbols/" + name);
    if(in == null) {
      return Optional.absent();
    }
    try(InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8))  {
      Properties properties = new Properties();
      properties.load(reader);

      return Optional.of(fromDescriptor(name, properties)); 
    }
  }

}
