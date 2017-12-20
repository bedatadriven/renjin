/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;


import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.parser.NumericLiterals;
import org.renjin.parser.StringLiterals;
import org.renjin.primitives.text.ReservedWords;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.escape.Escaper;
import org.renjin.repackaged.guava.escape.Escapers;
import org.renjin.sexp.*;

public class Deparse {

  private static final char BACK_TICK = '`';

  public static int KEEP_INTEGER = 1;
  public static int QUOTE_EXPRESSIONS = 2;
  public static int SHOW_ATTRIBUTES = 4;
  public static int USE_SOURCE = 8;
  public static int WARN_INCOMPLETE = 16;
  public static int DELAY_PROMISES = 32;
  public static int KEEP_NA = 64;
  public static int S_COMPAT = 128;
  public static int HEX_NUMERIC = 256;
  public static int DIGITS_16 = 512;

  public static String[] BINARY_OPS = new String[] {
    "+", "-",  "/",  "*",
    "^",
    "<-", "<<-", "=", 
    "%in%", "%/%", 
    ":", 
    "==", "!=", "<", ">", "<=", ">=",
    "&", "&&", "|", "||",
    "$"
  };
  
  public static String[] BINARY_OPS_WITHOUT_SPACE = new String[] {
    ":", "^", "$"
  };
  
  public static String[] UNARY_OPS = new String[] {
    "!", "-", "+"
  };
  
  public static String[] CONTROL_STATEMENTS = new String[] { "break", "next" };

  public static final Escaper ESCAPER = Escapers.builder()
      .addEscape('\"', "\\\"")
      .addEscape('\n', "\\n")
      .addEscape('\r', "\\r")
      .addEscape('\t', "\\t")
      .addEscape('\b', "\\b")
      .addEscape('\f', "\\f")
      .addEscape('\\', "\\\\")
      .addEscape('`', "\\`")
      .build();


  @Internal
  public static String deparse(@Current Context context, SEXP exp, int widthCutoff, boolean backTick, int options, int nlines) {
    return new DeparsingVisitor(context, options, exp).getResult();
  }
  
  public static String deparseExp(Context context, SEXP exp) {
    return new DeparsingVisitor(context, 0, exp).getResult();
  }

  public static String deparseExpWithAttributes(Context context, SEXP sexp) {
    return new DeparsingVisitor(context, SHOW_ATTRIBUTES, sexp).getResult();
  }

  private static class DeparsingVisitor extends SexpVisitor<String> {

    private StringBuilder deparsed = new StringBuilder();
    private Context context;
    private boolean keepAttributes;

    public DeparsingVisitor(Context context, int options, SEXP exp) {
      this.context = context;
      this.keepAttributes = (options & SHOW_ATTRIBUTES) != 0;
      deparse(exp);
    }

    public void deparse(SEXP exp) {
      if(keepAttributes && requiresStructure(exp)) {
        deparsed.append("structure(");
        exp.accept(this);
        deparseAttributes(exp);
        deparsed.append(")");
      } else {
        exp.accept(this);
      }
    }

    private void deparseAttributes(SEXP exp) {
      for(Symbol name : exp.getAttributes().names()) {
        SEXP value = exp.getAttributes().get(name);
       // ".Dim", ".Dimnames", ".Names", ".Tsp" and ".Label"
       // "dim", "dimnames", "names", "tsp" and "levels".
        if(name == Symbols.DIM) {
          appendAttribute(".Dim", value);
        
        } else if(name == Symbols.DIMNAMES) {
          appendAttribute(".Dimnames", value);
        
        } else if(name == Symbols.NAMES) {
          appendAttribute(".Names", value);
        
        } else if(name == Symbol.get("tsp")) {
          appendAttribute(".Tsp", value);
          
        } else if(name == Symbols.LEVELS) {
          appendAttribute(".Label", value);
        
        } else {
          appendAttribute(name.getPrintName(), value);
        }
      }
    }

    private void appendAttribute(String name, SEXP value) {
      deparsed.append(", ").append(name).append(" = ");
      deparse(value);
    }

