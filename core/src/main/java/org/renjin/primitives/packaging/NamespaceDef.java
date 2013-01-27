package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
  
  public static class NamespaceImport {
    private Symbol namespace;
    private List<Symbol> symbols = Lists.newArrayList();
    private boolean importAll;
    public Symbol getNamespace() {
      return namespace;
    }
    public List<Symbol> getSymbols() {
      return symbols;
    }
    public boolean isImportAll() {
      return importAll;
    }
  }
  
  private List<NamespaceImport> imports = Lists.newArrayList();
  private Set<Symbol> exports = Sets.newHashSet();
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
        NamespaceImport nsImport = new NamespaceImport();
        nsImport.namespace = toSymbol( call.getArgument(0) );
        nsImport.importAll = true;
        imports.add(nsImport);
        
      } else if(call.getFunction() == Symbol.get("importFrom")) {
        
        NamespaceImport nsImport = new NamespaceImport();
        nsImport.namespace = toSymbol( call.getArgument(0) );
        for(int i=1; i<call.getArguments().length();++i) {
          nsImport.symbols.add( toSymbol( call.getArgument(i) ) );
        }
        imports.add(nsImport);
        
      } else if(call.getFunction() == Symbol.get("S3method")) {
        Symbol generic = toSymbol( call.getArgument(0) );
        Symbol klass = toSymbol( call.getArgument(1) );
        s3Exports.add(new S3Export(generic, klass));
      }
    }
  }
  
  private void addTo(PairList arguments, Collection<Symbol> list) {
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

  public List<NamespaceImport> getImports() {
    return imports;
  }

  public Set<Symbol> getExports() {
    return exports;
  }

  public List<S3Export> getS3Exports() {
    return s3Exports;
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
