package org.renjin.primitives.packaging;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Closeables;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Populates a namespace based on instructions
 * from the NAMESPACE file
 */
public class NamespaceDirectiveParser {

  private ExpressionVector source;
  private NamespaceDirectiveHandler handler;

  public static void parse(CharSource charSource, NamespaceDirectiveHandler handler) throws IOException {
    Reader reader = charSource.openStream();
    ExpressionVector source;
    try {
      source = RParser.parseAllSource(reader);
    } finally {
      Closeables.closeQuietly(reader);
    }
    parse(source, handler);
  }

  private static void parse(ExpressionVector source, NamespaceDirectiveHandler handler) {
    for(SEXP exp : source) {
      if(!(exp instanceof FunctionCall)) {
        throw new EvalException("Unknown NAMESPACE directive: " + exp.toString());
      }

      FunctionCall call = (FunctionCall)exp;
      parseCall(call, handler);
    }
  }

  private static void parseCall(FunctionCall call, NamespaceDirectiveHandler handler) {
    String directiveName = parseDirectiveName(call);
    if(directiveName.equals("import")) {
      parseImport(call, handler);
    } else if(directiveName.equals("importClass")) {
      parseImportClass(call, handler);
    } else if(directiveName.equals("importFrom")) {
      parseImportFrom(call, handler);
    } else if(directiveName.equals("importFromClass")) {
      parseImportFromClass(call, handler);
    } else if(directiveName.equals("S3method")) {
      parseS3Export(call, handler);
    } else if(directiveName.equals("export")) {
      parseExport(call, handler);
    } else if(directiveName.equals("exportPattern")) {
      parseExportPattern(call, handler);
    } else if(directiveName.equals("useDynLib")) {
      parseDynlib(call, handler);
    } else if(directiveName.equals("exportClasses")) {
      parseS4Export(call, handler);
    } else {
      throw new EvalException("Unknown NAMESPACE directive '" + directiveName + "'");
    }
  }



  private static void parseExportPattern(FunctionCall call, NamespaceDirectiveHandler handler) {
    if(call.getArguments().length() != 1) {
      throw new EvalException("Expected one argument to exportPattern() directive");
    }
    handler.exportPattern(parseStringArgument(call.getArgument(0)));
  }

  private static void parseExport(FunctionCall call, NamespaceDirectiveHandler handler) {
    List<Symbol> symbols = Lists.newArrayList();
    for(SEXP argument : call.getArguments().values()) {
      symbols.add(parseSymbolArgument(argument));
    }
    handler.export(symbols);
  }

  private static void parseImport(FunctionCall call, NamespaceDirectiveHandler handler) {
    handler.import_(parseNameArguments(call));
  }

  private static void parseImportClass(FunctionCall call, NamespaceDirectiveHandler handler) {
    handler.importClass(parseNameArguments(call));
  }

  private static List<String> parseNameArguments(FunctionCall call) {
    List<String> names = Lists.newArrayList();
    for(SEXP argument : call.getArguments().values()) {
      names.add(parseStringArgument(argument));
    }
    return names;
  }


  private static void parseImportFrom(FunctionCall call, NamespaceDirectiveHandler handler) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importFrom directive");
    }
    String packageName = parseStringArgument(call.getArgument(0));
    List<Symbol> symbols = Lists.newArrayList();
    for(int i=1;i<call.getArguments().length();++i) {
      symbols.add(parseSymbolArgument(call.getArgument(i)));
    }
    handler.importFrom(packageName, symbols);
  }

  private static void parseImportFromClass(FunctionCall call, NamespaceDirectiveHandler handler) {
    if(call.getArguments().length() < 2) {
      throw new EvalException("Expected at least two arguments to importFrom directive");
    }
    String className = parseStringArgument(call.getArgument(0));
    List<Symbol> methods = Lists.newArrayList();
    for(int i=1;i<call.getArguments().length();++i) {
      methods.add(parseSymbolArgument(call.getArgument(i)));
    }
    handler.importFromClass(className, methods);
  }

  private static Symbol parseSymbolArgument(SEXP argument) {
    if(argument instanceof Symbol) {
      return (Symbol) argument;
    } else if(argument instanceof StringVector && argument.length() == 1) {
      return Symbol.get(((StringVector) argument).getElementAsString(0));
    } else {
      throw new EvalException("Can't parse directive argument '" + argument + "' as symbol");
    }
  }

  private static String parseStringArgument(SEXP argument) {
    if(argument instanceof StringVector && argument.length() == 1) {
      return ((StringVector) argument).getElementAsString(0);
    } else if(argument instanceof Symbol) {
      return ((Symbol) argument).getPrintName();
    } else {
      throw new EvalException("Can't parse directive argument '" + argument + "' as string");
    }
  }

  private static String parseDirectiveName(FunctionCall call) {
    if(call.getFunction() instanceof Symbol) {
      return ((Symbol) call.getFunction()).getPrintName();
    } else {
      throw new EvalException("Unknown NAMESPACE directive: " + call);
    }
  }

  private static void parseS3Export(FunctionCall call, NamespaceDirectiveHandler handler) {
    if(call.getArguments().length() == 2) {
      handler.S3method(
          parseSymbolArgument(call.getArgument(0)),
          parseStringArgument(call.getArgument(1)));
    } else if(call.getArguments().length() == 3) {
      handler.S3method(
          parseSymbolArgument(call.getArgument(0)),
          parseStringArgument(call.getArgument(1)),
          parseSymbolArgument(call.getArgument(2)));
    } else {
      throw new UnsupportedOperationException("Expected 2 or 3 arguments to S3Method directive");
    }
  }


  private static void parseS4Export(FunctionCall call, NamespaceDirectiveHandler handler) {
    List<String> toExport = new ArrayList<String>();
    for (PairList.Node node : call.getArguments().nodes()) {
      toExport.add(parseStringArgument(node.getValue()));
    }
    handler.exportClasses(toExport);

  }

  private static void parseDynlib(FunctionCall call, NamespaceDirectiveHandler handler) {
    if(call.getArguments().length() < 1) {
      throw new EvalException("Expected at least one argument to useDynlib");
    }
    String libName = parseStringArgument(call.getArgument(0));

    boolean registration = false;
    String fixes = "";

    List<NamespaceDirectiveHandler.DynlibEntry> entries = Lists.newArrayList();
    for(PairList.Node node : Iterables.skip(call.getArguments().nodes(),1)) {
      if(node.hasTag()) {
        if(node.getTag().getPrintName().equals(".registration")) {
          registration = parseLogical(node.getValue());
        } else if(node.getTag().getPrintName().equals(".fixes")) {
          fixes = parseStringArgument(node.getValue());
        } else {
          entries.add(new NamespaceDirectiveHandler.DynlibEntry(
              parseStringArgument(node.getTag()),
              parseStringArgument(node.getValue())));
        }
      } else {
        entries.add(new NamespaceDirectiveHandler.DynlibEntry(
            parseStringArgument(node.getValue())));
      }
    }
    handler.useDynlib(libName, entries, registration, fixes);
  }

  private static boolean parseLogical(SEXP value) {
    if(value instanceof LogicalVector && value.length() == 1) {
      return ((LogicalVector) value).getElementAsRawLogical(0) == 1;
    } else {
      throw new EvalException("Expected TRUE or FALSE");
    }
  }
}