    private boolean requiresStructure(SEXP exp) {
      // for perhaps arbitrary reasons, attributes of 
      // function calls and S4 objects are not included in the deparse
      if(exp instanceof FunctionCall || exp instanceof S4Object) {
        return false;
      }

      // and for expression objects, only use structure if there
      // are attributes OTHER than names
      if(exp instanceof ExpressionVector) {
        return exp.getAttributes().hasAnyBesidesName() ||
            hasNamesRequiringSpecialHandling(exp);
      }

      return !exp.getAttributes().isEmpty();
    }

    private boolean hasNamesRequiringSpecialHandling(SEXP exp) {
      AttributeMap attributes = exp.getAttributes();
      if(!attributes.hasNames()) {
        return false;
      }
      StringVector names = attributes.getNames();
      for (int i = 0; i < names.length(); i++) {
        if(names.isElementNA(i)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void visit(CHARSEXP charExp) {
      StringLiterals.appendEscaped(deparsed, charExp.getValue());
    }

    @Override
    public void visit(ComplexVector vector) {
      deparseAtomicVector(vector, ElementDeparser.COMPLEX);
    }
    

    @Override
    public void visit(Environment environment) {
      // this is somewhat random; it's isn't parsable in any case
      deparsed.append("<environment>");
    }

    @Override
    public void visit(BuiltinFunction builtin) {
      visitPrimitive(builtin);
    }


    @Override
    public void visitSpecial(SpecialFunction special) {
      visitPrimitive(special);
    }

    private void visitPrimitive(PrimitiveFunction builtin) {
      deparsed.append(".Primitive(\"" + builtin.getName() + "\")");
    }

    
    @Override
    public void visit(IntVector vector) {
      if(isSequence(vector)) {
        deparsed.append(vector.getElementAsInt(0))
          .append(":").append(vector.getElementAsInt(vector.length()-1));
      } else {
        deparseAtomicVector(vector, ElementDeparser.INTEGER);
      }
    }
    
    /**
     * Checks for the special case in which an IntVector contains
     * a sequence that can be expressed as x:y
     */
    private boolean isSequence(IntVector vector) {
      if(vector.length() < 2) {
        return false;
      }
      
      int start = vector.getElementAsInt(0);
      int end = vector.getElementAsInt(vector.length()-1);
      int step = (start < end) ? 1 : -1;
      int expected = start+step;
      for(int i=1;i!=vector.length();++i) {
        if(vector.getElementAsInt(i)!=expected) {
          return false;
        }
        expected += step;
      }
      return true;
    }

    @Override
    public void visit(PairList.Node pairList) {
      deparseList("pairlist", pairList.namedValues());
    }

    @Override
    public void visit(Null nullExpression) {
      deparsed.append("NULL");
    }

    @Override
    public void visit(PrimitiveFunction primitive) {
      super.visit(primitive);
    }

    @Override
    public void visit(Promise promise) {
      deparse(promise.getExpression());
    }

    @Override
    public void visit(DoubleVector vector) {
      deparseAtomicVector(vector, ElementDeparser.DOUBLE);
    }

    @Override
    public void visit(StringVector vector) {
      deparseAtomicVector(vector, ElementDeparser.STRING);
    }

    @Override
    public void visit(LogicalVector vector) {
      deparseAtomicVector(vector, ElementDeparser.LOGICAL);
    }

    public void visit(ListVector list) {
      deparseList("list", list.namedValues());
    }

    @Override
    public void visit(ExpressionVector vector) {
      // Special handling because elements of the expression
      // are always deparsed WITHOUT attributes

      deparsed.append("expression(");
      boolean needsComma = false;
      for(NamedValue namedValue : vector.namedValues()) {
        if(needsComma) {
          deparsed.append(", ");
        } else {
          needsComma = true;
        }
        maybeAppendArgumentName(namedValue, "`NA`");
        DeparsingVisitor elementVisitor = new DeparsingVisitor(context, 0, namedValue.getValue());
        deparsed.append(elementVisitor.getResult());
      }
      deparsed.append(")");
    }

    private void deparseList(final String listType, Iterable<NamedValue> list) {
      deparsed.append(listType + "(");
      boolean needsComma = false;
      for(NamedValue namedValue : list) {
        if(needsComma) {
          deparsed.append(", ");
        } else {
          needsComma = true;
        }
        maybeAppendArgumentName(namedValue, "\"NA\"");
        deparse(namedValue.getValue());
      }
      deparsed.append(")");
    }

    private void maybeAppendArgumentName(NamedValue namedValue, String naNameLiteral) {
      if(namedValue.hasName()) {
        String name = namedValue.getName();
        if(StringVector.isNA(name)) {
          // This is not actually correct - for example list("NA" = 1) will
          // evaluate to the a list with a names vector containing the *string* "NA", *not* NA_character_
          // However, there is actually no way to specify an NA name using this syntax, so we will
          // just abide by the convention used by GNU R. If "showAttributes" is enabled, then the structure() call
          // will include the correct representation of NA which will be used during evaluation.
          deparsed.append(naNameLiteral);
        } else {
          deparsed.append(name);
        }
        deparsed.append(" = ");
      }
    }

    @Override
    public void visit(Closure closure) {
      deparseFunction(closure.getFormals(), closure.getBody());
    }


    @Override
    public void visit(FunctionCall call) {
      if(call.getFunction() instanceof Symbol) {
        String name = ((Symbol)call.getFunction()).getPrintName();
        if(name.equals("if")) {
          deparseIf(call);
        } else if(name.equals("for")) {
          deparseFor(call);
        } else if(name.equals("while")) {
          deparseWhile(call);
        } else if(name.equals("repeat")) {
          deparseRepeat(call);
        } else if(name.equals("function")) {
          deparseFunction(call.getArgument(0), call.getArgument(1));
        } else if(name.equals("{")) {
          deparseBracket(call);
        } else if(name.equals("(")) {
          deparseParen(call);
        } else if(is(name, CONTROL_STATEMENTS)) {
          deparseControlStatement(name);
        } else if(name.startsWith("%") && name.endsWith("%")) {
          deparseUserInfixOp(name, call);
        } else if(call.getArguments().length() == 2 && is(name, BINARY_OPS)) {
          deparseBinaryOp(name, call.getArguments());
        } else if(call.getArguments().length() == 1 && is(name, UNARY_OPS)) {
          deparseUnaryOp(call);
        } else if(isSubset(name)) {
          deparseSubset(name, call.getArguments());
        } else if(name.equals("~")) {
          deparseTilde(call);
        } else {
          deparseNormalCall(call);
        }
      } else {
        deparseNormalCall(call);
      }
    }

    private void deparseUserInfixOp(String name, FunctionCall call) {
      if(call.getArguments().length() == 2) {
        // only if the function has exactly two arguments do we treat it as an infix operator
        deparse(call.getArgument(0));
        deparsed.append(" ").append(name).append(" ");
        deparse(call.getArgument(1));
      } else {
        // otherwise it must be backticked
        deparsed.append("`").append(name).append("`");
        deparsed.append("(");
        deparseArgumentList(call.getArguments().nodes());
        deparsed.append(")");
      }
    }


    private void deparseFunction(PairList formals, SEXP body) {
      deparsed.append("function (");
      deparseFormals(formals);
      deparsed.append(") ");
      body.accept(this);
    }

    public void deparseFormals(PairList formals) {
      boolean needsComma = false;
      for (PairList.Node node : formals.nodes()) {
        if(needsComma) {
          deparsed.append(", ");
        }
        node.getTag().accept(this);

        if(node.getValue() != Symbol.MISSING_ARG) {
          deparsed.append(" = ");
          node.getValue().accept(this);
        }

        needsComma = true;
      }
    }


    /**
     * Deparses 'break' and 'next' statements.
     */
    private void deparseControlStatement(String name) {
      deparsed.append(name);
    }

    private void deparseSubset(String name, PairList arguments) {
      deparse(arguments.getElementAsSEXP(0));
      deparsed.append(name);
      deparseArgumentList(Iterables.skip(arguments.nodes(), 1));
      deparsed.append(closingParens(name));
    }

    private void deparseTilde(FunctionCall call) {
      PairList arguments = call.getArguments();
      if(arguments.length() == 1) {
        deparsed.append("~");
        deparse(arguments.getElementAsSEXP(0));
      } else if(arguments.length() == 2) {
        deparse(arguments.getElementAsSEXP(0));
        deparsed.append(" ~ ");
        deparse(arguments.getElementAsSEXP(1));
      } else {
        deparseNormalCall(call);
      }
    }
    
    private String closingParens(String name) {
      if(name.equals("[")) {
        return "]";
      } else if(name.equals("[[")) {
        return "]]";
      } else {
        throw new IllegalArgumentException(name);
      }
    }

    private void deparseUnaryOp(FunctionCall call) {
      deparsed.append(((Symbol) call.getFunction()).getPrintName());
      deparse(call.getArgument(0));
    }

    private void deparseBinaryOp(String name, PairList arguments) {
      deparse(arguments.getElementAsSEXP(0));
      if(is(name, BINARY_OPS_WITHOUT_SPACE)) {
        deparsed.append(name);
      } else {
        deparsed.append(' ').append(name)
          .append(' ');
      }
      deparse(arguments.getElementAsSEXP(1));
    }

    private void deparseBracket(FunctionCall call) {
      deparsed.append("{\n");
      for (SEXP statement : call.getArguments().values()) {
        deparse(statement);
        deparsed.append("\n");
      }
      deparsed.append("}");
    }
    
    private void deparseParen(FunctionCall call) {
      deparsed.append("(");
      if(call.getArguments().length() == 0) {
        deparsed.append("NULL");
      } else {
        deparse(call.getArgument(0));
      }
      deparsed.append(")");
    }
    
    private boolean isSubset(String name) {
      return name.startsWith("[");
    }
    
    private boolean is(String name, String[] names) {
      for(String opName : names) {
        if(opName.equals(name)) {
          return true;
        }
      }
      return false;
    }

    private void deparseNormalCall(FunctionCall call) {
      if(call.getFunction() instanceof Function) {
        deparsed.append("FUN");
      } else {
        deparse(call.getFunction());
      }
      deparsed.append("(");
      deparseArgumentList(call.getArguments().nodes());
      deparsed.append(")");
    }

    private void deparseArgumentList(Iterable<PairList.Node> arguments) {
      boolean needsComma = false;
      for(PairList.Node argument : arguments) {
        if(needsComma) {
          deparsed.append(", ");
        } else {
          needsComma = true;
        }
        if(argument.hasTag()) {
          argument.getTag().accept(this);
          deparsed.append(" = ");
        }
        deparse(argument.getValue());
      }
    }

    private void deparseIf(FunctionCall call) {
      deparsed.append("if (");
      deparse(call.getArgument(0));
      deparsed.append(") ");
      deparse(call.getArgument(1));
      if(call.getArguments().length() == 3) {
        deparsed.append(" else ");
        deparse(call.getArgument(2));
      }
    }
    
    private void deparseRepeat(FunctionCall call) {
      deparsed.append("repeat ");
      deparse(call.getArgument(0));
    }
    
    private void deparseFor(FunctionCall call) {
      deparsed.append("for(");
      deparse(call.getArgument(0));
      deparsed.append(" in ");
      deparse(call.getArgument(1));
      deparsed.append(") ");
      deparse(call.getArgument(2));
    }
    
    private void deparseWhile(FunctionCall call) {
      deparsed.append("while (");
      deparse(call.getArgument(0));
      deparsed.append(") ");
      deparse(call.getArgument(1));
    }
    
    @Override
    public void visit(Symbol symbol) {
      if(symbol != Symbol.MISSING_ARG) {
        String name = symbol.getPrintName();
        if(Symbols.isValid(name) && !ReservedWords.isReserved(name)) {
          deparsed.append(name);
        } else {
          deparsed.append(BACK_TICK).append(name).append(BACK_TICK);
        }
      } else {
        deparsed.append("");
      }
    }

    @Override
    public void visit(S4Object s4Object) {
      // Expected form: <S4 object of class structure("hash", package = "hash")>>

      deparsed.append("<S4 object of class ");
      deparse(s4Object.getAttribute(Symbols.CLASS));
      deparsed.append(">");
    }

    protected void unhandled(SEXP exp) {
      // TODO: this is just a fallback for missing impl
      deparsed.append(exp.toString());
    }

    @Override
    public String getResult() {
      return deparsed.toString();
    }

    public <T> void deparseAtomicVector(AtomicVector vector, ElementDeparser deparser) {
      if(vector.length() == 0) {
        deparsed.append(deparser.deparseEmpty());
      } else if(vector.length() == 1) {
        if(vector.isElementNA(0)) {
          deparsed.append(deparser.typedNaLiteral());
        } else {
          deparsed.append(deparser.deparse(vector, 0));
        }
      } else {
        String naLiteral = computeNALiteral(vector, deparser);
        deparsed.append("c(");
        for(int i=0; i!=vector.length();++i) {
          if(i > 0) {
            deparsed.append(", ");
          }
          if(vector.isElementNA(i)) {
            deparsed.append(naLiteral);
          } else {
            deparsed.append(deparser.deparse(vector, i));
          }
        }
        deparsed.append(")");
      }
    }
  }
  
  /**
   * If NA values are mixed with non-NA values in an AtomicVector,
   * we can just use the simple "NA" constant. But if the vector ONLY
   * contains NA values, then we have to use the typed NA literals otherwise
   * the result of c() will be logical.
   */
  private static String computeNALiteral(Vector x, ElementDeparser deparser) {
    if(allNA(x)) {
      return deparser.typedNaLiteral();
    } else {
      return "NA";
    }
  }
  
  private static boolean allNA(Vector x) {
    for(int i=0;i!=x.length();++i) {
      if(!x.isElementNA(i)) {
        return false;
      }
    }
    return true;
  }
  
  private enum ElementDeparser {
   
    LOGICAL {
      @Override
      String deparse(Vector vector, int index) {
        return vector.getElementAsRawLogical(index) == 1 ? "TRUE" : "FALSE";
      }

      @Override
      String typedNaLiteral() {
        return "NA";
      }

      @Override
      String deparseEmpty() {
        return "logical(0)";
      }
    },
    INTEGER {
      @Override
      String deparse(Vector vector, int index) {
        return vector.getElementAsInt(index) + "L";
      }

      @Override
      String typedNaLiteral() {
        return "NA_integer_";
      }

      @Override
      String deparseEmpty() {
        return "integer(0)";
      }
    },
    DOUBLE {
      @Override
      String deparse(Vector vector, int index) {
        double value = vector.getElementAsDouble(index);
        if(Double.isNaN(value)) {
          return "NaN";
        } else if(Double.isInfinite(value)) {
          if(value < 0) {
            return "-Inf";
          } else {
            return "Inf";
          }
        } else {
          return NumericLiterals.toString(value);
        }
      }

      @Override
      String typedNaLiteral() {
        return "NA_real_";
      }

      @Override
      String deparseEmpty() {
        return "numeric(0)";
      }
    },
    COMPLEX {
      @Override
      String deparse(Vector vector, int index) {
        Complex complex = vector.getElementAsComplex(index);
        double r = complex.getReal();
        double i = complex.getImaginary();
        if(DoubleVector.isFinite(r) && DoubleVector.isFinite(i)) {
          StringBuilder sb = new StringBuilder();
          sb.append(NumericLiterals.toString(complex.getReal()));
          if(complex.getImaginary() >= 0 || Double.isNaN(complex.getImaginary())) {
            sb.append("+");
          }
          sb.append(NumericLiterals.toString(complex.getImaginary())).append("i");
          return sb.toString();
        } else {
          return String.format("complex(real=%s, i=%s)",
              NumericLiterals.format(r, "NA"),
              NumericLiterals.format(i, "NA"));
        }
      }

      @Override
      String typedNaLiteral() {
        return "NA_complex_";
      }

      @Override
      String deparseEmpty() {
        return "complex(0)";
      }
    },
    
    STRING {
      @Override
      String deparse(Vector vector, int index) {
        return "\"" + ESCAPER.escape(vector.getElementAsString(index)) + "\"";
      }

      @Override
      String typedNaLiteral() {
        return "NA_character_";
      }
      
      @Override
      String deparseEmpty() {
        return "character(0)";
      }
    };
    
    
    abstract String deparse(Vector vector, int index);
    
    abstract String typedNaLiteral();
    
    abstract String deparseEmpty();
  }
}
