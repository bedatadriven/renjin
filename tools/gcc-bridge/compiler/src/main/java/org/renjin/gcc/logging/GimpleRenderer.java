/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.logging;

import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.guava.html.HtmlEscapers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GimpleRenderer {

  private SymbolTable symbolTable;
  private final StringBuilder html = new StringBuilder();

  private final Map<String, GimpleRecordTypeDef> typedefMap;
  private final Map<String, String> recordNameMap = new HashMap<>();

  public GimpleRenderer(SymbolTable symbolTable, GimpleCompilationUnit unit) {
    this.symbolTable = symbolTable;

    typedefMap = new HashMap<>();
    for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
      typedefMap.put(recordTypeDef.getId(), recordTypeDef);
    }

    abbreviateIds();


  }

  private void abbreviateIds() {
    for (GimpleRecordTypeDef typeDef : typedefMap.values()) {
      if(typeDef.getName() != null) {
        recordNameMap.put(typeDef.getId(), typeDef.getName());
      } else {
        int abbrLength = 4;
        while(!uniqueEnough(typeDef.getId(), abbrLength)) {
          abbrLength ++;
        }

        recordNameMap.put(typeDef.getId(), "anonymous_" +
            typeDef.getId().substring(typeDef.getId().length() - abbrLength));
      }
    }
  }

  private boolean uniqueEnough(String id, int length) {

    if(id.length() <= length) {
      return true;
    }

    String abbr = id.substring(id.length() - length);

    for (String otherId : typedefMap.keySet()) {
      if(!id.equals(otherId) && otherId.endsWith(abbr)) {
        return false;
      }
    }
    return true;
  }

  private void appendEscaped(String text) {
    html.append(HtmlEscapers.htmlEscaper().escape(text));
  }

  private void appendHtml(String html) {
    this.html.append(html);
  }

  public String renderFunction(GimpleFunction function) {
    appendHtml("<div>\n");

    appendHtml("<div class=\"funcdecl\">");
    appendEscaped(function.getReturnType().toString());
    appendHtml(" ");
    appendHtml(String.format("<span title=\"%s\">%s</span>",
        function.getMangledName(),
        function.getName()));
    symbol("(");
    for (int i = 0; i < function.getParameters().size(); i++) {
      if(i > 0) {
        symbol(", ");
      }
      GimpleParameter param = function.getParameters().get(i);
      appendHtml(renderType(param.getType(), false));
      appendHtml(" ");
      if(param.getName() == null) {
        appendEscaped("P" + param.getId());
      } else {
        appendEscaped(param.getName());
      }
    }
    symbol(")");
    appendHtml("</div>");

    for (GimpleVarDecl gimpleVarDecl : function.getVariableDeclarations()) {
      if(gimpleVarDecl.getValue() != null) {
        html.append("<div class=\"vardecl\">");
        html.append(gimpleVarDecl.getName());
        html.append(" = ");
        expr(gimpleVarDecl.getValue());
        html.append("</div>");
      }
    }

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      html.append("<div class=\"bb\">\n");
      html.append("<div class=\"bblabel\">");
      html.append(basicBlock.getName() + ":");
      html.append("</div>\n");
      for (GimpleStatement statement : basicBlock.getStatements()) {
        renderStatement(statement);
      }
      html.append("</div>");
    }
    html.append("</div>");
    return html.toString();
  }

  public String renderRecords() {

    for (GimpleRecordTypeDef typeDef : typedefMap.values()) {
      html.append("<h2 id=\"").append(typeDef.getId()).append("\">");
      html.append(recordName(typeDef));
      html.append("</h2>");

      html.append("<table>");
      html.append("<thead>");
      startRow();
      appendTableHeader("Bit", "offset");
      appendTableHeader("Byte", "offset");
      appendTableHeader("Word", "offset");
      appendTableHeader("Type", "field-type");
      appendTableHeader("Name", "field-name");
      appendTableHeader("Size", "field-size");
      endRow();
      html.append("</thead>");
      html.append("<tbody>");

      for (GimpleField field : typeDef.getFields()) {

        startRow();

        offsetCell(field, 1);
        offsetCell(field, 8);
        offsetCell(field, 32);

        startCell("field-type");
        appendHtml(renderType(field.getType(), false));
        endCell();
        startCell("field-name");
        appendEscaped(field.getName());
        endCell();
        startCell("field-size");
        endCell();
        endRow();
      }

      html.append("</tbody>");
      html.append("</table>");
    }

    return html.toString();
  }

  private void offsetCell(GimpleField field, int multiple) {
    startCell("offset");
    if(field.getOffset() % multiple == 0) {
      html.append(field.getOffset() / multiple);
    }
    endCell();
  }

  private void startRow() {
    html.append("<tr>");
  }

  private StringBuilder startCell(final String className) {
    return html.append("<td class=\"").append(className).append("\">");
  }

  private StringBuilder endCell() {
    return html.append("</td>");
  }

  private void endRow() {
    html.append("</tr>");
  }


  private StringBuilder appendTableHeader(final String text, final String className) {
    return html.append("<th class=\"").append(className).append("\">").append(text).append("</th>");
  }

  private void renderStatement(GimpleStatement statement) {
    html.append("<div class=\"gstatement");
    if(statement.getLineNumber() != null) {
      html.append(String.format(" SL SL%d", statement.getLineNumber()));
    }
    html.append("\">");
    if(statement instanceof GimpleAssignment) {
      renderAssignment(((GimpleAssignment) statement));
    } else if(statement instanceof GimpleGoto) {
      renderGoto(((GimpleGoto) statement));
    } else if(statement instanceof GimpleReturn) {
      renderReturn(((GimpleReturn) statement));
    } else if(statement instanceof GimpleCall) {
      renderCall(((GimpleCall) statement));
    } else if(statement instanceof GimpleConditional) {
      conditional(((GimpleConditional) statement));
    }
    html.append("</div>\n");
  }


  private void renderCall(GimpleCall statement) {
    if(statement.getLhs() != null) {
      renderLhsType(statement.getLhs());
      expr(statement.getLhs());
      html.append(" ");
      symbol("=");
      html.append(" ");
    }
    expr(statement.getFunction());
    argumentList(statement.getOperands());

  }

  private void renderReturn(GimpleReturn statement) {
    html.append("RETURN ");
    if(statement.getValue() != null) {
      expr(statement.getValue());
    }
  }

  private void conditional(GimpleConditional statement) {
    symbol("IF");
    html.append(" ");
    renderOperation(statement.getOperator(), statement.getOperands());
    html.append(" GOTO ");
    html.append("BB").append(statement.getTrueLabel());
    html.append(" ELSE " );
    html.append("BB").append(statement.getFalseLabel());
  }

  private void renderGoto(GimpleGoto statement) {
    html.append("GOTO ").append(statement.getTarget());
  }

  private void renderAssignment(GimpleAssignment statement) {
    renderLhsType(statement.getLHS());
    expr(statement.getLHS());
    html.append(" = ");
    renderOperation(statement.getOperator(), statement.getOperands());
  }

  private void renderLhsType(GimpleLValue lhs) {
    appendHtml("<span class=\"lhs-type\">");
    appendHtml(renderType(lhs.getType(), false));
    appendHtml("</span> ");
  }

  private void renderOperation(GimpleOp operator, List<GimpleExpr> operands) {
    switch (operator) {
      case REAL_CST:
      case INTEGER_CST:
      case STRING_CST:
      case NOP_EXPR:
      case SSA_NAME:
      case PARM_DECL:
      case VAR_DECL:
      case COMPONENT_REF:
      case ARRAY_REF:
      case COMPLEX_CST:
      case  MEM_REF:
      case ADDR_EXPR:
      case CONSTRUCTOR:
        assert operands.size() == 1;
        expr(operands.get(0));
        break;
      case MULT_EXPR:
        operator("*", operands);
        break;
      case RDIV_EXPR:
        operator("/", operands);
        break;
      case ABS_EXPR:
        renderFunction("ABS", operands);
        break;
      case MIN_EXPR:
        renderFunction("MIN", operands);
        break;
      case MAX_EXPR:
        renderFunction("MAX", operands);
        break;
      case FLOAT_EXPR:
        renderFunction("FLOAT", operands);
        break;
      case FIX_TRUNC_EXPR:
        renderFunction("FIX_TRUNC", operands);
        break;
      case EXACT_DIV_EXPR:
        renderFunction("EXACT_DIV", operands);
        break;
      case TRUNC_DIV_EXPR:
        renderFunction("TRUNC_DIV", operands);
        break;
      case NE_EXPR:
        operator("!=", operands);
        break;
      case EQ_EXPR:
        operator("==", operands);
        break;
      case LT_EXPR:
        operator("<", operands);
        break;
      case GT_EXPR:
        operator(">", operands);
        break;
      case LE_EXPR:
        operator("<=", operands);
        break;
      case GE_EXPR:
        operator(">=", operands);
        break;
      case TRUTH_NOT_EXPR:
        operator("!", operands);
        break;
      case TRUTH_XOR_EXPR:
        operator("XOR", operands);
        break;
      case TRUTH_OR_EXPR:
        operator("||", operands);
        break;
      case TRUTH_AND_EXPR:
        operator("&&", operands);
        break;
      case POINTER_PLUS_EXPR:
        operator("+", operands);
        break;
      case INDIRECT_REF:
        break;
      case PLUS_EXPR:
        operator("+", operands);
        break;
      case MINUS_EXPR:
        operator("-", operands);
        break;
      case BIT_NOT_EXPR:
        operator("~", operands);
        break;
      case BIT_AND_EXPR:
        operator("&", operands);
        break;
      case BIT_IOR_EXPR:
        operator("|", operands);
        break;
      case BIT_XOR_EXPR:
        operator("^^", operands);
        break;
      case LSHIFT_EXPR:
        operator("<<", operands);
        break;
      case RSHIFT_EXPR:
        operator(">>", operands);
        break;
      case LROTATE_EXPR:
        renderFunction("LROTATE", operands);
        break;
      case NEGATE_EXPR:
        operator("~", operands);
        break;
      case PAREN_EXPR:
        operator("", operands);
        break;
      default:
        renderFunction(operator.name(), operands);
    }
  }

  private void renderFunction(String functionName, List<GimpleExpr> operands) {
    html.append(functionName);
    argumentList(operands);
  }

  private void argumentList(List<GimpleExpr> operands) {
    html.append("(");
    for (int i = 0; i < operands.size(); i++) {
      if(i > 0) {
        html.append(", ");
      }
      expr(operands.get(i));
    }
    html.append(")");
  }

  private void operator(String operator, List<GimpleExpr> operands) {
    if(operands.size() == 1) {
      html.append(HtmlEscapers.htmlEscaper().escape(operator));
      expr(operands.get(0));
    } else if (operands.size() == 2) {
      expr(operands.get(0));
      html.append(" ");
      html.append(HtmlEscapers.htmlEscaper().escape(operator));
      html.append(" ");
      expr(operands.get(1));
    } else {
      throw new IllegalArgumentException("arity: " + operands.size());
    }
  }

  private void symbol(String symbol) {
    html.append("<span class=\"symbol\">").append(HtmlEscapers.htmlEscaper().escape(symbol))
        .append("</span>");
  }

  private void exprMaybeGrouped(GimpleExpr value) {
    if(isSimple(value)) {
      expr(value);
    } else {
      symbol("(");
      expr(value);
      symbol(")");
    }
  }

  private void expr(final GimpleExpr expr) {
    html.append(String.format("<span class=\"gexpr %s\" title=\"%s\">",
        expr.getClass().getSimpleName(), renderType(expr.getType(), true)));

    expr.accept(new GimpleExprVisitor() {


      @Override
      public void visitAddressOf(GimpleAddressOf addressOf) {
        symbol("&");
        expr(addressOf.getValue());
      }

      @Override
      public void visitArrayRef(GimpleArrayRef arrayRef) {
        expr(arrayRef.getArray());
        symbol("[");
        expr(arrayRef.getIndex());
        symbol("]");
      }

      @Override
      public void visitBitFieldRef(GimpleBitFieldRefExpr bitFieldRef) {
        expr(bitFieldRef.getValue());
        symbol("[");
        html.append(bitFieldRef.getOffset() + ":" + bitFieldRef.getSize());
        symbol("]");
      }

      @Override
      public void visitComplexConstant(GimpleComplexConstant constant) {
        expr(constant.getReal());
        symbol("+");
        expr(constant.getIm());
        symbol("i");
      }

      @Override
      public void visitComponentRef(GimpleComponentRef componentRef) {
        exprMaybeGrouped(componentRef.getValue());
        symbol(".");
        expr(componentRef.getMember());
      }

      @Override
      public void visitCompoundLiteral(GimpleCompoundLiteral compoundLiteral) {
        expr(compoundLiteral.getDecl());
      }

      @Override
      public void visitConstantRef(GimpleConstantRef constantRef) {
        expr(constantRef.getValue());
      }

      @Override
      public void visitConstructor(GimpleConstructor constructor) {
        symbol("{");
        boolean needsComma = false;
        for (GimpleConstructor.Element element : constructor.getElements()) {
          if(needsComma) {
            symbol(", ");
          }
          if(element.getField() != null) {
            html.append(element.getFieldName()).append(": ");
          }
          expr(element.getValue());
          needsComma = true;
        }
        symbol("}");
      }

      @Override
      public void visitFieldRef(GimpleFieldRef fieldRef) {
        html.append(HtmlEscapers.htmlEscaper().escape(fieldRef.toString()));
      }

      private FunctionGenerator resolveGimpleFunction(GimpleFunctionRef ref) {
        CallGenerator callGenerator = symbolTable.findCallGenerator(ref);
        if(callGenerator instanceof FunctionCallGenerator) {
          FunctionCallGenerator fcg = (FunctionCallGenerator) callGenerator;
          if(fcg.getStrategy() instanceof FunctionGenerator) {
            return (FunctionGenerator) fcg.getStrategy();
          }
        }
        return null;
      }

      @Override
      public void visitFunctionRef(GimpleFunctionRef functionRef) {

        FunctionGenerator gimpleFunction = resolveGimpleFunction(functionRef);
        if(gimpleFunction != null) {
          html.append(String.format("<a href=\"../%s/%s.html\" title=\"%s\">%s</a>",
              gimpleFunction.getCompilationUnit().getSourceName(),
              gimpleFunction.getMangledName(),
              gimpleFunction.getMangledName(),
              gimpleFunction.getFunction().getName()));
        } else {
          html.append(functionRef.getName());
        }
      }

      @Override
      public void visitPrimitiveConstant(GimplePrimitiveConstant constant) {
        html.append(constant.getNumberValue());
      }

      @Override
      public void visitMemRef(GimpleMemRef memRef) {
        symbol("*");
        if(memRef.isOffsetZero()) {
          exprMaybeGrouped(memRef.getPointer());
        } else {
          symbol("(");
          expr(memRef.getPointer());
          symbol("+");
          expr(memRef.getOffset());
          symbol(")");
        }
      }

      @Override
      public void visitNop(GimpleNopExpr expr) {
        expr(expr.getValue());
      }

      @Override
      public void visitParamRef(GimpleParamRef paramRef) {
        html.append(paramRef.getName());
      }

      @Override
      public void visitPointerPlus(GimplePointerPlus pointerPlus) {
        exprMaybeGrouped(pointerPlus.getPointer());
        symbol("+");
        expr(pointerPlus.getOffset());
      }

      @Override
      public void visitResultDecl(GimpleResultDecl resultDecl) {
        html.append("__RESULT");
      }

      @Override
      public void visitSsaName(GimpleSsaName ssaName) {
        expr(ssaName.getVar());
      }

      @Override
      public void visitStringConstant(GimpleStringConstant stringConstant) {
        html.append(HtmlEscapers.htmlEscaper().escape(stringLiteral(stringConstant.getValue())));
      }

      @Override
      public void visitVariableRef(GimpleVariableRef variableRef) {
        html.append(HtmlEscapers.htmlEscaper().escape(variableRef.toString()));
      }

    });
    html.append("</span>");
  }


  private boolean isSimple(GimpleExpr expr) {
    return expr instanceof GimpleConstant ||
        expr instanceof GimpleConstantRef ||
        expr instanceof GimpleVariableRef ||
        expr instanceof GimpleParamRef;
  }

  private String renderType(GimpleType type, boolean tooltip) {
    if(type instanceof GimpleRecordType) {

      GimpleRecordType recordType = (GimpleRecordType) type;
      if(tooltip) {
        return recordName(recordType.getId());
      } else {
        return "<a href=\"records.html#" + recordType.getId() + "\">" +
            HtmlEscapers.htmlEscaper().escape(recordName(recordType.getId())) + "</a>";
      }

    } else if(type instanceof GimpleIndirectType) {
      return renderType(type.getBaseType(), tooltip) + "*";

    } else if(type instanceof GimpleFunctionType) {
      return "FUN";

    } else if(type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      StringBuilder sb = new StringBuilder();
      sb.append(renderType(arrayType.getComponentType(), tooltip));
      sb.append("[");
      if(arrayType.getUbound() != null) {
        if(arrayType.getLbound() != 0) {
          sb.append(arrayType.getLbound());
          sb.append(":");
          sb.append(arrayType.getUbound());
        } else {
          sb.append(arrayType.getElementCount());
        }
      }
      sb.append("]");
      return sb.toString();

    } else {
      return HtmlEscapers.htmlEscaper().escape(type.toString());
    }
  }

  private String recordName(String id) {
    String name = recordNameMap.get(id);
    if(name != null) {
      return name;
    }
    return "undef_" + id.replace("0x", "");
  }

  private String recordName(GimpleRecordTypeDef typeDef) {
    return recordName(typeDef.getId());
  }

  public static String stringLiteral(String string) {
    StringBuilder lit = new StringBuilder();
    lit.append("\"");
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if(c == '\n') {
        lit.append("\\n");
      } else if(c == '\r') {
        lit.append("\\r");
      } else if(c == '\t') {
        lit.append("\\t");
      } else if(c == '"') {
        lit.append("\\\"");
      } else if(c >= ' ' && c < 127) {
        lit.append(c);
      } else {
        lit.append("\\" + Integer.toHexString(c));
      }
    }
    lit.append("\"");
    return lit.toString();
  }
}
