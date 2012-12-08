package org.renjin.gcc.gimple;

import com.google.common.collect.Lists;

import org.renjin.gcc.CallingConvention;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.*;

import polyglot.ast.Typed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class GimpleParser {


  private static final String FORTRAN_STRING_PREFIX = "&\"";
  private static final String FORTRAN_STRING_SUFFIX = "\"[1]{lb: 1 sz: 1}";
  private GimpleFunction currentFunction;
  private GimpleBasicBlock currentBB;
  private CallingConvention callingConvention;
  

  public GimpleParser(CallingConvention callingConvention) {
    super();
    this.callingConvention = callingConvention;
  }

  public List<GimpleFunction> parse(Reader reader) throws IOException {
    return parse(new BufferedReader(reader));
  }

  public List<GimpleFunction> parse(BufferedReader reader) throws IOException {

    List<GimpleFunction> functions = Lists.newArrayList();

    String line;
    currentFunction = null;
    currentBB = null;

    while((line = reader.readLine()) != null) {

      line = line.trim();
      if(line.isEmpty() || isComment(line)) {
        continue;
      }
      try {
  
        if(currentFunction == null) {
          // expect function declaration
          // but also stray debug output like 'Analyzing Edge Insertions' from
          // some earlier versions of GCC
          if(line.contains("(") && line.contains(")"))  {
            currentFunction = parseFunctionDeclaration(line);
            functions.add(currentFunction);
            assertNextLine(reader, "{");
          }
        } else if(line.equals("}")) {
          currentFunction = null;
        } else {
            parseFunctionBodyLine(reader, line);
        }
      } catch(Exception e) {
        if(currentFunction == null) {
          throw new RuntimeException("Exception parsing line '" + line + "' at top level", e);
        } else {
          throw new RuntimeException("Exception parsing line '" + line + "' in function '" + currentFunction.getName() + "'", e);
        }
      }
    }
    return functions;
  }

  private void parseFunctionBodyLine(BufferedReader reader, String line) throws IOException {
    // function body
    if(isLabel(line)) {
      currentBB = currentFunction.addBasicBlock(parseLabel(line));
    } else if(line.startsWith("gimple_assign")) {
      currentBB.addIns(parseAssign(line));
      //  gimple_assign <indirect_ref, D.6853_34, *n_29(D), NULL>
    } else if(line.startsWith("goto")) {
      currentBB.addIns(parseGoto(line));
    } else if(line.startsWith("gimple_cond")) {
      currentBB.addIns(parseCond(line, reader));
    } else if(line.startsWith("gimple_call")) {
      currentBB.addIns(parseCall(line));
    } else if(line.startsWith("gimple_return")) {
      currentBB.addIns(parseReturn(line));
    } else if(line.startsWith("gimple_switch")) {
      currentBB.addIns(parseSwitch(line));
    } else if(line.startsWith("gimple_label")) {
      currentBB.addIns(parseLabelIns(line));
    } else if(line.startsWith("#")) {
      // ignore
    } else if(line.startsWith("static ")) {
      currentFunction.addVarDecl(parseStaticVarDecl(line));
    } else {
      currentFunction.addVarDecl(parseVarDecl(line, null));
    }
  }

  private GimpleVarDecl parseStaticVarDecl(String line) {
    int equals = line.lastIndexOf('=');
    String varDecl = line.substring("static ".length(), equals).trim();
    String constant = line.substring(equals+1, line.lastIndexOf(';')).trim();

    return parseVarDecl(varDecl, parseNumericConstant(constant).getValue());
  }

  private GimpleIns parseLabelIns(String line) {
    //gimple_label <<L0>>
    String[] args = parseInsArguments(line);
    GimpleLabel target = new GimpleLabel(args[0].substring(1, args[0].length()-1));
    return new GimpleLabelIns(target);
  }

  private GimpleLabel parseSwitchLabel(String branch) {
    int labelStart = branch.indexOf('<');
    int labelEnd = branch.indexOf('>', labelStart+1);
    return new GimpleLabel(branch.substring(labelStart+1, labelEnd));
  }

  private int parseSwitchValue(String branch) {
    int start = "case ".length();
    int end = branch.indexOf(':');
    return Integer.parseInt(branch.substring(start, end));
  }

  private GimpleIns parseSwitch(String line) {
    String[] args = parseInsArguments(line);
    GimpleExpr expr = parseExpr(args[0]);

    List<GimpleSwitch.Branch> branches = Lists.newArrayList();
    for(int i=1;i!=args.length;++i) {
      if(args[i].startsWith("default:")) {
        branches.add(new GimpleSwitch.Branch(GimpleSwitch.DEFAULT, parseSwitchLabel(args[i])));
      } else if(args[i].startsWith("case ")) {
        branches.add(new GimpleSwitch.Branch(parseSwitchValue(args[i]), parseSwitchLabel(args[i])));
      }
    }

    return new GimpleSwitch(expr, branches);
  }

  private GimpleVarDecl parseVarDecl(String line, Object constantValue) {
    // strip final ;
    int terminatingSemi = line.indexOf(';');
    if(terminatingSemi != -1) {
      line = line.substring(0, terminatingSemi);
    }

    // split into tokens
    int identifierStart = line.lastIndexOf(' ')+1;
    String typeDecl = line.substring(0, identifierStart-1).trim();
    GimpleType type = parseType(typeDecl);

    String identifier = line.substring(identifierStart);
    if(identifier.indexOf('[') != -1) {
      if(constantValue == null) {
        throw new UnsupportedOperationException("array type decl without constant value provided: " + line);
      }
      identifier = identifier.substring(0, identifier.indexOf('['));
      type = new PointerType(type);
    }
    
    return new GimpleVarDecl(type, identifier, constantValue);
  }

  private GimpleIns parseReturn(String line) {
    GimpleExpr value = parseExpr(parseInsArguments(line)[0]);
    return new GimpleReturn(value);
  }

  protected GimpleConditional parseCond(String line, BufferedReader reader)
          throws IOException {
//		gimple_cond <lt_expr, D.6854_37, nn2_35, NULL, NULL>
//	      goto <bb 7>;	
//	    else
//	      goto <bb 8>;

    String trueBranch = reader.readLine();
    String elseKeyword = reader.readLine().trim();
    String falseBranch = reader.readLine();

    String[] arguments = parseInsArguments(line);

    GimpleConditional cond = new GimpleConditional();
    cond.setOperator(parseOp(arguments[0]));
    cond.setOperands( parseOperands(arguments, 1 ));
    cond.setTrueTarget( parseGoto(trueBranch).getTarget() );
    cond.setFalseTarget( parseGoto(falseBranch).getTarget() );
    return cond;
  }

  private GimpleAssign parseAssign(String line) {
    //  gimple_assign <indirect_ref, D.6853_34, *n_29(D), NULL>
    String[] arguments = parseInsArguments(line);
    List<GimpleExpr> operands = parseOperands(arguments, 2);
    return new GimpleAssign(parseOp(arguments[0]), parseLValue(arguments[1]), operands);
  }

  private GimpleOp parseOp(String string) {
    return GimpleOp.valueOf(string.toUpperCase());
  }

  private List<GimpleExpr> parseOperands(String[] arguments, int start) {
    List<GimpleExpr> operands = Lists.newArrayList();
    for(int i=start;i<arguments.length;++i) {
      operands.add(parseExpr(arguments[i]));
    }
    return operands;
  }

  private String[] parseInsArguments(String line) {
    int start = line.indexOf('<')+1;
    int end = line.lastIndexOf('>');
    String[] arguments = line.substring(start,end).split("\\s*,\\s*");
    return arguments;
  }

  private Goto parseGoto(String line) {
    GimpleLabel target = new GimpleLabel(parseInsArguments(line)[0]);
    return new Goto(target);
  }

  private GimpleExpr parseExpr(String text) {
    if(text.equals("NULL")) {
      return GimpleNull.INSTANCE;
    } else if(text.startsWith("&\"") && text.endsWith("\"[0]")) {
      return parseStringConstant(text, "&\"", "\"[0]");
    } else if(text.startsWith(FORTRAN_STRING_PREFIX) && text.endsWith(FORTRAN_STRING_SUFFIX)) {
      return parseFortranStringConstant(text);
    } else if(text.startsWith("\"") && text.endsWith("\"")) {
      return parseStringConstant(text, "\"", "\"");
    } else if(text.startsWith("&")) {
      return parseAddressOf(text);
    } else if(Character.isDigit(text.charAt(0)) || text.charAt(0)=='-') {
      return parseNumericConstant(text);
    } else if(text.contains("->")) {  
      return parseCompoundArrowRef(text);
    } else if(text.startsWith("*")) {
      return parseIndirection(text);
    } else {
      return parseName(text);
    }
  }
  


  private GimpleExpr parseIndirection(String text) {
    GimpleExpr pointerExpr = parseExpr(text.substring("*".length()));
    return new GimpleIndirection(pointerExpr);
  }

  private GimpleExpr parseAddressOf(String text) {
    GimpleExpr expr = parseExpr(text.substring("&".length()));
    return new GimpleAddressOf(expr);
  }

  private GimpleExpr parseCompoundArrowRef(String text) {
    int arrow = text.indexOf("->");
    GimpleVar var;
    String varName = text.substring(0, arrow);
    if(isSsaName(varName)) {
      var = parseSsaName(varName);
    } else if(isLocalVariable(varName)) {
      var = parseLocalVariable(varName);
    } else {
      throw new UnsupportedOperationException("While trying to parse compound ref '" + text + "'," +
              " could not figure out what to do with variable name '" + varName + "'");
    }
    String field = text.substring(arrow+"->".length());
    return new GimpleCompoundRef(var, field);
  }

  private GimpleExpr parseStringConstant(String text, String prefix, String suffix) {
    String str = text.substring(prefix.length(), text.length()-suffix.length());
    return new GimpleConstant(str);
  }
  
  private GimpleExpr parseFortranStringConstant(String text) {
    String str = text.substring(FORTRAN_STRING_PREFIX.length(), text.length() - FORTRAN_STRING_SUFFIX.length());
    return new GimpleConstant(str);
  }

  private GimpleConstant parseNumericConstant(String text) {
    if(text.equals("0B")) {
      // TODO: not sure if this is correct
      return new GimpleConstant(0);
    } else if(text.startsWith("{")) {
      return parseConstantArray(text);
    } else if(text.indexOf('.') != -1) {
      return new GimpleConstant(Double.parseDouble(text));
    } else {
      return new GimpleConstant(Integer.parseInt(text));
    }
  }

  private GimpleConstant parseConstantArray(String text) {
   if(text.indexOf('.') != -1) {
     String[] values = text.substring("{".length(), text.lastIndexOf('}')).split("\\s*,\\s*");
     double [] dvalues = new double[values.length];
     for(int i=0;i!=values.length;++i) {
       dvalues[i] = Double.parseDouble(values[i]);
     }
     return new GimpleConstant(dvalues);
   } else {
     throw new UnsupportedOperationException(text);
   }
   
  }

  private String stripNameDecorators(String name) {
    int originStart = name.indexOf('(');
    if(originStart != -1) {
      name = name.substring(0, originStart);
    }
    if(name.startsWith("*")) {
      throw new UnsupportedOperationException();
    }
    return name;
  }

  private GimpleExpr parseName(String text) {
    if(isArrayRef(text)) {
      return parseArrayRef(text);
    } else if(text.startsWith("*")) {
      return parseIndirection(text);
    }

    String cleanText = stripNameDecorators(text);
    if(isCompoundRef(cleanText)) {
      return parseCompoundRef(cleanText);
    } else if(isSsaName(cleanText)) {
      return parseSsaName(cleanText);
    } else if(isLocalVariable(cleanText)) {
      return parseLocalVariable(cleanText);
    } else {
      return new GimpleExternal(text);
    }
  }

  private GimpleExpr parseArrayRef(String cleanText) {
    int bracket = cleanText.lastIndexOf('[');
    String index = cleanText.substring(bracket+1, cleanText.lastIndexOf(']'));
    GimpleExpr indexExpr = parseExpr(index);

    String name = stripEnclosingParens(cleanText.substring(0, bracket));
    if(name.startsWith("*")) {
      throw new UnsupportedOperationException();
    }
    GimpleExpr var = parseName(name);
    if(!(var instanceof GimpleVar)) {
      throw new UnsupportedOperationException(var.toString() + " [" + var.getClass().getSimpleName() + "]");
    }
    return new GimpleArrayRef((GimpleVar)var, indexExpr);
  }

  private String stripEnclosingParens(String s) {
    String trimmed = s.trim();
    if(trimmed.startsWith("(") && trimmed.endsWith(")")) {
      return trimmed.substring(1,trimmed.length()-1).trim();
    } else {
      return trimmed;
    }
  }

  private boolean isArrayRef(String text) {
    return text.matches(".*\\[\\S+\\]");
  }

  private GimpleLValue parseLValue(String text) {
    GimpleExpr name = parseName(text);
    if(name instanceof GimpleLValue) {
      return (GimpleLValue)name;
    } else {
      throw new UnsupportedOperationException("Expected lvalue: " + text);
    }
  }

  private boolean isCompoundRef(String text) {
    int dot = text.indexOf('.');
    if(dot == -1) {
      return false;
    }
    String varName = text.substring(0, dot);
    if(!currentFunction.hasVariable(varName)) {
      return false;
    }
    GimpleType variableType = currentFunction.getVariableType(varName);
    return variableType instanceof GimpleStructType;
  }

  private GimpleCompoundRef parseCompoundRef(String text) {
    int dot = text.indexOf('.');
    String varName = text.substring(0, dot);
    String field = text.substring(dot+1);
    return new GimpleCompoundRef(new GimpleVar(varName), field);
  }

  private boolean isSsaName(String text) {
    text = stripNameDecorators(text);
    int underscore = text.lastIndexOf('_');
    if(underscore == -1) {
      return false;
    }
    String varName = text.substring(0, underscore);
    return currentFunction.hasVariable(varName);
  }

  private GimpleVar parseSsaName(String ssaName) {
    ssaName = stripNameDecorators(ssaName);
    int underscore = ssaName.lastIndexOf('_');
    if(underscore == -1) {
      throw new AssertionError("Expecting ssa name: " + ssaName);
    }
    String name = ssaName.substring(0, underscore);
    int version = Integer.parseInt(ssaName.substring(underscore+1));
    return new GimpleVar(name, version);
  }

  private boolean isLocalVariable(String text) {
    return currentFunction.hasVariable(text);
  }

  private GimpleVar parseLocalVariable(String text) {
    return new GimpleVar(stripNameDecorators(text).trim());
  }

  private GimpleCall parseCall(String line) {
    String[] arguments = parseInsArguments(line);
    List<GimpleExpr> operands = parseOperands(arguments, 2);
    if(arguments[1].equals("NULL")) {
      return new GimpleCall(parseExpr(arguments[0]), null, operands);
    } else {
      return new GimpleCall(parseExpr(arguments[0]), parseLValue(arguments[1]), operands);
    }
  }

  private void assertNextLine(BufferedReader reader, String expected) throws IOException {
    String line = reader.readLine();
    if(line == null) {
      throw new GimpleParseException("Unexpected EOF, expected '" + expected + "'");
    }
  }

  private GimpleFunction parseFunctionDeclaration(String line) {
    int paramListStart = line.indexOf('(');
    String name = line.substring(0, paramListStart);
    GimpleFunction fn = new GimpleFunction(callingConvention.mangleFunctionName(name.trim()), callingConvention);

    int paramListEnd = line.lastIndexOf(')');
    String paramList = line.substring(paramListStart+1, paramListEnd).trim();
    if(!paramList.isEmpty()) {
      String params[] = paramList.split("\\s*,\\s*");
      for(String param : params) {
        fn.addParameter(parseParameter(param));
      }
    }

    return fn;
  }

  private GimpleParameter parseParameter(String paramDecl) {
    int nameStart = paramDecl.lastIndexOf(' ');
    String typeDecl = paramDecl.substring(0, nameStart).trim();
    String name = paramDecl.substring(nameStart+1);
    return new GimpleParameter(parseType(typeDecl), name);
  }

  private GimpleType parseType(String typeDecl) {

    // ignore the restrict keyword.
    // it's not clear from the docs what it means
    // but for our purposes it doesn't seem to matter
    typeDecl = typeDecl.replaceAll("restrict", "").trim();

    // we may also see additional information about the length
    // of the array provided by the fortran compiler, but we're
    // going to ignore that too:
    if(typeDecl.matches(".*\\[\\S+:(\\S+)?\\]")) {
      int lastBracket = typeDecl.lastIndexOf('[');
      typeDecl = typeDecl.substring(0, lastBracket).trim();
    }

    if(typeDecl.endsWith("*") || typeDecl.endsWith("&")) {
      return new PointerType(parseType(typeDecl.substring(0, typeDecl.length()-1).trim()));
    } else if(typeDecl.equals("double") || typeDecl.equals("real(kind=8)")) {
      return PrimitiveType.DOUBLE_TYPE;
    } else if(typeDecl.equals("float")) {
      return PrimitiveType.FLOAT_TYPE;
    } else if(typeDecl.equals("int") ||
            typeDecl.equals("integer(kind=4)") ||
            typeDecl.equals("character(kind=4)")) {
      return PrimitiveType.INT_TYPE;

    } else if(typeDecl.equals("integer(kind=8)")) {
      return PrimitiveType.LONG;
      
    } else if(typeDecl.equals("long int")) {
      return PrimitiveType.LONG;
      
    } else if(typeDecl.equals("real(kind=4)")) {
      return PrimitiveType.FLOAT_TYPE;
      
    } else if(typeDecl.equals("char") || typeDecl.equals("const char")) {
      return PrimitiveType.CHAR;

    } else if( typeDecl.equals("long unsigned int") ||
            typeDecl.equals("<unnamed-unsigned:64>") ||
            typeDecl.equals("bit_size_type") || 
            typeDecl.equals("unsigned int")) {
      // TODO: we're treating unsigned 64-bit integers as java its because to this point these
      // types represent pointers, and even on 64-bit platform, java arrays are only addressable to 31-bits
      // but this isn't something that should be addressed here at the parser level.
      return PrimitiveType.INT_TYPE;
      

    } else if(typeDecl.equals("_Bool") || typeDecl.equals("logical(kind=4)")) {
      return PrimitiveType.BOOLEAN;
    } else if(typeDecl.equals("void")) {
      return PrimitiveType.VOID_TYPE;
    } else if(isFunctionPointerTypeDecl(typeDecl)) {
      return parseFunctionPointerType(typeDecl);
    } else if(typeDecl.startsWith("struct ")) {
      return parseStructType(typeDecl);
    } else {
      throw new UnsupportedOperationException("cannot parse '" + typeDecl + "' type declaration yet");
    }
  }

  private GimpleType parseStructType(String typeDecl) {
    String structName = typeDecl.substring("struct ".length()).trim();
    return new GimpleStructType(structName);

  }

  private boolean isFunctionPointerTypeDecl(String typeDecl) {
    return typeDecl.matches(".+\\(\\*.*\\) \\(.*\\)");
  }

  private GimpleType parseFunctionPointerType(String typeDecl) {
    // double (*<T573>) (double)

    int firstParen = typeDecl.indexOf('(');
    GimpleType returnType = parseType(typeDecl.substring(0, firstParen).trim());

    int firstClauseEnd = typeDecl.indexOf(')', firstParen+1);
    String firstClause = typeDecl.substring(firstParen+1, firstClauseEnd).trim();
    if(!firstClause.startsWith("*")) {
      throw new IllegalArgumentException("first clause: " + firstClause);
    }

    int secondClauseStart = typeDecl.indexOf('(', firstClauseEnd+1);
    int secondClauseEnd = typeDecl.lastIndexOf(')');
    String secondClause = typeDecl.substring(secondClauseStart+1, secondClauseEnd).trim();

    String[] argumentTypes = secondClause.split(",");
    List<GimpleType> arguments = Lists.newArrayList();
    for(String argument : argumentTypes) {
      arguments.add(parseType(argument.trim()));
    }
    return new FunctionPointerType(returnType, arguments);
  }

  private boolean isComment(String line) {
    return line.startsWith(";;");
  }

  private boolean isLabel(String line) {
    // labels take the form " <bb 33>: "
    return line.matches("<bb \\d+>:");
  }

  private String parseLabel(String line) {
    if(!isLabel(line)) {
      throw new GimpleParseException("Expected label declaration, found: '" + line + "'");
    }
    return line.substring(1, line.length()-2);
  }
}
