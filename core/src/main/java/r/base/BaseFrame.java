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

package r.base;

import com.google.common.collect.Sets;
import org.apache.commons.math.distribution.Distribution;
import r.base.special.*;
import r.lang.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static r.base.PPkind.*;
import static r.base.PPprec.*;

/**
 *  The {@code Frame} that provides the primitive functions for the
 *  the base environment.
 *
 *  The singleton instance is immutable and so can be safely shared between
 * multiple threads / contexts.
 */
public class BaseFrame implements Frame {

  private Map<Symbol, SEXP> builtins = new HashMap<Symbol, SEXP>();
  private Map<Symbol, SEXP> internals = new HashMap<Symbol, SEXP>();
  private Map<Symbol, SEXP> loaded = new HashMap<Symbol, SEXP>();

  @Override
  public Set<Symbol> getSymbols() {
    return Sets.union(builtins.keySet(), loaded.keySet());
  }

  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = builtins.get(name);
    if(value != null) {
      return value;
    }
    value = loaded.get(name);
    if(value != null ) {
      return value;
    }
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  public SEXP getInternal(Symbol name) {
    SEXP value = internals.get(name);
    if(value != null) {
      return value;
    }
    return Null.INSTANCE;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    loaded.put(name, value);
  }

  public BaseFrame() {
    installBuiltins();
    installPlatform();
    installMachine();
    installLoaded();
  }

  protected void installLoaded() {
  }

  private void add(String name, Function fn) {
    builtins.put(new Symbol(name), fn);
  }

  private void add(SpecialFunction fn) {
    builtins.put(new Symbol(fn.getName()), fn);
  }

  private void add(Entry entry) {
    Symbol symbol = new Symbol(entry.name);
    PrimitiveFunction primitive;
    primitive = new BuiltinFunction(entry);

    if ((entry.eval % 100) / 10 != 0) {
      internals.put(symbol, primitive);
    } else {
      builtins.put(symbol, primitive);
    }
  }

  private void installPlatform() {
    builtins.put(new Symbol(".Platform"), ListVector.newBuilder()
        .add("OS.type", new StringVector(resolveOsName()))
        .add("file.sep", new StringVector("/"))
        .add("GUI", new StringVector("unknown"))
        .add("endian", new StringVector("big"))
        .add("pkgType", new StringVector("source"))
        .add("r_arch", new StringVector(""))
        .add("dynlib.ext", new StringVector(".dll"))
        .build());
  }

  /**
   * Adds the .Machine list to the base frame,
   * a variable holding information on the numerical characteristics of the machine R is
   * running on, such as the largest double or integer and the machine's precision.
   */
  private void installMachine() {
    // TODO: I'm not sure how these values are used, but
    // I have mostly just copied them from my local R installation
    builtins.put(new Symbol(".Machine"), ListVector.newBuilder()
        .add("double.eps", new DoubleVector(DoubleVector.EPSILON))
        .add("double.neg.eps", new DoubleVector(1.110223e-16))
        .add("double.xmin",new DoubleVector(2.225074e-308))
        .add("double.xmax", new DoubleVector(1.797693e+308))
        .add("double.base", new IntVector(2))
        .add("double.digits", new IntVector(53))
        .add("double.rounding", new IntVector(5))
        .add("double.guard", new IntVector(0))
        .add("double.ulp.digits", new IntVector(-52))
        .add("double.neg.ulp.digits", new IntVector(-53))
        .add("double.exponent", new IntVector(11))
        .add("double.min.exp", new IntVector(Double.MIN_EXPONENT))
        .add("double.max.exp", new IntVector(Double.MAX_EXPONENT))
        .add("integer.max", new IntVector(Integer.MAX_VALUE))
        .add("sizeof.long", new IntVector(4))
        .add("sizeof.longlong", new IntVector(8))
        .add("sizeof.longdouble", new IntVector(12))
        .add("sizeof.pointer", new IntVector(4))
       .build());

  }

  private String resolveOsName() {
    return java.lang.System.getProperty("os.name").contains("windows") ? "windows" : "unix";
  }

