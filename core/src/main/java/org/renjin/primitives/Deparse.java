/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives;


import com.google.common.collect.Iterables;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.text.ReservedWords;
import org.renjin.sexp.*;

public class Deparse {

  private static final char BACK_TICK = '`';


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

  @Internal
  public static String deparse(@Current Context context, SEXP exp, int widthCutoff, boolean backTick, int options, int nlines) {
    return new DeparsingVisitor(context, exp).getResult();
  }
  
  public static String deparseExp(Context context, SEXP exp) {
    return new DeparsingVisitor(context, exp).getResult();
  }

  private static class DeparsingVisitor extends SexpVisitor<String> {

    private StringBuilder deparsed = new StringBuilder();
    private Context context;

    public DeparsingVisitor(Context context, SEXP exp) {
      this.context = context;
      deparse(exp);
    }
    
    public void deparse(SEXP exp) {
      if(requiresStructure(exp)) {
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
      // function calls are not included in the deparse
      if(exp instanceof FunctionCall) {
         return false;
      }
      return !exp.getAttributes().empty();
    }

    @Override
    public void visit(CHARSEXP charExp) {
      throw new UnsupportedOperationException();
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
    public void visit(ExpressionVector vector) {
      throw new UnsupportedOperationException();
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
      deparseList(pairList.namedValues());
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
      deparseList(list.namedValues());
    }

    private void deparseList(Iterable<NamedValue> list) {
      deparsed.append("list(");
      boolean needsComma = false;
      for(NamedValue namedValue : list) {
        if(needsComma) {
          deparsed.append(", ");
        } else {
          needsComma = true;
        }
        if(namedValue.hasName()) {
          deparsed.append(namedValue.getName()).append(" = ");
        }
        deparse(namedValue.getValue());
      }
      deparsed.append(")");
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
      deparsed.append(((Symbol)call.getFunction()).getPrintName());
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
      deparse(call.getArgument(0));
      deparsed.append("\n}");
    }
    
    private void deparseParen(FunctionCall call) {
      deparsed.append("(");
      deparse(call.getArgument(0));
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
      }
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
          return "Inf";
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
        return "\"" + vector.getElementAsString(index) + "\"";
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
