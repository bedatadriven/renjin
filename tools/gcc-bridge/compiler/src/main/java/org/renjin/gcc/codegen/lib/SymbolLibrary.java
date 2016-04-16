package org.renjin.gcc.codegen.lib;

import java.util.List;

import org.renjin.gcc.codegen.type.TypeOracle;

public interface SymbolLibrary {

	List<SymbolFunction> getFunctions(TypeOracle typeOracle);

	List<SymbolMethod> getMethods();
}