package org.renjin.primitives.packaging;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class NamespaceDef {

  public List<DynLib> getDynLibs() {
    return dynLibs;
  }

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

  /**
   * GNU-R-Style Dynamic library, which we map
   * to a JVM class
   */
  public static class DynLib {
    private Symbol packageName;
    private String prefix;

    public DynLib(Symbol packageName) {
      this.packageName = packageName;
      this.prefix = "";
    }

    public Symbol getPackageName() {
      return packageName;
    }

    public String getPrefix() {
      return prefix;
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
  private List<DynLib> dynLibs = Lists.newArrayList();
  
  public void parse(Reader reader) throws IOException {
    parse(RParser.parseAllSource(reader));
  }

  private void parse(ExpressionVector vector) {
    for(SEXP exp : vector) {
      FunctionCall call = (FunctionCall)exp;
      if(call.getFunction() == Symbol.get("export")) {
        addTo(call.getArguments(), exports);
        
      } else if(call.getFunction() == Symbol.get("import")) {
        for(int i=0; i<call.getArguments().length();++i) {
          NamespaceImport nsImport = new NamespaceImport();
          nsImport.namespace = toSymbol( call.getArgument(i) );
          nsImport.importAll = true;
          imports.add(nsImport);          
        }
        
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

      } else if(call.getFunction() == Symbol.get("useDynLib")) {
        DynLib dynLib = new DynLib( toSymbol( call.getArgument(0) ) );
        SEXP prefix = call.getNamedArgument(".fixes");
        if(prefix instanceof StringVector) {
          dynLib.prefix = ((StringVector) prefix).getElementAsString(0);
        }
        dynLibs.add(dynLib);
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


  private Symbol toSymbol(PairList arguments, String name) {
    for(PairList.Node node : arguments.nodes()) {
      if(node.hasTag() && node.getTag().getPrintName().equals(name)) {
        return toSymbol(node.getValue());
      }
    }
    throw new EvalException("no argument named '%s'", name);
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
