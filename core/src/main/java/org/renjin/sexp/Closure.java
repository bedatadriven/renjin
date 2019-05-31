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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


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
  private SEXP body;
  private PairList formals;

  private ArgumentMatcher matcher;
  private SEXP[] formalSymbols;

  public Closure(Environment enclosingEnvironment, PairList formals, SEXP body, AttributeMap attributes) {
    super(attributes);
    assert !(formals instanceof FunctionCall);
    this.enclosingEnvironment = enclosingEnvironment;
    this.body = body;
    this.formals = formals;
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
    return new Closure(this.enclosingEnvironment, this.formals, this.body, newAttributes);
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
          arguments.add(Symbol.MISSING_ARG);
        } else {
          arguments.add(Promise.repromise(rho, value));
        }
      }
    }
    return apply(context, rho, call, argumentNames.toArray(new String[0]), arguments.toArray(new SEXP[0]), null);
  }

  public SEXP apply(Context callingContext, Environment callingEnvironment, FunctionCall call, String[] argNames, SEXP[] args, DispatchTable dispatch) {

    if(this.matcher == null) {
      this.matcher = new ArgumentMatcher(getFormals());
      this.formalSymbols = matcher.getFormalNameArray();
    }

    SEXP[] arguments = new SEXP[matcher.getFormalCount()];

    MatchedArguments matching = matcher.match(argNames, args);

    for (int formalIndex = 0; formalIndex < matching.getFormalCount(); formalIndex++) {
      if (matching.isFormalEllipses(formalIndex)) {
        arguments[formalIndex] = matching.buildExtraArgumentList();

      } else {
        int actualIndex = matching.getActualIndex(formalIndex);
        if (actualIndex != -1) {
          SEXP actualValue = matching.getActualValue(actualIndex);
          if (actualValue != Symbol.MISSING_ARG) {
            arguments[formalIndex] = actualValue;
          }
        }
      }
    }



    SEXP[] locals = Arrays.copyOf(arguments, arguments.length);

    FunctionEnvironment functionEnvironment = new FunctionEnvironment(
        getEnclosingEnvironment(),
        formalSymbols,
        arguments,
        locals,
        dispatch
    );

    Context functionContext = callingContext.beginFunction(
        callingEnvironment,
        functionEnvironment,
        call,
        this
    );

    for (int i = 0; i < locals.length; i++) {
      if (locals[i] == null) {
        SEXP defaultValue = matcher.getDefaultValue(i);
        if (defaultValue != Symbol.MISSING_ARG) {
          defaultValue = Promise.repromise(functionEnvironment, defaultValue);
        }
        locals[i] = defaultValue;
      }
    }

    // If we are being called by UseMethod() or by NextMethod(), then
    // save a reference to our matched arguments. This is required by NextMethod().

    if(dispatch instanceof S3DispatchMetadata) {
      ((S3DispatchMetadata) dispatch).arguments = matching;
    }

    try {

      try {


        return functionContext.evaluate(body);

      } catch (EvalException e) {
        // Associate this EvalException with this function call context if it's not already.
        // N.B. initContext() also searches for condition handlers and may rethrow this
        // EvalException as a ConditionException if found.
        e.initContext(functionContext);
        throw e;
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


  public SEXP doApply(Context functionContext) {
    return functionContext.evaluate(body);
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
   * @param env the new enclosing environment.
   * @return
   */
  public Closure setEnclosingEnvironment(Environment env) {
    return new Closure(env, formals, body, getAttributes());
  }

  /**
   * The body is a parsed R statement.
   * It is usually a collection of statements in braces but it
   * can be a single statement, a symbol or even a constant.
   */
  public SEXP getBody() {
    return body;
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
    this.body = body;
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
    if(!Objects.equals(body, other.body)) {
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
