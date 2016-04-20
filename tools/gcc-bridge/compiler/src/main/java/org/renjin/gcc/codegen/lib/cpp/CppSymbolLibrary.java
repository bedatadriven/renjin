package org.renjin.gcc.codegen.lib.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.renjin.gcc.codegen.call.FreeCallGenerator;
import org.renjin.gcc.codegen.call.MallocCallGenerator;
import org.renjin.gcc.codegen.lib.SymbolFunction;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.SymbolMethod;
import org.renjin.gcc.codegen.type.TypeOracle;


public class CppSymbolLibrary implements SymbolLibrary {

  @Override
  public List<SymbolFunction> getFunctions(TypeOracle typeOracle) {
    List<SymbolFunction> functions = new ArrayList<SymbolFunction>();
    functions.add(new SymbolFunction("operator new", new MallocCallGenerator(typeOracle)));
    functions.add(new SymbolFunction("operator delete", new FreeCallGenerator()));
    functions.add(new SymbolFunction("__comp_ctor ", new CtorCallGenerator())); // complete constructor
    functions.add(new SymbolFunction("__comp_dtor ", new DtorCallGenerator())); // complete destructor
    return functions;
  }

  @Override
  public List<SymbolMethod> getMethods() {
    return Collections.emptyList();
  }
}