/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.sexp;

import org.renjin.eval.*;
import org.renjin.primitives.special.ReturnException;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * The function closure data type.
 *
 * <p>
 * In R functions are objects and can be manipulated in much the same way as any other object.
 * Functions (or more precisely, function closures) have three basic components:
 *  a formal argument list, a body and an environment.
 *
 */
public class Closure extends AbstractSEXP implements Function {

  public static final String TYPE_NAME = "closure";
  private Environment enclosingEnvironment;
  private Supplier<SEXP> body;
  private PairList formals;

  private ArgumentMatcher matcher;

  public SEXP[] frameSymbols;
  public MethodHandle compiledBody;

  public Closure(Environment enclosingEnvironment, PairList formals, SEXP body, AttributeMap attributes) {
    super(attributes);
    assert !(formals instanceof FunctionCall);
    this.enclosingEnvironment = enclosingEnvironment;
    this.body = () -> body;
    this.formals = formals;
  }

  public Closure(Environment enclosingEnvironment, PairList formals, Supplier<SEXP> body, AttributeMap attributes,
                 MethodHandle methodHandle, SEXP[] frameSymbols) {
    super(attributes);
    this.enclosingEnvironment = enclosingEnvironment;
    this.formals = formals;
    this.body = body;
    this.compiledBody = methodHandle;
    this.frameSymbols = frameSymbols;
  }


