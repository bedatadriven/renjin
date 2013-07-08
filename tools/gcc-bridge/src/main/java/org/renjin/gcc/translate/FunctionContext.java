package org.renjin.gcc.translate;

import java.util.Map;

import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.Variable;

import com.google.common.collect.Maps;

public class FunctionContext {

  private TranslationContext translationContext;
  private GimpleFunction gimpleFunction;
  private JimpleMethodBuilder builder;
  private Map<Integer, Variable> symbolTable = Maps.newHashMap();

  private int nextLabelId = 1000;

  public FunctionContext(TranslationContext translationContext, GimpleFunction gimpleFunction,
      JimpleMethodBuilder builder) {
    this.gimpleFunction = gimpleFunction;
    this.translationContext = translationContext;
    this.builder = builder;

    VarUsageInspector varUsage = new VarUsageInspector(gimpleFunction);

    for (GimpleVarDecl decl : gimpleFunction.getVariableDeclarations()) {
      Variable localVariable = translationContext.resolveType(decl.getType())
          .createLocalVariable(this, decl.getName(),
              varUsage.getUsage(decl.getId()));

      symbolTable.put(decl.getId(), localVariable);
    }

    for (GimpleParameter param : gimpleFunction.getParameters()) {
      ImType type = translationContext.resolveType(param.getType());
      builder.addParameter(type.paramType(), "p_" + param.getName());
      ImExpr paramExpr = JvmExprs.toExpr(this, new JimpleExpr("p_" + param.getName()), type.paramType(), true);

      Variable variable = type.createLocalVariable(this, param.getName(), varUsage.getUsage(param.getId()));
      variable.writeAssignment(this, paramExpr);


      symbolTable.put(param.getId(), variable);
    }
  }

  public MethodRef resolveMethod(GimpleCall call) {
    return translationContext.resolveMethod(call, getCallingConvention());
  }

  public CallingConvention getCallingConvention() {
    return gimpleFunction.getCallingConvention();
  }

  public String declareTemp(JimpleType type) {
    return getBuilder().addTempVarDecl(type);
  }

  public String declareTemp(Class clazz) {
    return declareTemp(new RealJimpleType(clazz));
  }

  public JimpleExpr declareTemp(JimpleType type, JimpleExpr value) {
    String tempVar = declareTemp(type);
    getBuilder().addAssignment(tempVar, value);
    return new JimpleExpr(tempVar);
  }

  public JimpleMethodBuilder getBuilder() {
    return builder;
  }

  public TranslationContext getTranslationContext() {
    return translationContext;
  }

  public String newLabel() {
    return "trlabel" + (nextLabelId++) + "__";
  }

  public ImExpr lookupVar(GimpleExpr gimpleExpr) {
    if (gimpleExpr instanceof SymbolRef) {
      SymbolRef symbol = (SymbolRef) gimpleExpr;
      ImExpr variable = symbolTable.get(symbol.getId());

      if(variable != null) {
        return variable;
      }

      if(symbol.getName() != null) {
        variable = translationContext.findGlobal(symbol.getName());
      }

      if (variable == null) {
        throw new IllegalArgumentException("No such variable '" + gimpleExpr + "' (id=" + symbol.getId() + ")");
      }
      return variable;
    } else {
      throw new UnsupportedOperationException("Expected GimpleVar, got: " + gimpleExpr + " ["
          + gimpleExpr.getClass().getSimpleName() + "]");
    }
  }

  public ImExpr resolveExpr(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleMemRef) {
      return resolveExpr(((GimpleMemRef) gimpleExpr).getPointer()).memref();
    } else if(gimpleExpr instanceof SymbolRef) {
      return lookupVar(gimpleExpr);

    } else if(gimpleExpr instanceof GimpleStringConstant) {
      return new ImStringConstant((GimpleStringConstant) gimpleExpr);

    } else if(gimpleExpr instanceof GimpleConstant) {
      return new ImPrimitiveConstant(this, (GimpleConstant) gimpleExpr);

    } else if(gimpleExpr instanceof GimpleAddressOf) {
      return resolveExpr(((GimpleAddressOf) gimpleExpr).getValue()).addressOf();

    } else if(gimpleExpr instanceof GimpleFunctionRef) {
      return new ImFunctionExpr(translationContext.resolveMethod(((GimpleFunctionRef) gimpleExpr).getName()));

    } else if(gimpleExpr instanceof GimpleArrayRef) {
      GimpleArrayRef arrayRef = (GimpleArrayRef) gimpleExpr;
      return resolveExpr(arrayRef.getArray()).elementAt(resolveExpr(arrayRef.getIndex()));

    } else if(gimpleExpr instanceof GimpleComponentRef) {
      GimpleComponentRef componentRef = (GimpleComponentRef) gimpleExpr;
      return resolveExpr(componentRef.getValue()).member(componentRef.getMember());

    } else if(gimpleExpr instanceof GimpleConstantRef) {
      return resolveExpr(((GimpleConstantRef) gimpleExpr).getValue());
    }
    throw new UnsupportedOperationException(gimpleExpr.toString());
  }

}
