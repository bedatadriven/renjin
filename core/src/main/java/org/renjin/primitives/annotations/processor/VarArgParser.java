package org.renjin.primitives.annotations.processor;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ListVector;

import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr._new;

public class VarArgParser {



  public static class PositionalArg {
    private final JvmMethod.Argument formal;
    private final JVar variable;

    public PositionalArg(JvmMethod.Argument formal, JVar variable) {
      this.formal = formal;
      this.variable = variable;
    }

    public JvmMethod.Argument getFormal() {
      return formal;
    }

    public JVar getVariable() {
      return variable;
    }
  }

  private ApplyMethodContext methodContext;
  private JBlock parent;
  private JvmMethod overload;

  private JVar varArgBuilder;
  private JVar varArgList;
  private List<JExpression> arguments = Lists.newArrayList();
  private List<PositionalArg> positionalArguments = Lists.newArrayList();
  private Map<JvmMethod.Argument, JVar> namedFlags = Maps.newHashMap();

  private JBlock argumentProcessingBlock;

  public VarArgParser(ApplyMethodContext methodContext, JBlock parent, JvmMethod overload) {
    this.methodContext = methodContext;
    this.parent = parent;
    this.overload = overload;

    boolean varArgsSeen = false;

    varArgBuilder = parent.decl(classRef(ListVector.NamedBuilder.class), "varArgs",
            _new(classRef(ListVector.NamedBuilder.class)));

    varArgList = parent.decl(classRef(ListVector.class), "varArgList");

    for (JvmMethod.Argument formal : this.overload.getAllArguments()) {
      if (formal.isContextual()) {
        // contextual arguments are filled in from the current
        // thread and calling environment
        arguments.add(contextualExpression(formal));

      } else if (formal.isVarArg()) {
        // the @ArgumentList parameter receives our varArg list
        arguments.add(varArgList);
        varArgsSeen = true;

      } else if (formal.isNamedFlag()) {
        // declare our flag with its default value and add it to the
        // namedFlags maps for subsequent matching of remainng arguments
        JVar flag = parent.decl(argumentType(formal), nextFlagName(), defaultValueExpression(formal));
        namedFlags.put(formal, flag);
        arguments.add(flag);

      } else {
        // normal positional parameter. we consume these in sequence
        if (varArgsSeen) {
          throw new GeneratorDefinitionException(
                  "Any argument following a @ArgumentList must be annotated with @NamedFlag");
        }

        JVar arg = parent.decl(argumentType(formal), nextPosName(formal));
        arguments.add(arg);
        positionalArguments.add(new PositionalArg(formal, arg));
      }
    }
    argumentProcessingBlock = parent.block();

    parent.block().assign(varArgList, varArgBuilder.invoke("build"));
  }

  private JExpression contextualExpression(JvmMethod.Argument formal) {
    if(formal.getClazz().equals(Context.class)) {
      return methodContext.getContext();
    } else if(formal.getClazz().equals(Environment.class)) {
      return methodContext.getEnvironment();
    } else if(formal.getClazz().equals(Session.class)) {
      return methodContext.getContext().invoke("getSession");
    } else {
      throw new RuntimeException("Invalid contextual argument type: " + formal.getClazz());
    }
  }

  private JClass classRef(Class clazz) {
    return methodContext.classRef(clazz);
  }

  private JExpression defaultValueExpression(JvmMethod.Argument argument) {
    if(argument.getClazz().equals(boolean.class)) {
      return argument.getDefaultValue() ? JExpr.TRUE : JExpr.FALSE;
    } else if(!argument.getClazz().isPrimitive()) {
      return JExpr._null();
    } else {
      throw new UnsupportedOperationException("Don't know how to define default value for " + argument);
    }
  }

  private JType argumentType(JvmMethod.Argument argument) {
    return methodContext.getCodeModel()._ref(argument.getClazz());
  }

  private String nextPosName(JvmMethod.Argument argument) {
    return "pos" + argument.getIndex();
  }

  private String nextFlagName() {
    return "flag" + namedFlags.size();
  }

  public List<PositionalArg> getPositionalArguments() {
    return positionalArguments;
  }

  public List<JExpression> getArguments() {
    return arguments;
  }

  public JVar getVarArgBuilder() {
    return varArgBuilder;
  }

  public JExpression getVarArgList() {
    return varArgList;
  }

  public Map<JvmMethod.Argument,JVar> getNamedFlags() {
    return namedFlags;
  }

  public JExpression getNamedFlagJExp(String name) {

    for(Map.Entry<JvmMethod.Argument, JVar> flag : namedFlags.entrySet()) {
      if(flag.getKey().getName().equals(name)) {
        return flag.getValue();
      }
    }
    throw new UnsupportedOperationException("not such named flag: " + name);
  }

  public JBlock getArgumentProcessingBlock() {
    return argumentProcessingBlock;
  }
}