  public Closure(Environment environment, PairList formals, SEXP body) {
    this(environment, formals, body, AttributeMap.EMPTY);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }


  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap newAttributes) {
    return new Closure(this.enclosingEnvironment, this.formals, this.body, newAttributes,
        compiledBody,
        frameSymbols);
  }

  @Override
  public String getImplicitClass() {
    return Function.IMPLICIT_CLASS;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public final SEXP apply(Context context, Environment rho, FunctionCall call) {

    List<String> argumentNames = new ArrayList<>();
    List<SEXP> arguments = new ArrayList<>();

    for (PairList.Node node : call.getArguments().nodes()) {
      SEXP value = node.getValue();
      if(value == Symbols.ELLIPSES) {
        SEXP expando = rho.getEllipsesVariable();
        if(expando == Symbol.UNBOUND_VALUE) {
          throw new EvalException("'...' used in an incorrect context");
        }
        if(expando instanceof PromisePairList) {
          PromisePairList extra = (PromisePairList) expando;
          for (PairList.Node extraNode : extra.nodes()) {
            argumentNames.add(extraNode.hasTag() ? extraNode.getName() : null);
            arguments.add(extraNode.getValue());
          }
        }

      } else {
        if(node.hasName()) {
          argumentNames.add(node.getTag().getPrintName());
        } else {
          argumentNames.add(null);
        }
        if(value == Symbol.MISSING_ARG) {
          arguments.add(value);
        } else {
          arguments.add(value.promise(rho));
        }
      }
    }
    return applyPromised(context, rho, call, argumentNames.toArray(new String[0]), arguments.toArray(new SEXP[0]), null);
  }

  public SEXP applyPromised(Context callingContext, Environment callingEnvironment, FunctionCall call, String[] argNames, SEXP[] args, DispatchTable dispatch) {

    if(this.matcher == null) {
      this.matcher = new ArgumentMatcher(getFormals());
    }
    if(frameSymbols == null) {
      this.frameSymbols = matcher.getFormalNameArray();
    }

    MatchedArguments matching = matcher.match(argNames, args);
    SEXP[] matchedArguments = new SEXP[matcher.getFormalCount()];

    int numFormals = matching.getFormalCount();

    for (int formalIndex = 0; formalIndex < numFormals; formalIndex++) {
      if (matching.isFormalEllipses(formalIndex)) {
        matchedArguments[formalIndex] = matching.buildExtraArgumentList();

      } else {
        int actualIndex = matching.getActualIndex(formalIndex);
        if (actualIndex != -1) {
          SEXP actualValue = matching.getActualValue(actualIndex);
          if (actualValue != Symbol.MISSING_ARG) {
            matchedArguments[formalIndex] = actualValue;
          }
        }
      }
    }

    SEXP[] locals = Arrays.copyOf(matchedArguments, frameSymbols.length);

    FunctionEnvironment functionEnvironment = new FunctionEnvironment(
        getEnclosingEnvironment(),
        frameSymbols,
        matchedArguments,
        matching,
        locals,
        dispatch
    );

    Context functionContext = callingContext.beginFunction(
        callingEnvironment,
        functionEnvironment,
        call,
        this
    );

    for (int i = 0; i < numFormals; i++) {
      if (locals[i] == null) {
        SEXP defaultValue = matcher.getDefaultValue(i);
        if (defaultValue != Symbol.MISSING_ARG) {
          defaultValue = defaultValue.promise(functionEnvironment);
        }
        locals[i] = defaultValue;
      }
    }

    try {

      try {

        if(compiledBody != null) {
          return (SEXP)compiledBody.invokeExact(functionContext, functionEnvironment);
        } else {
          return body.get().eval(functionContext, functionEnvironment);
        }

      } catch (EvalException e) {
        // Associate this EvalException with this function call context if it's not already.
        // N.B. initContext() also searches for condition handlers and may rethrow this
        // EvalException as a ConditionException if found.
        e.initContext(functionContext);
        throw e;
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable throwable) {
        throw new EvalException(throwable);
      }

    } catch(ReturnException e) {
      if (e.getEnvironment() != functionEnvironment) {
        throw e;
      }
      return e.getValue();

    } catch(ConditionException e) {
      if (e.getHandlerContext() == functionContext) {
        return new ListVector(e.getCondition(), Null.INSTANCE, e.getHandler());
      } else {
        throw e;
      }

    } catch (RestartException e) {
      if(e.getExitEnvironment() == functionContext.getEnvironment()) {
        // This return value is consumed by the R code in conditions.R
        return e.getArguments();
      } else {
        throw e;
      }

    } finally {
      functionContext.exit();
    }
  }


  /**
   * A function's <strong> evaluation environment</strong> is the environment
   * that was active at the time that the
   * function was created. Any symbols bound in that environment are
   * captured and available to the function. This combination of the code of the
   * function and the bindings in its environment is called a `function closure', a
   * term from functional programming theory.
   *
   */
  public Environment getEnclosingEnvironment() {
    return enclosingEnvironment;
  }

  /**
   * Creates a copy of this Closure with the new enclosing environment.
   */
  public Closure setEnclosingEnvironment(Environment env) {
    return new Closure(env, formals, body, getAttributes(), compiledBody, frameSymbols);
  }

  /**
   * The body is a parsed R statement.
   * It is usually a collection of statements in braces but it
   * can be a single statement, a symbol or even a constant.
   */
  public SEXP getBody() {
    return body.get();
  }

  /**
   * The formal argument list is a a pair list of arguments.
   * An argument can be a symbol, or a ‘symbol = default’ construct, or
   * the special argument ‘...’.
   *
   * <p> The second form of argument is
   *  used to specify a default value for an argument.
   * This value will be used if the function is called
   *  without any value specified for that argument.
   * The ‘...’ argument is special and can contain any number of arguments.
   * It is generally used if the number of arguments
   * is unknown or in cases where the arguments will
   * be passed on to another function.
   */
  public PairList getFormals() {
    return formals;
  }

  public void unsafeSetFormals(PairList formals) {
    this.formals = formals;
  }

  public void unsafeSetBody(SEXP body) {
    this.body = () -> body;
  }

  public void unsafeSetEnclosingEnvironment(Environment v) {
    this.enclosingEnvironment = v;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("function(");
    if(getFormals() instanceof PairList.Node) {
      ((PairList.Node) getFormals()).appendValuesTo(sb);
    }
    return sb.append(")").toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime
        * result
        + ((enclosingEnvironment == null) ? 0 : enclosingEnvironment.hashCode());
    result = prime * result + ((formals == null) ? 0 : formals.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Closure)) {
      return false;
    }
    Closure other = (Closure) obj;
    if(!Objects.equals(body.get(), other.body.get())) {
      return false;
    }
    if(!Objects.equals(enclosingEnvironment, other.enclosingEnvironment)) {
      return false;
    }
    if(!Objects.equals(formals, other.formals)) {
      return false;
    }
    return true;
  }

}