  private void installBuiltins() {
    add(new IfFunction());
    add(new WhileFunction());
    add(new ForFunction());
    add(new RepeatFunction());
    add(new BreakFunction());
    add(new NextFunction());
    add(new ReturnFunction());

    f("stop", Evaluation.class, 0, 11, 2);
    f("warning", Evaluation.class, 0, 111, 3);

    f("gettext", Text.class, 0, 11, 2);
    f("ngettext", Text.class, 0, 11, 4);
    f("bindtextdomain", Text.class, 0, 11, 2);

    f(".addCondHands", Conditions.class, 0, 111, 5);
    f(".resetCondHands", /*resetCondHands*/ null, 0, 111, 1);
    f(".signalCondition", Conditions.class, 0, 11, 3);
    f(".dfltStop", /*dfltStop*/ null, 0, 11, 2);
    f(".dfltWarn", /*dfltWarn*/ null, 0, 11, 2);
    f(".addRestart", Conditions.class, 0, 11, 1);
    f(".getRestart", /*getRestart*/ null, 0, 11, 1);
    f(".invokeRestart", /*invokeRestart*/ null, 0, 11, 2);
    f(".addTryHandlers", /*addTryHandlers*/ null, 0, 111, 0);

    f("geterrmessage", /*geterrmessage*/ null, 0, 11, 0);
    f("seterrmessage", /*seterrmessage*/ null, 0, 111, 1);
    f("printDeferredWarnings", /*printDeferredWarnings*/ null, 0, 111, 0);
    f("interruptsSuspended", /*interruptsSuspended*/ null, 0, 11, -1);

    addInternal("restart", new RestartFunction());
    add(new ClosureFunction());

    f("as.function.default", /*asfunction*/ null, 0, 11, 2, PP_FUNCTION, PREC_FN, 0);

    add(new AssignLeftFunction());
    f("=", /*set*/ null, 3, 100, -1, PP_ASSIGN, PREC_EQ, 1);

    add(new ReassignLeftFunction());
    add(new BeginFunction());
    add(new ParenFunction());

    f("subset", /*subset_dflt*/ null, 1, 1, -1);
    f(".subset2", /*subset2_dflt*/ null, 2, 1, -1);
    f("[",Subscript.class, 1, 0, -1, PP_SUBSET, PREC_SUBSET, 0);
    f("[[", Subscript.class, 2, 0, -1, PP_SUBSET, PREC_SUBSET, 0);
    f("$", Subscript.class, 3, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0);
    f("@", /*AT*/ null, 0, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0);
    f("[<-", Subscript.class, 0, 0, 3, PP_SUBASS, PREC_LEFT, 1);
    f("[[<-", Subscript.class, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1);
    f("$<-", Subscript.class, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1);

    addInternal("switch", new SwitchFunction());

    f("browser", /*browser*/ null, 0, 101, 3);
    f("debug", /*debug*/ null, 0, 111, 3);
    f("undebug", /*debug*/ null, 1, 111, 1);
    f("isdebugged", /*debug*/ null, 2, 11, 1);
    f("debugonce", /*debug*/ null, 3, 111, 3);
    f(".primTrace", /*trace*/ null, 0, 101, 1);
    f(".primUntrace", /*trace*/ null, 1, 101, 1);

    add(new InternalFunction());
    add(new OnExitFunction());

    f("Recall", /*recall*/ null, 0, 210, -1);
    f("delayedAssign", Evaluation.class, 0, 111, 4);
    f("makeLazy", Connections.class, 0, 111, 5);
    f(".Primitive", /*primitive*/ null, 0, 1, 1);
    f("identical",  Types.class, 0, 11, 5);


/* Binary Operators */
/* these are group generic and so need to eval args */
//    f("+",  Ops.class,"plus", PLUSOP, 1, 2, PP_BINARY, PREC_SUM, 0);
//    f("-", Ops.class, "minus", MINUSOP, 1, 2, PP_BINARY, PREC_SUM, 0);
//    f("*", Ops.class, "multiply", TIMESOP, 1, 2, PP_BINARY, PREC_PROD, 0);
//    f("/", Ops.class, "divide", DIVOP, 1, 2, PP_BINARY2, PREC_PROD, 0);
//    f("^", Ops.class, "pow", POWOP, 1, 2, PP_BINARY2, PREC_POWER, 1);
    add(new OpsFunction("+"));
    add(new OpsFunction("-"));
    add(new OpsFunction("*"));
    add(new OpsFunction("/"));
    add(new OpsFunction("^"));

    f("%%", Ops.class, 0 /* MODOP */, 1, 2, PP_BINARY2, PREC_PERCENT, 0);
    f("%/%", Ops.class, 0 /* IDIVOP */, 1, 2, PP_BINARY2, PREC_PERCENT, 0);
    f("%*%", Ops.class, 0, 1, 2, PP_BINARY, PREC_PERCENT, 0);
    f("crossprod", /*matprod*/ null, 1, 11, 2);
    f("tcrossprod", /*matprod*/ null, 2, 11, 2);


/* these are group generic and so need to eval args */
//    f("==", Ops.class, EQOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f("!=", Ops.class, NEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f("<", Ops.class, LTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f("<=", Ops.class, LEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f(">=", Ops.class, GEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f(">", Ops.class, GTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
//    f("&", Ops.class,  1, 1, 2, PP_BINARY, PREC_AND, 0);
//    f("|", Ops.class, 2, 1, 2, PP_BINARY, PREC_OR, 0);
//    f("!", Ops.class, 3, 1, 1, PP_UNARY, PREC_NOT, 0);
    add(new OpsFunction("=="));
    add(new OpsFunction("!="));
    add(new OpsFunction("<"));
    add(new OpsFunction("<="));
    add(new OpsFunction(">"));
    add(new OpsFunction(">="));
    add(new OpsFunction("&"));
    add(new OpsFunction("|"));
    add(new OpsFunction("!"));

    f("&&", Comparison.class, "and", 1, 0, 2, PP_BINARY, PREC_AND, 0);
    f("||", Comparison.class, "or", 2, 0, 2, PP_BINARY, PREC_OR, 0);
    f(":", Sequences.class, "colon", 0, 1, 2, PP_BINARY2, PREC_COLON, 0);
    f("~", Models.class, 0, 0, 2, PP_BINARY, PREC_TILDE, 0);


/* Logic Related Functions */
/* these are group generic and so need to eval args */
    f("all", Summary.class, 1, 1, -1);
    f("any", Summary.class, 2, 1, -1);


/* Vectors, Matrices and Arrays */

/* printname	c-entry		offset	eval	arity	pp-kind	     precedence	rightassoc
 * ---------	-------		------	----	-----	-------      ----------	----------*/
    f("vector", Types.class, 0, 11, 2);
    f("complex", /*complex*/ null, 0, 11, 3);
    f("matrix", Combine.class, 0, 11, -1);
    f("length", Types.class, 0, 1, 1);
    f("length<-", /*lengthgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("row", /*rowscols*/ null, 1, 11, 1);
    f("col", /*rowscols*/ null, 2, 11, 1);
    f("c", Combine.class,  0, 0, -1);
    f("unlist", Combine.class, 0, 11, 3);
    f("cbind", Combine.class, 1, 10, -1);
    f("rbind", Combine.class, 2, 10, -1);
    f("drop", /*drop*/ null, 0, 11, 1);
    f("oldClass", Types.class, 0, 1, 1);
    f("oldClass<-", /*classgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("class", Types.class, "getClass", 0, 1, 1);
    f("class<-", Types.class, "setClass", 0, 1, 2);
    f("unclass", Types.class, 0, 1, 1);
    f("names", Types.class,  "getNames", 0, 1, 1);
    f("names<-", Types.class, "setNames", 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("dimnames", Types.class, 0, 1, 1);
    f("dimnames<-", Types.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("all.names", /*allnames*/ null, 0, 11, 4);
    f("dim", Types.class, 0, 1, 1);
    f("dim<-", Types.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("attributes", Types.class, 0, 1, 1);
    f("attributes<-", Types.class, null, 0, 1, 1, PP_FUNCALL, PREC_LEFT, 1);
    f("attr", Types.class, 0, 1, -1);
    f("attr<-", Types.class, 0, 1, 3, PP_FUNCALL, PREC_LEFT, 1);
    f("comment", /*comment*/ null, 0, 11, 1);
    f("comment<-", /*commentgets*/ null, 0, 11, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("levels<-", /*levelsgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("get", Types.class, 1, 11, 4);
    f("mget", /*mget*/ null, 1, 11, 5);
    f("exists", Types.class, 0, 11, 4);
    f("assign", Evaluation.class, 0, 111, 4);
    f("remove", /*remove*/ null, 0, 111, 3);
    f("duplicated", /*duplicated*/ null, 0, 11, 3);
    f("unique", Match.class, 1, 11, 3);
    f("anyDuplicated", Match.class, 2, 11, 3);
    f("which.min", /*first_min*/ null, 0, 11, 1);
    f("pmin", /*pmin*/ null, 0, 11, -1);
    f("pmax", /*pmin*/ null, 1, 11, -1);
    f("which.max", /*first_min*/ null, 1, 11, 1);
    f("match", Match.class, 0, 11, 4);
    f("pmatch", Match.class, 0, 11, 4);
    f("charmatch", /*charmatch*/ null, 0, 11, 3);
    f("match.call", /*matchcall*/ null, 0, 11, 3);
    f("complete.cases", /*compcases*/ null, 0, 11, 1);

    f("attach", Types.class, 0, 111, 3);
    f("detach", Types.class, 0, 111, 1);
    f("search", Types.class, 0, 11, 0);


/* Mathematical Functions */
/* these are group generic and so need to eval args */
/* Note that the number of arguments for the primitives in the Math group
   only applies to the default method. */
    f("round", Math.class, 10001, 0, -1);
    f("signif", /*Math2*/ null, 10004, 0, -1);
    f("atan",Math.class, 10002, 1, 1);
    f("log", MathExt.class, 10003, 0, -1);
    f("log10", Math.class, 10, 1, 1);
    f("log2", MathExt.class, 2, 1, 1);
    f("abs", Math.class, 6, 1, 1);
    f("floor", Math.class, 1, 1, 1);
    f("ceiling", Math.class, "ceil", 2, 1, 1);
    f("sqrt", Math.class, 3, 1, 1);
    f("sign", Math.class, "signnum", 4, 1, 1);
    f("trunc", /*trunc*/ null, 5, 1, -1);

    f("exp", Math.class, 10, 1, 1);
    f("expm1", /*math1*/ null, 11, 1, 1);
    f("log1p", /*math1*/ null, 12, 1, 1);

    f("cos", Math.class, 20, 1, 1);
    f("sin", Math.class, 21, 1, 1);
    f("tan", Math.class, 22, 1, 1);
    f("acos", Math.class, 23, 1, 1);
    f("asin", Math.class, 24, 1, 1);

    f("cosh", Math.class, 30, 1, 1);
    f("sinh", Math.class, 31, 1, 1);
    f("tanh", Math.class, 32, 1, 1);
    f("acosh", /*math1*/ null, 33, 1, 1);
    f("asinh", /*math1*/ null, 34, 1, 1);
    f("atanh", /*math1*/ null, 35, 1, 1);

    f("lgamma", org.apache.commons.math.special.Gamma.class, "logGamma", 40, 1, 1);
    f("gamma", MathExt.class, 41, 1, 1);

    f("digamma", org.apache.commons.math.special.Gamma.class, 42, 1, 1);
    f("trigamma",org.apache.commons.math.special.Gamma.class, 43, 1, 1);
/* see "psigamma" below !*/

/* Mathematical Functions of Two Numeric (+ 1-2 int) Variables */

    f("atan2", /*math2*/ null, 0, 11, 2);

    f("lbeta", /*math2*/ null, 2, 11, 2);
    f("beta", /*math2*/ null, 3, 11, 2);
    f("lchoose", /*math2*/ null, 4, 11, 2);
    f("choose", /*math2*/ null, 5, 11, 2);

    f("dchisq", Distributions.class, 6, 11, 2 + 1);
    f("pchisq", Distributions.class, 7, 11, 2 + 2);
    f("qchisq", Distributions.class, 8, 11, 2 + 2);

    f("dexp", Distributions.class, 9, 11, 2 + 1);
    f("pexp", Distributions.class, 10, 11, 2 + 2);
    f("qexp", Distributions.class, 11, 11, 2 + 2);

    f("dgeom", /*math2*/ null, 12, 11, 2 + 1);
    f("pgeom", /*math2*/ null, 13, 11, 2 + 2);
    f("qgeom", /*math2*/ null, 14, 11, 2 + 2);

    f("dpois", Distributions.class, 15, 11, 2 + 1);
    f("ppois", Distributions.class, 16, 11, 2 + 2);
    f("qpois", Distributions.class, 17, 11, 2 + 2);

    f("dt", Distributions.class, 18, 11, 2 + 1);
    f("pt", Distributions.class, 19, 11, 2 + 2);
    f("qt", Distributions.class, 20, 11, 2 + 2);

    f("dsignrank", /*math2*/ null, 21, 11, 2 + 1);
    f("psignrank", /*math2*/ null, 22, 11, 2 + 2);
    f("qsignrank", /*math2*/ null, 23, 11, 2 + 2);

    f("besselJ", /*math2*/ null, 24, 11, 2);
    f("besselY", /*math2*/ null, 25, 11, 2);

    f("psigamma", /*math2*/ null, 26, 11, 2);


/* Mathematical Functions of a Complex Argument */
/* these are group generic and so need to eval args */

    f("Re", /*cmathfuns*/ null, 1, 1, 1);
    f("Im", /*cmathfuns*/ null, 2, 1, 1);
    f("Mod", /*cmathfuns*/ null, 3, 1, 1);
    f("Arg", /*cmathfuns*/ null, 4, 1, 1);
    f("Conj", /*cmathfuns*/ null, 5, 1, 1);


/* Mathematical Functions of Three Numeric (+ 1-2 int) Variables */

    f("dbeta", Distribution.class, 1, 11, 3 + 1);
    f("pbeta", Distributions.class, 2, 11, 3 + 2);
    f("qbeta", Distributions.class, 3, 11, 3 + 2);

    f("dbinom", Distributions.class, 4, 11, 3 + 1);
    f("pbinom", Distributions.class, 5, 11, 3 + 2);
    f("qbinom", Distributions.class, 6, 11, 3 + 2);

    f("dcauchy", Distributions.class, 7, 11, 3 + 1);
    f("pcauchy", Distributions.class, 8, 11, 3 + 2);
    f("qcauchy", Distributions.class, 9, 11, 3 + 2);

    f("df", Distributions.class, 10, 11, 3 + 1);
    f("pf", Distributions.class, 11, 11, 3 + 2);
    f("qf", Distributions.class, 12, 11, 3 + 2);

    f("dgamma", Distributions.class, 13, 11, 3 + 1);
    f("pgamma", Distributions.class, 14, 11, 3 + 2);
    f("qgamma", Distributions.class, 15, 11, 3 + 2);

    f("dlnorm", /*math3*/ null, 16, 11, 3 + 1);
    f("plnorm", /*math3*/ null, 17, 11, 3 + 2);
    f("qlnorm", /*math3*/ null, 18, 11, 3 + 2);

    f("dlogis", /*math3*/ null, 19, 11, 3 + 1);
    f("plogis", /*math3*/ null, 20, 11, 3 + 2);
    f("qlogis", /*math3*/ null, 21, 11, 3 + 2);

    f("dnbinom", /*math3*/ null, 22, 11, 3 + 1);
    f("pnbinom", /*math3*/ null, 23, 11, 3 + 2);
    f("qnbinom", /*math3*/ null, 24, 11, 3 + 2);

    f("dnorm", Distributions.class, 25, 11, 3 + 1);
    f("pnorm", Distributions.class, 26, 11, 3 + 2);
    f("qnorm", Distributions.class, 27, 11, 3 + 2);

    f("dunif", Distributions.class, 28, 11, 3 + 1);
    f("punif", Distributions.class, 29, 11, 3 + 2);
    f("qunif", Distributions.class, 30, 11, 3 + 2);

    f("dweibull", Distributions.class, 31, 11, 3 + 1);
    f("pweibull", Distributions.class, 32, 11, 3 + 2);
    f("qweibull", Distributions.class, 33, 11, 3 + 2);

    f("dnchisq", /*math3*/ null, 34, 11, 3 + 1);
    f("pnchisq", /*math3*/ null, 35, 11, 3 + 2);
    f("qnchisq", /*math3*/ null, 36, 11, 3 + 2);

    f("dnt", /*math3*/ null, 37, 11, 3 + 1);
    f("pnt", /*math3*/ null, 38, 11, 3 + 2);
    f("qnt", /*math3*/ null, 39, 11, 3 + 2);

    f("dwilcox", /*math3*/ null, 40, 11, 3 + 1);
    f("pwilcox", /*math3*/ null, 41, 11, 3 + 2);
    f("qwilcox", /*math3*/ null, 42, 11, 3 + 2);

    f("besselI", /*math3*/ null, 43, 11, 3);
    f("besselK", /*math3*/ null, 44, 11, 3);

    f("dnbinom_mu", /*math3*/ null, 45, 11, 3 + 1);
    f("pnbinom_mu", /*math3*/ null, 46, 11, 3 + 2);
    f("qnbinom_mu", /*math3*/ null, 47, 11, 3 + 2);


/* Mathematical Functions of Four Numeric (+ 1-2 int) Variables */

    f("dhyper", Distribution.class, 1, 11, 4 + 1);
    f("phyper", Distribution.class, 2, 11, 4 + 2);
    f("qhyper", Distribution.class, 3, 11, 4 + 2);

    f("dnbeta", /*math4*/ null, 4, 11, 4 + 1);
    f("pnbeta", /*math4*/ null, 5, 11, 4 + 2);
    f("qnbeta", /*math4*/ null, 6, 11, 4 + 2);

    f("dnf", /*math4*/ null, 7, 11, 4 + 1);
    f("pnf", /*math4*/ null, 8, 11, 4 + 2);
    f("qnf", /*math4*/ null, 9, 11, 4 + 2);

    f("dtukey", /*math4*/ null, 10, 11, 4 + 1);
    f("ptukey", /*math4*/ null, 11, 11, 4 + 2);
    f("qtukey", /*math4*/ null, 12, 11, 4 + 2);

/* Random Numbers */

    f("rchisq", /*random1*/ null, 0, 11, 2);
    f("rexp", /*random1*/ null, 1, 11, 2);
    f("rgeom", /*random1*/ null, 2, 11, 2);
    f("rpois", /*random1*/ null, 3, 11, 2);
    f("rt", /*random1*/ null, 4, 11, 2);
    f("rsignrank", /*random1*/ null, 5, 11, 2);

    f("rbeta", /*random2*/ null, 0, 11, 3);
    f("rbinom", /*random2*/ null, 1, 11, 3);
    f("rcauchy", /*random2*/ null, 2, 11, 3);
    f("rf", /*random2*/ null, 3, 11, 3);
    f("rgamma", /*random2*/ null, 4, 11, 3);
    f("rlnorm", /*random2*/ null, 5, 11, 3);
    f("rlogis", /*random2*/ null, 6, 11, 3);
    f("rnbinom", /*random2*/ null, 7, 11, 3);
    f("rnbinom_mu", /*random2*/ null, 13, 11, 3);
    f("rnchisq", /*random2*/ null, 12, 11, 3);
    f("rnorm", /*random2*/ null, 8, 11, 3);
    f("runif", /*random2*/ null, 9, 11, 3);
    f("rweibull", /*random2*/ null, 10, 11, 3);
    f("rwilcox", /*random2*/ null, 11, 11, 3);

    f("rhyper", /*random3*/ null, 0, 11, 4);

    f("rmultinom", /*rmultinom*/ null, 0, 11, 3);
    f("sample", /*sample*/ null, 0, 11, 4);

    f("RNGkind", /*RNGkind*/ null, 0, 11, 2);
    f("set.seed", /*setseed*/ null, 0, 11, 3);

/* Data Summaries */
/* sum, min, max, prod, range are group generic and so need to eval args */
    f("sum", Summary.class, 0, 1, -1);
    f("mean", Summary.class, 1, 11, 1);
    f("min", Summary.class, 2, 1, -1);
    f("max", Summary.class, 3, 1, -1);
    f("prod", Summary.class, 4, 1, -1);
    f("range", Summary.class, 0, 1, -1);
    f("cov", Summary.class, 0, 11, 4);
    f("cor", Summary.class, 1, 11, 4);

/* Note that the number of arguments in this group only applies
   to the default method */
    f("cumsum", /*cum*/ null, 1, 1, 1);
    f("cumprod", /*cum*/ null, 2, 1, 1);
    f("cummax", /*cum*/ null, 3, 1, 1);
    f("cummin", /*cum*/ null, 4, 1, 1);

/* Type coercion */

    f("as.character", Types.class, "asCharacter", 0, 1, -1);
    f("as.integer", Types.class, "asInteger", 1, 1, -1);
    f("as.double", Types.class, "asDouble",  2, 1, -1);
    f("as.complex", /*ascharacter*/ null, 3, 1, -1);
    f("as.logical", Types.class, "asLogical", 4, 1, -1);
    f("as.raw", /*ascharacter*/ null, 5, 1, 1);
    f("as.vector", Types.class, 0, 11, 2);
    f("paste", Text.class, 0, 11, 3);
    f("file.path", Text.class, 0, 11, 2);
    f("format", /*format*/ null, 0, 11, 8);
    f("format.info", /*formatinfo*/ null, 0, 11, 3);
    f("cat", Connections.class, 0, 111, 6);
    f("call", Evaluation.class, 0, 0, -1);
    f("do.call", Evaluation.class, 0, 211, 3);
    f("as.call", /*ascall*/ null, 0, 1, 1);
    f("type.convert", /*typecvt*/ null, 1, 11, 4);
    f("as.environment", Types.class, "asEnvironment", 0, 1, 1);
    f("storage.mode<-", Types.class, 0, 1, 2);


/* String Manipulation */

    f("nchar", Text.class, 1, 11, 3);
    f("nzchar", Text.class, 1, 1, 1);
    f("substr", Text.class, 1, 11, 3);
    f("substr<-", /*substrgets*/ null, 1, 11, 4);
    f("strsplit", Text.class, 1, 11, 6);
    f("abbreviate", /*abbrev*/ null, 1, 11, 3);
    f("make.names", Text.class, 0, 11, 2);
    f("grep", Text.class, 0, 11, 9);
    f("grepl", Text.class, 1, 11, 9);
    f("sub", Text.class, 0, 11, 8);
    f("gsub", Text.class, 1, 11, 8);
    f("regexpr", /*regexpr*/ null, 1, 11, 7);
    f("gregexpr", /*gregexpr*/ null, 1, 11, 7);
    f("agrep", Text.class, 1, 11, 9);
    f("tolower", Text.class, 0, 11, 1);
    f("toupper", Text.class, 1, 11, 1);
    f("chartr", Text.class, 1, 11, 3);
    f("sprintf", Text.class, 1, 11, -1);
    f("make.unique", Text.class, 0, 11, 2);
    f("charToRaw", /*charToRaw*/ null, 1, 11, 1);
    f("rawToChar", /*rawToChar*/ null, 1, 11, 2);
    f("rawShift", /*rawShift*/ null, 1, 11, 2);
    f("intToBits", /*intToBits*/ null, 1, 11, 1);
    f("rawToBits", /*rawToBits*/ null, 1, 11, 1);
    f("packBits", /*packBits*/ null, 1, 11, 2);
    f("utf8ToInt", /*utf8ToInt*/ null, 1, 11, 1);
    f("intToUtf8", /*intToUtf8*/ null, 1, 11, 2);
    f("encodeString", /*encodeString*/ null, 1, 11, 5);
    f("iconv", /*iconv*/ null, 0, 11, 5);
    f("strtrim", /*strtrim*/ null, 0, 11, 2);

/* Type Checking (typically implemented in ./coerce.c ) */

    f("is.null", Types.class, "isNull", 0 /*NILSXP*/, 1, 1);
    f("is.logical", Types.class, "isLogical" ,0 /*LGLSXP*/, 1, 1);
    f("is.integer", Types.class, "isInteger", 0 /*INTSXP*/, 1, 1);
    f("is.real", Types.class, "isReal", 0/*REALSXP */, 1, 1);
    f("is.double", Types.class, "isDouble", 0 /*REALSXP*/, 1, 1);
    f("is.complex", Types.class, "isComplex", 0/*CPLXSXP*/, 1, 1);
    f("is.character", Types.class,"isCharacter", 0 /*STRSXP*/, 1, 1);
    f("is.symbol", Types.class, "isSymbol", 0 /*SYMSXP*/, 1, 1);
    f("is.environment", Types.class, "isEnvironment", 0/* ENVSXP */, 1, 1);
    f("is.list", Types.class,"isList", 0/* VECSXP */, 1, 1);
    f("is.pairlist", Types.class, "isPairList", 0 /*LISTSXP */, 1, 1);
    f("is.expression", Types.class, "isExpression",  0 /* EXPRSXP*/, 1, 1);
    f("is.raw", /*is*/ null,0 /* RAWSXP */, 1, 1);

    f("is.object", Types.class, 50, 1, 1);

    f("is.numeric", Types.class, "isNumeric", 100, 1, 1);
    f("is.matrix", Types.class, 101, 1, 1);
    f("is.array", Types.class, 102, 1, 1);

    f("is.atomic", Types.class, "isAtomic", 200, 1, 1);
    f("is.recursive", Types.class, "isRecursive",201, 1, 1);

    f("is.call",  Types.class, "isCall", 300, 1, 1);
    f("is.language", Types.class, "isLanguage",  301, 1, 1);
    f("is.function", Types.class, "isFunction", 302, 1, 1);

    f("is.single", Types.class,"isSingle", 999, 1, 1);

    f("is.vector", Types.class, 0, 11, 2);
    f("is.na", Types.class, "isNA", 0, 1, 1);
    f("is.nan", Types.class, "isNaN", 0, 1, 1);
    f("is.finite", Types.class, "isFinite", 0, 1, 1);
    f("is.infinite", Types.class, "isInfinite",  0, 1, 1);


/* Miscellaneous */

    f("proc.time", /*proctime*/ null, 0, 1, 0);
    f("gc.time", /*gctime*/ null, 0, 1, -1);
    f("Version", System.class, 0, 11, 0);
    f("machine", /*machine*/ null, 0, 11, 0);
    f("commandArgs", System.class, 0, 11, 0);
    f("unzip", Files.class, 0, 111, 6);
    f("parse", Evaluation.class, 0, 11, 6);
    f("parse_Rd", /*parseRd*/ null, 0, 11, 7);
    f("save", /*save*/ null, 0, 111, 6);
    f("saveToConn", /*saveToConn*/ null, 0, 111, 6);
    f("load", /*load*/ null, 0, 111, 2);
    f("loadFromConn2", Connections.class, 0, 111, 2);
    f("serializeToConn", /*serializeToConn*/ null, 0, 111, 5);
    f("unserializeFromConn", Connections.class, 0, 111, 2);
    f("deparse", Deparse.class, 0, 11, 5);
    f("deparseRd", /*deparseRd*/ null, 0, 11, 2);
    f("dput", /*dput*/ null, 0, 111, 3);
    f("dump", /*dump*/ null, 0, 111, 5);
    add(new SubstituteFunction());
    f("quote", Evaluation.class, 0, 0, 1);
    f("quit", /*quit*/ null, 0, 111, 3);
    f("interactive", Evaluation.class, 0, 0, 0);
    f("readline", /*readln*/ null, 0, 11, 1);
    f("menu", /*menu*/ null, 0, 11, 1);
    f("print.default", /*printdefault*/ null, 0, 111, 9);
    f("print.function", /*printfunction*/ null, 0, 111, 3);
    f("prmatrix", /*prmatrix*/ null, 0, 111, 6);
    f("invisible", Types.class, 0, 101, 1);
    f("gc", /*gc*/ null, 0, 11, 2);
    f("gcinfo", /*gcinfo*/ null, 0, 11, 1);
    f("gctorture", /*gctorture*/ null, 0, 11, 1);
    f("memory.profile", /*memoryprofile*/ null, 0, 11, 0);
    f("rep", Sequences.class, 0, 0, -1);
    f("rep.int", Sequences.class, 0, 11, 2);
    f("seq.int", Sequences.class, 0, 0, -1);
    f("seq_len", Sequences.class, 0, 1, 1);
    f("seq_along", Sequences.class, "seqAlong", 0, 1, 1);
    f("list", Types.class, "list", 1, 1, -1);
    f("split", /*split*/ null, 0, 11, 2);
    f("is.loaded", /*isloaded*/ null, 0, 11, -1, PP_FOREIGN, PREC_FN, 0);
    f(".C", /*dotCode*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Fortran", /*dotCode*/ null, 1, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".External", /*External*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Call", Evaluation.class, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".External.graphics", /*Externalgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Call.graphics", /*dotcallgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f("recordGraphics", /*recordGraphics*/ null, 0, 211, 3, PP_FOREIGN, PREC_FN, 0);
    f("dyn.load", System.class, 0, 111, 4);
    f("dyn.unload", System.class, 0, 111, 1);
    f("ls", Types.class, 1, 11, 2);
    f("typeof", Types.class, 1, 11, 1);
    f("eval", Evaluation.class, 0, 211, 3);
    f("eval.with.vis", /*eval*/ null, 1, 211, 3);
    f("withVisible", /*withVisible*/ null, 1, 10, 1);
    f("expression", /*expression*/ null, 1, 0, -1);
    f("sys.parent", Contexts.class, 1, 11, -1);
    f("sys.call", Contexts.class, 2, 11, -1);
    f("sys.frame", Contexts.class, 3, 11, -1);
    f("sys.nframe", Contexts.class, 4, 11, -1);
    f("sys.calls", Contexts.class, 5, 11, -1);
    f("sys.frames", Contexts.class, 6, 11, -1);
    f("sys.on.exit", Contexts.class, 7, 11, -1);
    f("sys.parents", Contexts.class, 8, 11, -1);
    f("sys.function", Contexts.class, 9, 11, -1);
    f("browserText", /*sysbrowser*/ null, 1, 11, 1);
    f("browserCondition", /*sysbrowser*/ null, 2, 11, 1);
    f("browserSetDebug", /*sysbrowser*/ null, 3, 111, 1);
    f("parent.frame", Contexts.class, "parentFrame", 0, 11, -1);
    f("sort", Sort.class, 1, 11, 2);
    f("is.unsorted", /*isunsorted*/ null, 0, 11, 2);
    f("psort", /*psort*/ null, 0, 11, 2);
    f("qsort", /*qsort*/ null, 0, 11, 2);
    f("radixsort", /*radixsort*/ null, 0, 11, 3);
    f("order", /*order*/ null, 0, 11, -1);
    f("rank", /*rank*/ null, 0, 11, 2);
    f("missing", Evaluation.class, "missing", 1, 0, 1);
    f("nargs", Evaluation.class, 1, 0, 0);
    f("scan", Scan.class, 0, 11, 18);
    f("count.fields", /*countfields*/ null, 0, 11, 6);
    f("readTableHead", /*readtablehead*/ null, 0, 11, 6);
    f("t.default", /*transpose*/ null, 0, 11, 1);
    f("aperm", Combine.class, 0, 11, 3);
    f("builtins", /*builtins*/ null, 0, 11, 1);
    f("edit", /*edit*/ null, 0, 11, 4);
    f("dataentry", /*dataentry*/ null, 0, 11, 2);
    f("dataviewer", /*dataviewer*/ null, 0, 111, 2);
    f("args", /*args*/ null, 0, 11, 1);
    f("formals", Types.class, 0, 11, 1);
    f("body", /*body*/ null, 0, 11, 1);
    f("bodyCode", /*bodyCode*/ null, 0, 11, 1);
    f("emptyenv", /*emptyenv*/ null, 0, 1, 0);
    f("baseenv", Types.class, "baseEnv", 0, 1, 0);
    f("globalenv", Types.class, "globalEnv", 0, 1, 0);
    f("environment", Types.class, 0, 11, 1);
    f("environment<-", Types.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("environmentName", /*envirName*/ null, 0, 11, 1);
    f("env2list", /*env2list*/ null, 0, 11, 2);
    f("reg.finalizer", /*regFinaliz*/ null, 0, 11, 3);
    f("options", Types.class, 0, 211, 1);
    f("sink", /*sink*/ null, 0, 111, 4);
    f("sink.number", /*sinknumber*/ null, 0, 11, 1);
    f("lib.fixup", Types.class, 0, 111, 2);
    f("pos.to.env", /*pos2env*/ null, 0, 1, 1);
    f("eapply", /*eapply*/ null, 0, 10, 4);
    f("lapply", Evaluation.class, 0, 10, 2);
    f("rapply", /*rapply*/ null, 0, 11, 5);
    f("islistfactor",  Types.class, 0, 11, 2);
    f("colSums", /*colsum*/ null, 0, 11, 4);
    f("colMeans", /*colsum*/ null, 1, 11, 4);
    f("rowSums", /*colsum*/ null, 2, 11, 4);
    f("rowMeans", /*colsum*/ null, 3, 11, 4);
    f("Rprof", /*Rprof*/ null, 0, 11, 4);
    f("Rprofmem", /*Rprofmem*/ null, 0, 11, 3);
    f("tracemem", /*memtrace*/ null, 0, 1, 1);
    f("retracemem", /*memretrace*/ null, 0, 1, -1);
    f("untracemem", /*memuntrace*/ null, 0, 101, 1);
    f("object.size", /*objectsize*/ null, 0, 11, 1);
    f("inspect", /*inspect*/ null, 0, 111, 1);
    f("mem.limits", /*memlimits*/ null, 0, 11, 2);
    f("merge", /*merge*/ null, 0, 11, 4);
    f("capabilities", System.class, 0, 11, 0);
    f("capabilitiesX11", /*capabilitiesX11*/ null, 0, 11, 0);
    f("new.env", Types.class, "newEnv", 0, 11, 3);
    f("parent.env", Types.class, 0, 11, 1);
    f("parent.env<-", Types.class, 0, 11, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("visibleflag", /*visibleflag*/ null, 0, 1, 0);
    f("l10n_info", /*l10n_info*/ null, 0, 11, 0);
    f("Cstack_info", /*Cstack_info*/ null, 0, 11, 0);
    f("startHTTPD", /*startHTTPD*/ null, 0, 11, 2);
    f("stopHTTPD", /*stopHTTPD*/ null, 0, 11, 0);

/* Functions To Interact with the Operating System */

    f("file.show", /*fileshow*/ null, 0, 111, 5);
    f("file.edit", /*fileedit*/ null, 0, 111, 3);
    f("file.create", /*filecreate*/ null, 0, 11, 2);
    f("file.remove", /*fileremove*/ null, 0, 11, 1);
    f("file.rename", /*filerename*/ null, 0, 11, 2);
    f("file.append", /*fileappend*/ null, 0, 11, 2);
    f("codeFiles.append", /*fileappend*/ null, 1, 11, 2);
    f("file.symlink", /*filesymlink*/ null, 0, 11, 2);
    f("file.copy", /*filecopy*/ null, 0, 11, 4);
    f("list.files", Files.class, 0, 11, 6);
    f("file.exists", Files.class, 0, 11, 1);
    f("file.choose", /*filechoose*/ null, 0, 11, 1);
    f("file.info", Files.class, 0, 11, 1);
    f("file.access", /*fileaccess*/ null, 0, 11, 2);
    f("dir.create", Files.class, 0, 11, 4);
    f("tempfile", Files.class, 0, 11, 2);
    f("tempdir", Files.class, 0, 11, 0);
    f("R.home", System.class, "getRHome", 0, 11, 0);
    f("date", /*date*/ null, 0, 11, 0);
    f("index.search", /*indexsearch*/ null, 0, 11, 5);
    f("Sys.getenv", System.class, 0, 11, 2);
    f("Sys.setenv", System.class, 0, 111, 2);
    f("Sys.unsetenv", /*unsetenv*/ null, 0, 111, 1);
    f("getwd", Files.class, 0, 11, 0);
    f("setwd", Files.class, 0, 111, 1);
    f("basename", Files.class, 0, 11, 1);
    f("dirname", Files.class, 0, 11, 1);
    f("dirchmod", /*dirchmod*/ null, 0, 111, 1);
    f("Sys.chmod", /*syschmod*/ null, 0, 111, 2);
    f("Sys.umask", /*sysumask*/ null, 0, 111, 1);
    f("Sys.readlink", /*readlink*/ null, 0, 11, 1);
    f("Sys.info", /*sysinfo*/ null, 0, 11, 0);
    f("Sys.sleep", /*syssleep*/ null, 0, 11, 1);
    f("Sys.getlocale", System.class, 0, 11, 1);
    f("Sys.setlocale", /*setlocale*/ null, 0, 11, 2);
    f("Sys.localeconv", /*localeconv*/ null, 0, 11, 0);
    f("path.expand", Files.class, "pathExpand", 0, 11, 1);
    f("Sys.getpid", /*sysgetpid*/ null, 0, 11, 0);
    f("normalizePath", /*normalizepath*/ null, 0, 11, 1);
    f("Sys.glob", Files.class, "glob", 0, 11, 2);
    f("unlink", Files.class, 0, 111, 2);

/* Complex Valued Functions */
    f("fft", /*fft*/ null, 0, 11, 2);
    f("mvfft", /*mvfft*/ null, 0, 11, 2);
    f("nextn", /*nextn*/ null, 0, 11, 2);
    f("polyroot", /*polyroot*/ null, 0, 11, 1);

/* Device Drivers */


/* Graphics */

    f("dev.control", /*devcontrol*/ null, 0, 111, 1);
    f("dev.displaylist", /*devcontrol*/ null, 1, 111, 0);
    f("dev.copy", /*devcopy*/ null, 0, 111, 1);
    f("dev.cur", /*devcur*/ null, 0, 111, 0);
    f("dev.next", /*devnext*/ null, 0, 111, 1);
    f("dev.off", /*devoff*/ null, 0, 111, 1);
    f("dev.prev", /*devprev*/ null, 0, 111, 1);
    f("dev.set", /*devset*/ null, 0, 111, 1);
    f("rgb", /*rgb*/ null, 0, 11, 6);
    f("rgb256", /*rgb*/ null, 1, 11, 5);
    f("rgb2hsv", /*RGB2hsv*/ null, 0, 11, 1);
    f("hsv", /*hsv*/ null, 0, 11, 5);
    f("hcl", /*hcl*/ null, 0, 11, 5);
    f("gray", /*gray*/ null, 0, 11, 1);
    f("colors", /*colors*/ null, 0, 11, 0);
    f("col2rgb", /*col2RGB*/ null, 0, 11, 1);
    f("palette", /*palette*/ null, 0, 11, 1);
    f("plot.new", /*plot_new*/ null, 0, 111, 0);
    f("plot.window", /*plot_window*/ null, 0, 111, 3);
    f("axis", /*axis*/ null, 0, 111, 13);
    f("plot.xy", /*plot_xy*/ null, 0, 111, 7);
    f("text", /*text*/ null, 0, 111, -1);
    f("mtext", /*mtext*/ null, 0, 111, 5);
    f("title", /*title*/ null, 0, 111, 4);
    f("abline", /*abline*/ null, 0, 111, 6);
    f("box", /*box*/ null, 0, 111, 3);
    f("rect", /*rect*/ null, 0, 111, 6);
    f("polygon", /*polygon*/ null, 0, 111, 5);
    f("xspline", /*xspline*/ null, 0, 111, -1);
    f("par", /*par*/ null, 0, 11, 1);
    f("segments", /*segments*/ null, 0, 111, -1);
    f("arrows", /*arrows*/ null, 0, 111, -1);
    f("layout", /*layout*/ null, 0, 111, 10);
    f("locator", /*locator*/ null, 0, 11, 2);
    f("identify", /*identify*/ null, 0, 211, 8);
    f("strheight", /*strheight*/ null, 0, 11, -1);
    f("strwidth", /*strwidth*/ null, 0, 11, -1);
    f("contour", /*contour*/ null, 0, 11, 12);
    f("contourLines", /*contourLines*/ null, 0, 11, 5);
    f("image", /*image*/ null, 0, 111, 4);
    f("dend", /*dend*/ null, 0, 111, 6);
    f("dend.window", /*dendwindow*/ null, 0, 111, 5);
    f("erase", /*erase*/ null, 0, 111, 1);
    f("persp", /*persp*/ null, 0, 111, 4);
    f("filledcontour", /*filledcontour*/ null, 0, 111, 5);
    f("getSnapshot", /*getSnapshot*/ null, 0, 111, 0);
    f("playSnapshot", /*playSnapshot*/ null, 0, 111, 1);
    f("symbols", /*symbols*/ null, 0, 111, -1);
    f("getGraphicsEvent", /*getGraphicsEvent*/ null, 0, 11, 5);
    f("devAskNewPage", /*devAskNewPage*/ null, 0, 211, 1);
    f("dev.size", /*devsize*/ null, 0, 11, 0);
    f("clip", /*clip*/ null, 0, 111, 4);
    f("grconvertX", /*convertXY*/ null, 0, 11, 3);
    f("grconvertY", /*convertXY*/ null, 1, 11, 3);

/* Objects */
    f("inherits", Types.class, 0, 11, 3);
    f("UseMethod", Evaluation.class, 0, 200, -1);
    f("NextMethod", Evaluation.class, 0, 210, -1);
    f("standardGeneric", /*standardGeneric*/ null, 0, 201, -1);

/* Modelling Functionality */

    f("nlm", /*nlm*/ null, 0, 11, 11);
    f("fmin", /*fmin*/ null, 0, 11, 4);
    f("zeroin", /*zeroin*/ null, 0, 11, 5);
    f("zeroin2", /*zeroin2*/ null, 0, 11, 7);
    f("optim", /*optim*/ null, 0, 11, 7);
    f("optimhess", /*optimhess*/ null, 0, 11, 4);
    f("terms.formula", Models.class, 0, 11, 5);
    f("update.formula", /*updateform*/ null, 0, 11, 2);
    f("model.frame", Models.class, 0, 11, 8);
    f("model.matrix", /*modelmatrix*/ null, 0, 11, 2);

    f("D", /*D*/ null, 0, 11, 2);
    f("deriv.default", /*deriv*/ null, 0, 11, 5);

/* History manipulation */
    f("loadhistory", /*loadhistory*/ null, 0, 11, 1);
    f("savehistory", /*savehistory*/ null, 0, 11, 1);
    f("addhistory", /*addhistory*/ null, 0, 11, 1);

/* date-time manipulations */
    f("Sys.time", System.class, "sysTime", 0, 11, 0);
    f("as.POSIXct", /*asPOSIXct*/ null, 0, 11, 2);
    f("as.POSIXlt", /*asPOSIXlt*/ null, 0, 11, 2);
    f("format.POSIXlt", /*formatPOSIXlt*/ null, 0, 11, 3);
    f("strptime", DateTime.class,/*strptime*/  0, 11, 3);
    f("Date2POSIXlt", /*D2POSIXlt*/ null, 0, 11, 1);
    f("POSIXlt2Date", /*POSIXlt2D*/ null, 0, 11, 1);


/* Connections */
    f("stdin", Connections.class, 0, 11, 0);
    f("stdout", Connections.class, 0, 11, 0);
    f("stderr", Connections.class, 0, 11, 0);
    f("readLines", /*readLines*/ null, 0, 11, 5);
    f("writeLines", /*writelines*/ null, 0, 11, 4);
    f("readBin", /*readbin*/ null, 0, 11, 6);
    f("writeBin", /*writebin*/ null, 0, 211, 5);
    f("readChar", Connections.class, 0, 11, 3);
    f("writeChar", /*writechar*/ null, 0, 211, 5);
    f("open", /*open*/ null, 0, 11, 3);
    f("isOpen", /*isopen*/ null, 0, 11, 2);
    f("isIncomplete", /*isincomplete*/ null, 0, 11, 1);
    f("isSeekable", /*isseekable*/ null, 0, 11, 1);
    f("close", Connections.class, 0, 11, 2);
    f("flush", /*flush*/ null, 0, 11, 1);
    f("file", Connections.class, 1, 11, 4);
    f("url", /*url*/ null, 0, 11, 4);
    f("pipe", /*pipe*/ null, 0, 11, 3);
    f("fifo", /*fifo*/ null, 0, 11, 4);
    f("gzfile", Connections.class, 0, 11, 4);
    f("bzfile", /*gzfile*/ null, 1, 11, 4);
    f("xzfile", /*gzfile*/ null, 2, 11, 4);
    f("unz", /*unz*/ null, 0, 11, 3);
    f("seek", /*seek*/ null, 0, 11, 4);
    f("truncate", /*truncate*/ null, 0, 11, 1);
    f("pushBack", /*pushback*/ null, 0, 11, 3);
    f("clearPushBack", /*clearpushback*/ null, 0, 11, 1);
    f("pushBackLength", /*pushbacklength*/ null, 0, 11, 1);
    f("rawConnection", /*rawconnection*/ null, 0, 11, 3);
    f("rawConnectionValue", /*rawconvalue*/ null, 0, 11, 1);
    f("textConnection", /*textconnection*/ null, 0, 11, 5);
    f("textConnectionValue", /*textconvalue*/ null, 0, 11, 1);
    f("socketConnection", /*sockconn*/ null, 0, 11, 6);
    f("sockSelect", /*sockselect*/ null, 0, 11, 3);
    f("getConnection", /*getconnection*/ null, 0, 11, 1);
    f("getAllConnections", /*getallconnections*/ null, 0, 11, 0);
    f("summary.connection", /*sumconnection*/ null, 0, 11, 1);
    f("download", /*download*/ null, 0, 11, 5);
    f("nsl", /*nsl*/ null, 0, 11, 1);
    f("gzcon", /*gzcon*/ null, 0, 11, 3);
    f("memCompress", /*memCompress*/ null, 0, 11, 2);
    f("memDecompress", /*memDecompress*/ null, 0, 11, 2);

    f("readDCF", /*readDCF*/ null, 0, 11, 2);

    f("getNumRtoCConverters", /*getNumRtoCConverters*/ null, 0, 11, 0);
    f("getRtoCConverterDescriptions", /*getRtoCConverterDescriptions*/ null, 0, 11, 0);
    f("getRtoCConverterStatus", /*getRtoCConverterStatus*/ null, 0, 11, 0);
    f("setToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 0, 11, 2);
    f("removeToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 1, 11, 1);

    f("lockEnvironment", Types.class, 0, 111, 2);
    f("environmentIsLocked", Types.class, 0, 11, 1);
    f("lockBinding", Types.class, 0, 111, 2);
    f("unlockBinding", Types.class, 1, 111, 2);
    f("bindingIsLocked", Types.class, 0, 11, 2);
    f("makeActiveBinding", /*mkActiveBnd*/ null, 0, 111, 3);
    f("bindingIsActive", /*bndIsActive*/ null, 0, 11, 2);
/* looks like mkUnbound is unused in base R */
    f("mkUnbound", /*mkUnbound*/ null, 0, 111, 1);
    f("isNamespaceEnv", Namespaces.class, 0, 11, 1);
    f("registerNamespace", Namespaces.class, 0, 11, 2);
    f("unregisterNamespace", Namespaces.class, 0, 11, 1);
    f("getRegisteredNamespace", Namespaces.class, 0, 11, 1);
    f("getNamespaceRegistry", Namespaces.class, 0, 11, 0);
    f("importIntoEnv", Namespaces.class, 0, 11, 4);
    f("env.profile", /*envprofile*/ null, 0, 211, 1);

    f("write.table", /*writetable*/ null, 0, 111, 11);
    f("Encoding", Types.class, 0, 11, 1);
    f("setEncoding", Types.class, 0, 11, 2);
    f("lazyLoadDBfetch", Connections.class, 0, 1, 4);
    f("setTimeLimit", /*setTimeLimit*/ null, 0, 111, 3);
    f("setSessionTimeLimit", /*setSessionTimeLimit*/ null, 0, 111, 2);
    f("icuSetCollate", /*ICUset*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0) ;

  }

  private void add(BuiltinFunction fn) {
    builtins.put(new Symbol(fn.getName()), fn);
  }

  private void addInternal(String name, Function fn) {
    internals.put(new Symbol(name), fn);
  }

  private void addInternal(SpecialFunction fn) {
    internals.put(new Symbol(fn.getName()), fn);
  }

  private void f(String name, Class cfun, Object offset, int eval, int arity, PPkind kind, PPprec prec, int rightassoc) {
    Entry e = new Entry(name, cfun, name, offset, eval, arity, new PPinfo(kind, prec, rightassoc));
    add(e);
  }

  private void f(String name, Class cfun, String methodName, Object offset, int eval, int arity, PPkind kind, PPprec prec, int rightassoc) {
    Entry e = new Entry(name, cfun,methodName, offset, eval, arity, new PPinfo(kind, prec, rightassoc));
    add(e);
  }

  private void f(String name, Class clazz, Object offset, int eval, int arity) {
    Entry e = new Entry();
    e.name = name;
    e.functionClass = clazz;
    e.code = offset;
    e.eval = eval;
    e.arity = arity;
    e.gram = new PPinfo(PP_FUNCALL, PPprec.PREC_RIGHT, 0);
    add(e);
  }

  private void f(String name, Class clazz, String methodName, Object offset, int eval, int arity) {
    Entry e = new Entry();
    e.name = name;
    e.functionClass = clazz;
    e.methodName = methodName;
    e.code = offset;
    e.eval = eval;
    e.arity = arity;
    e.gram = new PPinfo(PP_FUNCALL, PPprec.PREC_RIGHT, 0);
    add(e);
  }


  public static class PPinfo {
    /**
     * deparse kind
     */
    public PPkind kind;

    /**
     * operator precedence
     */
    public PPprec precedence;

    /**
     * right associative?
     */
    public int rightassoc;

    private PPinfo(PPkind kind, PPprec precedence, int rightassoc) {
      this.kind = kind;
      this.precedence = precedence;
      this.rightassoc = rightassoc;
    }
  }

  public static class Entry {

    private Entry() {

    }

    private Entry(String name, Class functionClass, String methodName, Object code, int eval, int arity, PPinfo gram) {
      this.name = name;
      this.functionClass = functionClass;
      this.methodName = methodName;
      this.code = code;
      this.eval = eval;
      this.arity = arity;
      this.gram = gram;
    }

    /**
     * print name
     */
    public String name;

    public String group;

    /* c-code address */
    public Class functionClass;
    public String methodName;
    public Object code;     /* offset within c-code */
    public int eval;     /* evaluate args? */
    public int arity;    /* function arity */
    public PPinfo gram;     /* pretty-print info */

    public Entry group(String groupName) {
      this.group = groupName;
      return this;
    }

    public boolean isSpecial() {
      return eval % 10 == 0;
    }

    public boolean isGroupGeneric() {
      return group != null;
    }
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("The base frame cannot be cleared");
    
  }
}
