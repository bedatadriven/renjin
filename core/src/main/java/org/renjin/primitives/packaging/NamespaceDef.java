package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;

public class NamespaceDef {

  private List<Symbol> imports = Lists.newArrayList();
  private List<Symbol> exports = Lists.newArrayList();
  
  public void parse(Reader reader) throws IOException {
    parse(RParser.parseAllSource(reader));
  }

  private void parse(ExpressionVector vector) {
    for(SEXP exp : vector) {
      FunctionCall call = (FunctionCall)exp;
      if(call.getFunction() == Symbol.get("export")) {
        addTo(call.getArguments(), exports);
        
      } else if(call.getFunction() == Symbol.get("import")) {
        addTo(call.getArguments(), imports);
        
      } else if(call.getFunction() == Symbol.get("S3Method")) {
        
      }
    }
  }
  
  private void addTo(PairList arguments, List<Symbol> list) {
    for(SEXP exp : arguments.values()) {
      if(exp instanceof Symbol) {
        list.add((Symbol)exp);
      } else if(exp instanceof StringVector) {
        String name = ((StringVector) exp).getElementAsString(0);
        list.add(Symbol.get(name));
      }
    }
  }

  public List<Symbol> getImports() {
    return imports;
  }

  public void setImports(List<Symbol> imports) {
    this.imports = imports;
  }

  public List<Symbol> getExports() {
    return exports;
  }

  public void setExports(List<Symbol> exports) {
    this.exports = exports;
  }

  public void parse(InputSupplier<InputStreamReader> readerSupplier) throws IOException {
    Reader reader = readerSupplier.getInput();
    try {
      parse(reader);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
  
  
}
