package org.renjin.primitives.annotations.processor;

import java.util.List;

import org.renjin.primitives.Primitives.Entry;


import r.jvmi.binding.JvmMethod;
import r.lang.exception.EvalException;

/**
 * Base class for all the different strategies for generating 
 * wrapper code for a given R function.
 * 
 * 
 * @author alex
 *
 */
public abstract class GeneratorStrategy {

    
  public abstract boolean accept(List<JvmMethod> overloads);

  public final void generate(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads) {
    
    
    s.writePackage("r.base.primitives");   
    s.writeImport("r.lang.*");
    s.writeImport(WrapperRuntime.class);
    s.writeImport(ArgumentException.class);
    s.writeImport(ArgumentIterator.class);
    s.writeImport(EvalException.class);

    s.writeStaticImport("org.renjin.primitives.annotations.processor.WrapperRuntime.*");

    s.writeBeginClass(entry);
    s.writeBlankLine();
    s.writeConstructor(entry.name);
    s.writeBlankLine();
   
    generateMethods(entry, s, overloads);
    
    s.writeCloseBlock();
    s.close();    
  }
  
  
  protected abstract void generateMethods(Entry entry, WrapperSourceWriter s,
      List<JvmMethod> overloads);
   
}
