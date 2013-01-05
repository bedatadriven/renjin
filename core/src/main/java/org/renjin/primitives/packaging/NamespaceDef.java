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

  public static class S3Export {
    private Symbol genericFunction;
    private Symbol className;
    
    private S3Export(Symbol genericFunction, Symbol className) {
      super();
      this.genericFunction = genericFunction;
      this.className = className;
    }
    
    /**
     * @return the name of the generic function (e.g. 'summary' or 'print')
     */
    public Symbol getGenericFunction() {
      return genericFunction;
    }
    
    /**
     * @return the name of the S3 class
     */
    public Symbol getClassName() {
      return className;
    }

    /**
     * @return the name of the specific S3 method (e.g. as.matrix.dist)
     */
    public Symbol getMethod() {
      return Symbol.get(genericFunction.getPrintName() + "." + className.getPrintName());
    }
  }
  
  private static class Import {
    private Symbol namespace;
    private List<Symbol> symbols = Lists.newArrayList();
    
    
  }
  
  
  private List<Symbol> imports = Lists.newArrayList();
  private List<Symbol> exports = Lists.newArrayList();
  private List<S3Export> s3Exports = Lists.newArrayList();
  
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
        
      } else if(call.getFunction() == Symbol.get("importFrom")) {
        
        
      } else if(call.getFunction() == Symbol.get("S3method")) {
        Symbol generic = toSymbol( call.getArgument(0) );
        Symbol klass = toSymbol( call.getArgument(1) );
        s3Exports.add(new S3Export(generic, klass));
      }
    }
  }
  
  private void addTo(PairList arguments, List<Symbol> list) {
    for(SEXP exp : arguments.values()) {
      list.add(toSymbol(exp));
    }
  }

  private Symbol toSymbol(SEXP exp) {
    if(exp instanceof Symbol) {
      return (Symbol)exp;
    } else if(exp instanceof StringVector && exp.length() == 1) {
      return Symbol.get(  ((StringVector) exp).getElementAsString(0)  );
    } else {
      throw new IllegalArgumentException(exp.toString());
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

  public List<S3Export> getS3Exports() {
    return s3Exports;
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
