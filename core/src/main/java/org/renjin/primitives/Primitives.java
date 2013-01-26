package org.renjin.primitives;

import static org.renjin.primitives.PPkind.PP_BINARY;
import static org.renjin.primitives.PPkind.PP_BINARY2;
import static org.renjin.primitives.PPkind.PP_DOLLAR;
import static org.renjin.primitives.PPkind.PP_FOREIGN;
import static org.renjin.primitives.PPkind.PP_FUNCALL;
import static org.renjin.primitives.PPkind.PP_FUNCTION;
import static org.renjin.primitives.PPkind.PP_SUBASS;
import static org.renjin.primitives.PPkind.PP_SUBSET;
import static org.renjin.primitives.PPkind.PP_UNARY;
import static org.renjin.primitives.PPprec.PREC_AND;
import static org.renjin.primitives.PPprec.PREC_COLON;
import static org.renjin.primitives.PPprec.PREC_COMPARE;
import static org.renjin.primitives.PPprec.PREC_DOLLAR;
import static org.renjin.primitives.PPprec.PREC_FN;
import static org.renjin.primitives.PPprec.PREC_LEFT;
import static org.renjin.primitives.PPprec.PREC_NOT;
import static org.renjin.primitives.PPprec.PREC_OR;
import static org.renjin.primitives.PPprec.PREC_PERCENT;
import static org.renjin.primitives.PPprec.PREC_POWER;
import static org.renjin.primitives.PPprec.PREC_PROD;
import static org.renjin.primitives.PPprec.PREC_SUBSET;
import static org.renjin.primitives.PPprec.PREC_SUM;
import static org.renjin.primitives.PPprec.PREC_TILDE;
import static org.renjin.util.CDefines.RelOpType.EQOP;
import static org.renjin.util.CDefines.RelOpType.GEOP;
import static org.renjin.util.CDefines.RelOpType.GTOP;
import static org.renjin.util.CDefines.RelOpType.LEOP;
import static org.renjin.util.CDefines.RelOpType.LTOP;
import static org.renjin.util.CDefines.RelOpType.NEOP;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.distribution.Distribution;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.methods.Methods;
import org.renjin.primitives.annotations.processor.WrapperGenerator2;
import org.renjin.primitives.files.Files;
import org.renjin.primitives.graphics.Graphics;
import org.renjin.primitives.graphics.Par;
import org.renjin.primitives.graphics.Plot;
import org.renjin.primitives.graphics.RgbHsv;
import org.renjin.primitives.io.Cat;
import org.renjin.primitives.io.DebianControlFiles;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.serialization.Serialization;
import org.renjin.primitives.match.Duplicates;
import org.renjin.primitives.match.Match;
import org.renjin.primitives.matrix.Matrices;
import org.renjin.primitives.models.Models;
import org.renjin.primitives.optimize.Optimizations;
import org.renjin.primitives.optimize.Roots;
import org.renjin.primitives.packaging.Namespaces;
import org.renjin.primitives.packaging.Packages;
import org.renjin.primitives.random.Distributions;
import org.renjin.primitives.random.RNG;
import org.renjin.primitives.random.Sampling;
import org.renjin.primitives.special.AssignFunction;
import org.renjin.primitives.special.AssignLeftFunction;
import org.renjin.primitives.special.BeginFunction;
import org.renjin.primitives.special.BreakFunction;
import org.renjin.primitives.special.ClosureFunction;
import org.renjin.primitives.special.ForFunction;
import org.renjin.primitives.special.IfFunction;
import org.renjin.primitives.special.InternalFunction;
import org.renjin.primitives.special.NextFunction;
import org.renjin.primitives.special.OnExitFunction;
import org.renjin.primitives.special.ParenFunction;
import org.renjin.primitives.special.QuoteFunction;
import org.renjin.primitives.special.ReassignLeftFunction;
import org.renjin.primitives.special.RecallFunction;
import org.renjin.primitives.special.RepeatFunction;
import org.renjin.primitives.special.RestartFunction;
import org.renjin.primitives.special.ReturnFunction;
import org.renjin.primitives.special.SubstituteFunction;
import org.renjin.primitives.special.SwitchFunction;
import org.renjin.primitives.special.WhileFunction;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.primitives.text.Text;
import org.renjin.primitives.time.Time;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SpecialFunction;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Primitives {

  private IdentityHashMap<Symbol, PrimitiveFunction> builtins = new IdentityHashMap<Symbol, PrimitiveFunction>();
  private IdentityHashMap<Symbol, PrimitiveFunction> internals = new IdentityHashMap<Symbol, PrimitiveFunction>();

  // these are loaded on demand
  private IdentityHashMap<Symbol, Entry> builtinEntries = new IdentityHashMap<Symbol, Entry>();
  private IdentityHashMap<Symbol, Entry> internalEntries = new IdentityHashMap<Symbol, Entry>();
  
  
  private static final Primitives INSTANCE = new Primitives();
  
  public static PrimitiveFunction getBuiltin(String name) {
    return getBuiltin(Symbol.get(name));
  }
  
  public static PrimitiveFunction getBuiltin(Symbol symbol) {
    PrimitiveFunction fn = INSTANCE.builtins.get(symbol);
    if(fn == null) {
      Entry entry = INSTANCE.builtinEntries.get(symbol);
      if(entry != null) {
        fn = createFunction(entry);
        INSTANCE.builtins.put(symbol, fn);
      }
    }
    return fn;
  }
  
  public static PrimitiveFunction getInternal(Symbol symbol) {
    PrimitiveFunction fn = INSTANCE.internals.get(symbol);
    if(fn == null) {
      Entry entry = INSTANCE.internalEntries.get(symbol);
      if(entry != null) {
        fn = createFunction(entry);
        INSTANCE.internals.put(symbol, fn);
      }
    }
    return fn;
  }
  
  public static List<Entry> getEntries() {
    List<Entry> set = Lists.newArrayList();
    set.addAll(INSTANCE.internalEntries.values());
    set.addAll(INSTANCE.builtinEntries.values());
    return set;
  }
  
  public static Set<Symbol> getBuiltinSymbols() {
    return Sets.union(INSTANCE.builtins.keySet(), INSTANCE.builtinEntries.keySet());
  }
  
  private static PrimitiveFunction createFunction(final Entry entry) {
   try {
      return (PrimitiveFunction) Class.forName(WrapperGenerator2.toFullJavaName(entry.name)).newInstance();
    } catch(Exception e) {
      return new BuiltinFunction(entry.name) {

        @Override
        public SEXP apply(Context context, Environment rho,
            FunctionCall call, PairList args) {
          throw new EvalException("Sorry! " + entry.name + " not yet implemented!");
        }
      };
    }
  }
  
   
  public Primitives() {
    add(new IfFunction());
    add(new WhileFunction());
    add(new ForFunction());
    add(new RepeatFunction());
    add(new BreakFunction());
    add(new NextFunction());
    add(new ReturnFunction());

    f("stop", Conditions.class, 0, 11, 2);
    f("warning", Warning.class, 0, 111, 3);

    f("gettext", Text.class, 0, 11, 2);
    f("ngettext", Text.class, 0, 11, 4);
    f("bindtextdomain", Text.class, 0, 11, 2);
    f(".addCondHands", Conditions.class, 0, 111, 5);
    f(".resetCondHands", /*resetCondHands*/ null, 0, 111, 1);
    f(".signalCondition", Conditions.class, 0, 11, 3);
    f(".dfltStop", Conditions.class, 0, 11, 2);
    f(".dfltWarn", /*dfltWarn*/ null, 0, 11, 2);
    f(".addRestart", Conditions.class, 0, 11, 1);
    f(".getRestart", /*getRestart*/ null, 0, 11, 1);
    f(".invokeRestart", /*invokeRestart*/ null, 0, 11, 2);
    f(".addTryHandlers", /*addTryHandlers*/ null, 0, 111, 0);

    f("geterrmessage", Conditions.class, 0, 11, 0);
    f("seterrmessage", Conditions.class, 0, 111, 1);
    f("printDeferredWarnings", Warning.class, 0, 111, 0);
    f("interruptsSuspended", /*interruptsSuspended*/ null, 0, 11, -1);

    addInternal("restart", new RestartFunction());
    add(new ClosureFunction());

    f("as.function.default", Types.class, 0, 11, 2, PP_FUNCTION, PREC_FN, 0);

    add(new AssignLeftFunction());
    add(new AssignFunction());
    
    add(new ReassignLeftFunction());
    add(new BeginFunction());
    add(new ParenFunction());

    f(".subset", Subsetting.class, 1, 1, -1);
    f(".subset2", Subsetting.class, 2, 1, -1);
    f("[",Subsetting.class, 1, 0, -1, PP_SUBSET, PREC_SUBSET, 0);
    f("[[", Subsetting.class, 2, 0, -1, PP_SUBSET, PREC_SUBSET, 0);
    f("$", Subsetting.class, 3, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0);
    f("@", Subsetting.class, 0, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0);
    f("[<-", Subsetting.class, 0, 0, 3, PP_SUBASS, PREC_LEFT, 1);
    f("[[<-", Subsetting.class, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1);
    f("$<-", Subsetting.class, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1);

    add(new SwitchFunction());

    f("browser", /*browser*/ null, 0, 101, 3);
    f("debug", /*debug*/ null, 0, 111, 3);
    f("undebug", /*debug*/ null, 1, 111, 1);
    f("isdebugged", /*debug*/ null, 2, 11, 1);
    f("debugonce", /*debug*/ null, 3, 111, 3);
    f(".primTrace", /*trace*/ null, 0, 101, 1);
    f(".primUntrace", /*trace*/ null, 1, 101, 1);

    add(new InternalFunction());
    add(new OnExitFunction());

    addInternal("Recall", new RecallFunction());
    f("delayedAssign", Evaluation.class, 0, 111, 4);
    f("makeLazy", Serialization.class, 0, 111, 5);
    f(".Primitive", Evaluation.class, 0, 1, 1);
    f("identical",  Types.class, 0, 11, 5);


/* Binary Operators */
/* these are group generic and so need to eval args */
    f("+",  Ops.class, 0, /* PLUSOP, */ 1, 2, PP_BINARY, PREC_SUM, 0);
    f("-", Ops.class,  0, /* MINUSOP, */ 1, 2, PP_BINARY, PREC_SUM, 0);
    f("*", Ops.class,  0,/*TIMESOP ,*/ 1, 2, PP_BINARY, PREC_PROD, 0);
    f("/", Ops.class,  0,/*DIVOP,*/ 1, 2, PP_BINARY2, PREC_PROD, 0);
    f("^", Ops.class,  0, /*POWOP,*/ 1, 2, PP_BINARY2, PREC_POWER, 1);
//    add(new OpsFunction("+"));
//    add(new OpsFunction("-"));
//    add(new OpsFunction("*"));
//    add(new OpsFunction("/"));
//    add(new OpsFunction("^"));

    f("%%", Ops.class, 0 /* MODOP */, 1, 2, PP_BINARY2, PREC_PERCENT, 0);
    f("%/%", Ops.class, 0 /* IDIVOP */, 1, 2, PP_BINARY2, PREC_PERCENT, 0);
    f("%*%", Matrices.class, 0, 1, 2, PP_BINARY, PREC_PERCENT, 0);
    f("crossprod", Matrices.class, 1, 11, 2);
    f("tcrossprod", Matrices.class, 2, 11, 2);


/* these are group generic and so need to eval args */
    f("==", Ops.class, EQOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f("!=", Ops.class, NEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f("<", Ops.class, LTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f("<=", Ops.class, LEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f(">=", Ops.class, GEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f(">", Ops.class, GTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0);
    f("&", Ops.class,  1, 1, 2, PP_BINARY, PREC_AND, 0);
    f("|", Ops.class, 2, 1, 2, PP_BINARY, PREC_OR, 0);
    f("!", Ops.class, 3, 1, 1, PP_UNARY, PREC_NOT, 0);
//    add(new OpsFunction("=="));
//    add(new OpsFunction("!="));
//    add(new OpsFunction("<"));
//    add(new OpsFunction("<="));
//    add(new OpsFunction(">"));
//    add(new OpsFunction(">="));
//    add(new OpsFunction("&"));
//    add(new OpsFunction("|"));
//    add(new OpsFunction("!"));

    f("&&", Comparison.class, "and", 1, 0, 2, PP_BINARY, PREC_AND, 0);
    f("||", Comparison.class, "or", 2, 0, 2, PP_BINARY, PREC_OR, 0);
    f(":", Sequences.class, "colon", 0, 1, 2, PP_BINARY2, PREC_COLON, 0);
    f("~", Models.class, 0, 0, 2, PP_BINARY, PREC_TILDE, 0);


/* Logic Related Functions */
/* these are group generic and so need to eval args */
    f("all", Summary.class, 1, 1, -1);
    f("any", Summary.class, 2, 1, -1);


/* Vectors, Matrices and Arrays */

/* printname  c-entry   offset  eval  arity pp-kind      precedence rightassoc
 * ---------  -------   ------  ----  ----- -------      ---------- ----------*/
    f("vector", Types.class, 0, 11, 2);
    f("complex", ComplexGroup.class, 0, 11, 3);
    f("matrix", Matrices.class, 0, 11, -1);
    f("length", Types.class, 0, 1, 1);
    f("length<-", Types.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("row", Matrices.class, 1, 11, 1);
    f("col", Matrices.class, 2, 11, 1);
    f("c", Combine.class,  0, 1, -1);
    f("unlist", Combine.class, 0, 11, 3);
    f("cbind", Combine.class, 1, 10, -1);
    f("rbind", Combine.class, 2, 10, -1);
    f("drop", Types.class, 0, 11, 1);
    f("oldClass", Attributes.class, 0, 1, 1);
    f("oldClass<-", Attributes.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("class", Attributes.class, "getClass", 0, 1, 1);
    f(".cache_class", Methods.class, 1, 1, 2, PP_FUNCALL, PREC_FN, 0);
    f("class<-", Attributes.class, "setClass", 0, 1, 2);
    f("unclass", Attributes.class, 0, 1, 1);
    f("names", Attributes.class,  "getNames", 0, 1, 1);
    f("names<-", Attributes.class, "setNames", 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("dimnames", Attributes.class, 0, 1, 1);
    f("dimnames<-", Attributes.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("all.names", AllNamesVisitor.class, 0, 11, 4);
    f("dim", Attributes.class, 0, 1, 1);
    f("dim<-", Attributes.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("attributes", Attributes.class, 0, 1, 1);
    f("attributes<-", Attributes.class, null, 0, 1, 1, PP_FUNCALL, PREC_LEFT, 1);
    f("attr", Attributes.class, 0, 1, -1);
    f("attr<-", Attributes.class, 0, 1, 3, PP_FUNCALL, PREC_LEFT, 1);
    f("comment", Attributes.class, 0, 11, 1);
    f("comment<-", Attributes.class, 0, 11, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("levels<-", Attributes.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("get", Types.class, 1, 11, 4);
    f("mget", /*mget*/ null, 1, 11, 5);
    f("exists", Types.class, 0, 11, 4);
    f("assign", Evaluation.class, 0, 111, 4);
    f("remove", Evaluation.class, 0, 111, 3);
    f("duplicated", Duplicates.class, 0, 11, 3);
    f("unique", Duplicates.class, 1, 11, 3);
    f("anyDuplicated", Duplicates.class, 2, 11, 3);
    f("which.min", Sort.class, 0, 11, 1);
    f("which", Match.class, 0, 11, 1);
    f("pmin", Summary.class, 0, 11, -1);
    f("pmax", Summary.class, 1, 11, -1);
    f("which.max", Sort.class, 1, 11, 1);
    f("match", Match.class, 0, 11, 4);
    f("pmatch", Match.class, 0, 11, 4);
    f("charmatch", Match.class, 0, 11, 3);
    f("match.call", Match.class, 0, 11, 3);
    f("complete.cases", /*compcases*/ null, 0, 11, 1);

    f("attach", Types.class, 0, 111, 3);
    f("detach", Types.class, 0, 111, 1);
    f("search", Types.class, 0, 11, 0);


/* Mathematical Functions */
/* these are group generic and so need to eval args */
/* Note that the number of arguments for the primitives in the Math group
   only applies to the default method. */
    f("round", MathExt.class, 10001, 0, -1);
    f("signif", MathExt.class, 10004, 0, -1);
    f("atan",Math.class, 10002, 1, 1);
    f("log", MathExt.class, 10003, 0, -1);
    f("log10", Math.class, 10, 1, 1);
    f("log2", MathExt.class, 2, 1, 1);
    f("abs", MathExt.class, 6, 1, 1);
    f("floor", Math.class, 1, 1, 1);
    f("ceiling", Math.class, "ceil", 2, 1, 1);
    f("sqrt", Math.class, 3, 1, 1);
    f("sign", MathExt.class, "sign", 4, 1, 1);
    f("trunc", MathExt.class, 5, 1, -1);
    
    f("exp", Math.class, 10, 1, 1);
    f("expm1", MathExt.class, 11, 1, 1);
    f("log1p", MathExt.class, 12, 1, 1);

    f("cos", Math.class, 20, 1, 1);
    f("sin", Math.class, 21, 1, 1);
    f("tan", Math.class, 22, 1, 1);
    f("acos", Math.class, 23, 1, 1);
    f("asin", Math.class, 24, 1, 1);

    f("cosh", Math.class, 30, 1, 1);
    f("sinh", Math.class, 31, 1, 1);
    f("tanh", Math.class, 32, 1, 1);
    f("acosh", MathExt.class, 33, 1, 1);
    f("asinh", MathExt.class, 34, 1, 1);
    f("atanh", MathExt.class, 35, 1, 1);

    f("lgamma", org.apache.commons.math.special.Gamma.class, "logGamma", 40, 1, 1);
    f("gamma", MathExt.class, 41, 1, 1);

    f("digamma", org.apache.commons.math.special.Gamma.class, 42, 1, 1);
    f("trigamma",org.apache.commons.math.special.Gamma.class, 43, 1, 1);
/* see "psigamma" below !*/

/* Mathematical Functions of Two Numeric (+ 1-2 int) Variables */

    f("atan2", MathExt.class, 0, 11, 2);

    f("lbeta", MathExt.class, 2, 11, 2);
    f("beta", MathExt.class, 3, 11, 2);
    f("lchoose", MathExt.class, 4, 11, 2);
    f("choose", MathExt.class, 5, 11, 2);

    f("dchisq", Distributions.class, 6, 11, 2 + 1);
    f("pchisq", Distributions.class, 7, 11, 2 + 2);
    f("qchisq", Distributions.class, 8, 11, 2 + 2);

    f("dexp", Distributions.class, 9, 11, 2 + 1);
    f("pexp", Distributions.class, 10, 11, 2 + 2);
    f("qexp", Distributions.class, 11, 11, 2 + 2);

    f("dgeom", Distributions.class, 12, 11, 2 + 1);
    f("pgeom", Distributions.class, 13, 11, 2 + 2);
    f("qgeom", Distributions.class, 14, 11, 2 + 2);

    f("dpois", Distributions.class, 15, 11, 2 + 1);
    f("ppois", Distributions.class, 16, 11, 2 + 2);
    f("qpois", Distributions.class, 17, 11, 2 + 2);

    f("dt", Distributions.class, 18, 11, 2 + 1);
    f("pt", Distributions.class, 19, 11, 2 + 2);
    f("qt", Distributions.class, 20, 11, 2 + 2);

    f("dsignrank", Distributions.class, 21, 11, 2 + 1);
    f("psignrank", Distributions.class, 22, 11, 2 + 2);
    f("qsignrank", Distributions.class, 23, 11, 2 + 2);

    f("besselJ", /*math2*/ null, 24, 11, 2);
    f("besselY", /*math2*/ null, 25, 11, 2);

    f("psigamma", PsiGamma.class, 26, 11, 2);


/* Mathematical Functions of a Complex Argument */
/* these are group generic and so need to eval args */

    f("Re", ComplexGroup.class, 1, 1, 1);
    f("Im", ComplexGroup.class, 2, 1, 1);
    f("Mod", ComplexGroup.class, 3, 1, 1);
    f("Arg", ComplexGroup.class, 4, 1, 1);
    f("Conj", ComplexGroup.class, 5, 1, 1);


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

    f("dlnorm", Distributions.class, 16, 11, 3 + 1);
    f("plnorm", Distributions.class, 17, 11, 3 + 2);
    f("qlnorm", Distributions.class, 18, 11, 3 + 2);

    f("dlogis", Distributions.class, 19, 11, 3 + 1);
    f("plogis", Distributions.class, 20, 11, 3 + 2);
    f("qlogis", Distributions.class, 21, 11, 3 + 2);

    f("dnbinom", Distributions.class, 22, 11, 3 + 1);
    f("pnbinom", Distributions.class, 23, 11, 3 + 2);
    f("qnbinom", Distributions.class, 24, 11, 3 + 2);

    f("dnorm", Distributions.class, 25, 11, 3 + 1);
    f("pnorm", Distributions.class, 26, 11, 3 + 2);
    f("qnorm", Distributions.class, 27, 11, 3 + 2);

    f("dunif", Distributions.class, 28, 11, 3 + 1);
    f("punif", Distributions.class, 29, 11, 3 + 2);
    f("qunif", Distributions.class, 30, 11, 3 + 2);

    f("dweibull", Distributions.class, 31, 11, 3 + 1);
    f("pweibull", Distributions.class, 32, 11, 3 + 2);
    f("qweibull", Distributions.class, 33, 11, 3 + 2);

    f("dnchisq", Distributions.class, 34, 11, 3 + 1);
    f("pnchisq", Distributions.class, 35, 11, 3 + 2);
    f("qnchisq", Distributions.class, 36, 11, 3 + 2);

    f("dnt", Distributions.class, 37, 11, 3 + 1);
    f("pnt", Distributions.class , 38, 11, 3 + 2);
    f("qnt", Distributions.class, 39, 11, 3 + 2);

    f("dwilcox", Distributions.class, 40, 11, 3 + 1);
    f("pwilcox", Distributions.class, 41, 11, 3 + 2);
    f("qwilcox", Distributions.class, 42, 11, 3 + 2);

    f("besselI", /*math3*/ null, 43, 11, 3);
    f("besselK", /*math3*/ null, 44, 11, 3);

    f("dnbinom_mu", Distributions.class, 45, 11, 3 + 1);
    f("pnbinom_mu", Distributions.class, 46, 11, 3 + 2);
    f("qnbinom_mu", Distributions.class, 47, 11, 3 + 2);


/* Mathematical Functions of Four Numeric (+ 1-2 int) Variables */

    f("dhyper", Distributions.class, 1, 11, 4 + 1);
    f("phyper", Distributions.class, 2, 11, 4 + 2);
    f("qhyper", Distributions.class, 3, 11, 4 + 2);

    f("dnbeta", Distributions.class, 4, 11, 4 + 1);
    f("pnbeta", Distributions.class, 5, 11, 4 + 2);
    f("qnbeta", Distributions.class, 6, 11, 4 + 2);

    f("dnf", Distributions.class , 7, 11, 4 + 1);
    f("pnf", Distributions.class, 8, 11, 4 + 2);
    f("qnf", Distributions.class, 9, 11, 4 + 2);

    /* Where is this primitive? (dtukey) I could'nt find it in C source */
    f("dtukey", /*math4*/ null, 10, 11, 4 + 1);
    f("ptukey", Distributions.class, 11, 11, 4 + 2);
    f("qtukey", Distributions.class, 12, 11, 4 + 2);

/* Random Numbers */

    f("rchisq", RNG.class, 0, 11, 2);
    f("rexp", RNG.class, 1, 11, 2);
    f("rgeom", RNG.class, 2, 11, 2);
    f("rpois", RNG.class, 3, 11, 2);
    f("rt", RNG.class, 4, 11, 2);
    f("rsignrank", RNG.class, 5, 11, 2);

    f("rbeta", RNG.class, 0, 11, 3);
    f("rbinom", RNG.class, 1, 11, 3);
    f("rcauchy",RNG.class, 2, 11, 3);
    f("rf", RNG.class, 3, 11, 3);
    f("rgamma", RNG.class, 4, 11, 3);
    f("rlnorm", RNG.class, 5, 11, 3);
    f("rlogis", RNG.class, 6, 11, 3);
    f("rnbinom",RNG.class, 7, 11, 3);
    f("rnbinom_mu", RNG.class , 13, 11, 3);
    f("rnchisq", RNG.class, 12, 11, 3);
    f("rnorm", RNG.class, 8, 11, 3);
    f("runif", RNG.class, 9, 11, 3);
    f("rweibull", RNG.class, 10, 11, 3);
    f("rwilcox", RNG.class, 11, 11, 3);

    f("rhyper", RNG.class, 0, 11, 4);

    f("rmultinom", RNG.class, 0, 11, 3);
    f("sample", Sampling.class, 0, 11, 4);

    f("RNGkind", RNG.class, 0, 11, 2);
    f("set.seed", RNG.class, 0, 11, 3);

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
    f("cumsum", Summary.class, 1, 1, 1);
    f("cumprod", Summary.class, 2, 1, 1);
    f("cummax", Summary.class, 3, 1, 1);
    f("cummin", Summary.class, 4, 1, 1);

/* Type coercion */

    f("as.character", Types.class, "asCharacter", 0, 1, -1);
    f("as.integer", Types.class, "asInteger", 1, 1, -1);
    f("as.double", Types.class, "asDouble",  2, 1, -1);
    f("as.complex", Types.class, 3, 1, -1);
    f("as.logical", Types.class, "asLogical", 4, 1, -1);
    f("as.raw", Types.class, 5, 1, 1);
    f("as.vector", Types.class, 0, 11, 2);
    f("paste", Text.class, 0, 11, 3);
    f("file.path", Text.class, 0, 11, 2);
    f("format", Text.class, 0, 11, 8);
    f("format.info", /*formatinfo*/ null, 0, 11, 3);
    f("cat", Cat.class, 0, 111, 6);
    f("call", Evaluation.class, 0, 0, -1);
    f("do.call", Evaluation.class, 0, 211, 3);
    f("as.call", Types.class, 0, 1, 1);
    f("type.convert", Scan.class, 1, 11, 4);
    f("as.environment", Types.class, "asEnvironment", 0, 1, 1);
    f("storage.mode<-", Types.class, 0, 1, 2);


/* String Manipulation */

    f("nchar", Text.class, 1, 11, 3);
    f("nzchar", Text.class, 1, 1, 1);
    f("substr", Text.class, 1, 11, 3);
    f("substr<-", Text.class, 1, 11, 4);
    f("strsplit", Text.class, 1, 11, 6);
    f("abbreviate", /*abbrev*/ null, 1, 11, 3);
    f("make.names", Text.class, 0, 11, 2);
    f("grep", Text.class, 0, 11, 9);
    f("grepl", Text.class, 1, 11, 9);
    f("sub", Text.class, 0, 11, 8);
    f("gsub", Text.class, 1, 11, 8);
    f("regexpr", Text.class, 1, 11, 7);
    f("gregexpr", /*gregexpr*/ null, 1, 11, 7);
    f("agrep", Text.class, 1, 11, 9);
    f("tolower", Text.class, 0, 11, 1);
    f("toupper", Text.class, 1, 11, 1);
    f("chartr", Text.class, 1, 11, 3);
    f("sprintf", Text.class, 1, 11, -1);
    f("make.unique", Text.class, 0, 11, 2);
    f("charToRaw", Types.class, 1, 11, 1);
    f("rawToChar", Types.class, 1, 11, 2);
    f("rawShift", Types.class , 1, 11, 2);
    f("intToBits", Types.class, 1, 11, 1);
    f("rawToBits", Types.class , 1, 11, 1);
    f("packBits", /*packBits*/ null, 1, 11, 2);
    f("utf8ToInt", Text.class, 1, 11, 1);
    f("intToUtf8", Text.class, 1, 11, 2);
    f("encodeString",Text.class, 1, 11, 5);
    f("iconv", Text.class, 0, 11, 5);
    f("strtrim", Text.class, 0, 11, 2);
    f("strtoi", Text.class,  0, 11, 2);
    
/* Type Checking (typically implemented in ./coerce.c ) */

    f("is.null", Types.class,  0 /*NILSXP*/, 1, 1);
    f("is.logical", Types.class ,0 /*LGLSXP*/, 1, 1);
    f("is.integer", Types.class, 0 /*INTSXP*/, 1, 1);
    f("is.real", Types.class,  0/*REALSXP */, 1, 1);
    f("is.double", Types.class, 0 /*REALSXP*/, 1, 1);
    f("is.complex", Types.class, 0/*CPLXSXP*/, 1, 1);
    f("is.character", Types.class, 0 /*STRSXP*/, 1, 1);
    f("is.symbol", Types.class,  0 /*SYMSXP*/, 1, 1);
    f("is.environment", Types.class, "isEnvironment", 0/* ENVSXP */, 1, 1);
    f("is.list", Types.class,"isList", 0/* VECSXP */, 1, 1);
    f("is.pairlist", Types.class, "isPairList", 0 /*LISTSXP */, 1, 1);
    f("is.expression", Types.class, "isExpression",  0 /* EXPRSXP*/, 1, 1);
    f("is.raw", Types.class ,0 /* RAWSXP */, 1, 1);

    f("is.object", Types.class, 50, 1, 1);

    f("is.numeric", Types.class,  100, 1, 1);
    f("is.matrix", Types.class, 101, 1, 1);
    f("is.array", Types.class, 102, 1, 1);

    f("is.atomic", Types.class, 200, 1, 1);
    f("is.recursive", Types.class, 201, 1, 1);

    f("is.call",  Types.class, 300, 1, 1);
    f("is.language", Types.class, 301, 1, 1);
    f("is.function", Types.class, 302, 1, 1);

    f("is.single", Types.class, 999, 1, 1);

    f("is.vector", Types.class, 0, 11, 2);
    f("is.na", Types.class,  0, 1, 1);
    f("is.nan", Types.class,  0, 1, 1);
    f("is.finite", Types.class, 0, 1, 1);
    f("is.infinite", Types.class, 0, 1, 1);


/* Miscellaneous */

    f("proc.time", System.class, 0, 1, 0);
    f("gc.time", /*gctime*/ null, 0, 1, -1);
    f("Version", System.class, 0, 11, 0);
    f("machine", System.class, 0, 11, 0);
    f("commandArgs", System.class, 0, 11, 0);
    f("unzip", Files.class, 0, 111, 6);
    f("system", System.class, 0, 211, 5);
    f("parse", Evaluation.class, 0, 11, 6);
    f("parse_Rd", /*parseRd*/ null, 0, 11, 7);
    f("save", Serialization.class, 0, 111, 6);
    f("saveToConn", Serialization.class, 0, 111, 6);
    f("load", /*load*/ null, 0, 111, 2);
    f("loadFromConn2", Serialization.class, 0, 111, 2);
    f("serializeToConn", Serialization.class, 0, 111, 5);
    f("unserializeFromConn", Serialization.class, 0, 111, 2);
    f("deparse", Deparse.class, 0, 11, 5);
    f("deparseRd", /*deparseRd*/ null, 0, 11, 2);
    f("dput", /*dput*/ null, 0, 111, 3);
    f("dump", /*dump*/ null, 0, 111, 5);
    add(new SubstituteFunction());
    add(new QuoteFunction());// f("quote", Evaluation.class, 0, 0, 1);
    f("quit", Session.class, 0, 111, 3);
    f("interactive", Session.class, 0, 0, 0);
    f("readline", /*readln*/ null, 0, 11, 1);
    f("menu", Session.class, 0, 11, 1);
    f("print.default", Print.class, 0, 111, 9);
    f("print.function", Print.class, 0, 111, 3);
    f("prmatrix", /*prmatrix*/ null, 0, 111, 6);
    f("invisible", Types.class, 0, 101, 1);
    f("gc", System.class, 0, 11, 2);
    f("gcinfo", /*gcinfo*/ null, 0, 11, 1);
    f("gctorture", /*gctorture*/ null, 0, 11, 1);
    f("memory.profile", /*memoryprofile*/ null, 0, 11, 0);
    f("rep", Sequences.class, 0, 0, -1);
    f("rep.int", Sequences.class, 0, 11, 2);
    f("seq.int", Sequences.class, 0, 0, -1);
    f("seq_len", Sequences.class, 0, 1, 1);
    f("seq_along", Sequences.class, "seqAlong", 0, 1, 1);
    f("list", Types.class, "list", 1, 1, -1);
    f("split",  Split.class, 0, 11, 2);
    f("is.loaded", /*isloaded*/ null, 0, 11, -1, PP_FOREIGN, PREC_FN, 0);
    f(".C", Native.class, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Fortran", Native.class, 1, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".External", /*External*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Call", Native.class, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".External.graphics", /*Externalgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f(".Call.graphics", /*dotcallgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0);
    f("recordGraphics", /*recordGraphics*/ null, 0, 211, 3, PP_FOREIGN, PREC_FN, 0);
    f("dyn.load", Native.class,  0, 111, 4);
    f("dyn.unload", System.class, 0, 111, 1);
    f("ls", Types.class, 1, 11, 2);
    f("typeof", Types.class, 1, 11, 1);
    f("eval", Evaluation.class, 0, 211, 3);
    f("eval.with.vis",Evaluation.class, 1, 211, 3);
    f("withVisible", /*withVisible*/ null, 1, 10, 1);
    f("expression", Types.class, 1, 0, -1);
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
    f("xtfrm", Sort.class, 0, 1, 1);
    f("is.unsorted", Sort.class, 0, 11, 2);
    f("psort", Sort.class, null, 0, 11, 2);
    f("qsort", Sort.class, 0, 11, 2);
    f("radixsort", /*radixsort*/ null, 0, 11, 3);
    f("order", Sort.class, 0, 11, -1);
    f("rank", /*rank*/ null, 0, 11, 2);
    f("missing", Evaluation.class, "missing", 1, 0, 1);
    f("nargs", Evaluation.class, 1, 0, 0);
    f("scan", Scan.class, 0, 11, 18);
    f("count.fields", /*countfields*/ null, 0, 11, 6);
    f("readTableHead", Scan.class, 0, 11, 6);
    f("t.default", Matrices.class, 0, 11, 1);
    f("aperm", Matrices.class, 0, 11, 3);
    f("builtins", /*builtins*/ null, 0, 11, 1);
    f("edit", /*edit*/ null, 0, 11, 4);
    f("dataentry", /*dataentry*/ null, 0, 11, 2);
    f("dataviewer", /*dataviewer*/ null, 0, 111, 2);
    f("args", /*args*/ null, 0, 11, 1);
    f("formals", Types.class, 0, 11, 1);
    f("body", Types.class, 0, 11, 1);
    f("bodyCode", /*bodyCode*/ null, 0, 11, 1);
    f("emptyenv", Types.class, 0, 1, 0);
    f("baseenv", Types.class, 0, 1, 0);
    f("globalenv", Types.class, 0, 1, 0);
    f("environment", Types.class, 0, 11, 1);
    f("environment<-", Types.class, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1);
    f("environmentName", Types.class, 0, 11, 1);
    f("env2list", Types.class, 0, 11, 2);
    f("reg.finalizer", /*regFinaliz*/ null, 0, 11, 3);
    f("options", Types.class, 0, 211, 1);
    f("sink", Connections.class, 0, 111, 4);
    f("sink.number", /*sinknumber*/ null, 0, 11, 1);
    f("lib.fixup", Types.class, 0, 111, 2);
    f("pos.to.env", /*pos2env*/ null, 0, 1, 1);
    f("eapply", /*eapply*/ null, 0, 10, 4);
    f("lapply", Evaluation.class, 1, 10, 2);
    f("vapply", Evaluation.class, 1, 10, 4);
    f("rapply", /*rapply*/ null, 0, 11, 5);
    f("islistfactor",  Types.class, 0, 11, 2);
    f("colSums", Matrices.class, 0, 11, 4);
    f("colMeans", Matrices.class, 1, 11, 4);
    f("rowSums", Matrices.class, 2, 11, 4);
    f("rowMeans", Matrices.class, 3, 11, 4);
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
    f("new.env", Types.class, 0, 11, 3);
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
    f("file.create", Files.class, 0, 11, 2);
    f("file.remove", /*fileremove*/ null, 0, 11, 1);
    f("file.rename", /*filerename*/ null, 0, 11, 2);
    f("file.append", Files.class, 0, 11, 2);
    f("codeFiles.append", /*fileappend*/ null, 1, 11, 2);
    f("file.symlink", /*filesymlink*/ null, 0, 11, 2);
    f("file.copy", Files.class, 0, 11, 4);
    f("list.files", Files.class, 0, 11, 6);
    f("file.exists", Files.class, 0, 11, 1);
    f("file.choose", /*filechoose*/ null, 0, 11, 1);
    f("file.info", Files.class, 0, 11, 1);
    f("file.access", Files.class, 0, 11, 2);
    f("dir.create", Files.class, 0, 11, 4);
    f("tempfile", Files.class, 0, 11, 2);
    f("tempdir", Files.class, 0, 11, 0);
    f("R.home", System.class, "getRHome", 0, 11, 0);
    f("date", System.class, 0, 11, 0);
    f("index.search", /*indexsearch*/ null, 0, 11, 5);
    f("Sys.getenv", System.class, 0, 11, 2);
    f("Sys.setenv", System.class, 0, 111, 2);
    f("Sys.unsetenv", System.class, 0, 111, 1);
    f("getwd", Files.class, 0, 11, 0);
    f("setwd", Files.class, 0, 111, 1);
    f("basename", Files.class, 0, 11, 1);
    f("dirname", Files.class, 0, 11, 1);
    f("dirchmod", System.class, 0, 111, 1);
    f("Sys.chmod", System.class, 0, 111, 2);
    f("Sys.umask", System.class, 0, 111, 1);
    f("Sys.readlink", /*readlink*/ null, 0, 11, 1);
    f("Sys.info", System.class, 0, 11, 0);
    f("Sys.sleep", System.class, 0, 11, 1);
    f("Sys.getlocale", System.class, 0, 11, 1);
    f("Sys.setlocale", System.class, 0, 11, 2);
    f("Sys.localeconv", /*localeconv*/ null, 0, 11, 0);
    f("path.expand", Files.class, "pathExpand", 0, 11, 1);
    f("Sys.getpid",System.class, 0, 11, 0);
    f("normalizePath", Files.class, 0, 11, 1);
    f("Sys.glob", Files.class, "glob", 0, 11, 2);
    f("unlink", Files.class, 0, 111, 2);

/* Complex Valued Functions */
    f("fft", FFT.class, 0, 11, 2);
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
    f("rgb", RgbHsv.class, 0, 11, 6);
    f("rgb256", RgbHsv.class, 1, 11, 5);
    f("rgb2hsv", RgbHsv.class, 0, 11, 1);
    f("hsv", RgbHsv.class, 0, 11, 5);
    f("hcl", /*hcl*/ null, 0, 11, 5);
    f("gray", RgbHsv.class, 0, 11, 1);
    f("colors", /*colors*/ null, 0, 11, 0);
    f("col2rgb", RgbHsv.class, 0, 11, 1);
    f("palette", /*palette*/ null, 0, 11, 1);
    f("plot.new", Plot.class, 0, 111, 0);
    f("plot.window", Plot.class, 0, 111, 3);
    f("axis", Plot.class, 0, 111, 13);
    f("plot.xy", /*plot_xy*/ null, 0, 111, 7);
    f("text", /*text*/ null, 0, 111, -1);
    f("mtext", /*mtext*/ null, 0, 111, 5);
    f("title", Plot.class, 0, 111, 4);
    f("abline", /*abline*/ null, 0, 111, 6);
    f("box", Plot.class, 0, 111, 3);
    f("rect", Plot.class, 0, 111, 6);
    f("polygon", Plot.class, 0, 111, 5);
    f("xspline", Plot.class, 0, 111, -1);
    f("par", Par.class, 0, 11, 1);
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
    f("grconvertX", Graphics.class, 0, 11, 3);
    f("grconvertY", Graphics.class, 1, 11, 3);

/* Objects */
    f("inherits", Attributes.class, 0, 11, 3);
    f("UseMethod", S3.class, 0, 200, -1);
    f("NextMethod", S3.class, 0, 210, -1);
    f("standardGeneric", Methods.class, 0, 201, -1);

/* Modelling Functionality */

    f("nlm", Optimizations.class, 0, 11, 11);
    f("fmin", Optimizations.class, 0, 11, 4);
    f("zeroin", /*zeroin*/ null, 0, 11, 5);
    f("zeroin2", Roots.class, 0, 11, 7);
    f("optim", Optimizations.class, 0, 11, 7);
    f("optimhess", /*optimhess*/ null, 0, 11, 4);
    f("terms.formula", Models.class, 0, 11, 5);
    f("update.formula", /*updateform*/ null, 0, 11, 2);
    f("model.frame", Models.class, 0, 11, 8);
    f("model.matrix", Models.class, 0, 11, 2);

    f("D", /*D*/ null, 0, 11, 2);
    f("deriv.default", /*deriv*/ null, 0, 11, 5);

/* History manipulation */
    f("loadhistory", /*loadhistory*/ null, 0, 11, 1);
    f("savehistory", /*savehistory*/ null, 0, 11, 1);
    f("addhistory", /*addhistory*/ null, 0, 11, 1);

/* date-time manipulations */
    f("Sys.time", Time.class, 0, 11, 0);
    f("as.POSIXct", Time.class, 0, 11, 2);
    f("as.POSIXlt", Time.class, 0, 11, 2);
    f("format.POSIXlt", Time.class, 0, 11, 3);
    f("strptime", Time.class,  0, 11, 3);
    f("Date2POSIXlt", /*D2POSIXlt*/ null, 0, 11, 1);
    f("POSIXlt2Date", /*POSIXlt2D*/ null, 0, 11, 1);


/* Connections */
    f("stdin", Connections.class, 0, 11, 0);
    f("stdout", Connections.class, 0, 11, 0);
    f("stderr", Connections.class, 0, 11, 0);
    f("readLines",Connections.class, 0, 11, 5);
    f("writeLines", Connections.class, 0, 11, 4);
    f("readBin", /*readbin*/ null, 0, 11, 6);
    f("writeBin", /*writebin*/ null, 0, 211, 5);
    f("readChar", Connections.class, 0, 11, 3);
    f("writeChar", /*writechar*/ null, 0, 211, 5);
    f("open", Connections.class, 0, 11, 3);
    f("isOpen", Connections.class, 0, 11, 2);
    f("isIncomplete", /*isincomplete*/ null, 0, 11, 1);
    f("isSeekable", /*isseekable*/ null, 0, 11, 1);
    f("close", Connections.class, 0, 11, 2);
    f("flush", /*flush*/ null, 0, 11, 1);
    f("file", Connections.class, 1, 11, 4);
    f("url", Connections.class, 0, 11, 4);
    f("pipe", /*pipe*/ null, 0, 11, 3);
    f("fifo", /*fifo*/ null, 0, 11, 4);
    f("gzfile", Connections.class, 0, 11, 4);
    f("bzfile", /*gzfile*/ null, 1, 11, 4);
    f("xzfile", /*gzfile*/ null, 2, 11, 4);
    f("unz", /*unz*/ null, 0, 11, 3);
    f("seek", /*seek*/ null, 0, 11, 4);
    f("truncate", /*truncate*/ null, 0, 11, 1);
    f("pushBack", Connections.class, 0, 11, 3);
    f("clearPushBack", Connections.class, 0, 11, 1);
    f("pushBackLength", Connections.class, 0, 11, 1);
    f("rawConnection", /*rawconnection*/ null, 0, 11, 3);
    f("rawConnectionValue", /*rawconvalue*/ null, 0, 11, 1);
    f("textConnection", /*textconnection*/ null, 0, 11, 5);
    f("textConnectionValue", /*textconvalue*/ null, 0, 11, 1);
    f("socketConnection", Connections.class, 0, 11, 6);
    f("sockSelect", /*sockselect*/ null, 0, 11, 3);
    f("getConnection", /*getconnection*/ null, 0, 11, 1);
    f("getAllConnections", /*getallconnections*/ null, 0, 11, 0);
    f("summary.connection", Connections.class, 0, 11, 1);
    f("download", /*download*/ null, 0, 11, 5);
    f("nsl", /*nsl*/ null, 0, 11, 1);
    f("gzcon", /*gzcon*/ null, 0, 11, 3);
    f("memCompress", /*memCompress*/ null, 0, 11, 2);
    f("memDecompress", /*memDecompress*/ null, 0, 11, 2);

    f("readDCF", DebianControlFiles.class, 0, 11, 2);

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
    f("isNamespace", Namespaces.class, 0, 0, 1);
  // hiding:  f("registerNamespace", Namespaces.class, 0, 11, 2);
   // hiding: f("unregisterNamespace", Namespaces.class, 0, 11, 1);
    f("getNamespace", Namespaces.class, 0, 0, 1);
    f("getRegisteredNamespace",Namespaces.class,  0, 11,  1);
    f("loadedNamespaces", Namespaces.class, 0,0,0);
    
    //hiding: f("getNamespaceRegistry", Namespaces.class, 0, 11, 0);
   // hiding f("importIntoEnv", Namespaces.class, 0, 11, 4);
    f("env.profile", /*envprofile*/ null, 0, 211, 1);
    f(":::", Namespaces.class, 0, 0, -1);
    f("::", Namespaces.class, 0, 0, -1);
    f("getDataset", Namespaces.class, 0, 11, 1);
    
    f("write.table", /*writetable*/ null, 0, 111, 11);
    f("Encoding", Types.class, 0, 11, 1);
    f("setEncoding", Types.class, 0, 11, 2);
    f("lazyLoadDBfetch", Serialization.class, 0, 1, 4);
    f("setTimeLimit", /*setTimeLimit*/ null, 0, 111, 3);
    f("setSessionTimeLimit", /*setSessionTimeLimit*/ null, 0, 111, 2);
    f("icuSetCollate", /*ICUset*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0) ;
    
    // jvm specific
    f("import", Jvmi.class, 0, 0, -1);
    f("jload", Jvmi.class, 0, 0, -1);
    f("library", Packages.class, 0,0,-1);
  }

  private void add(SpecialFunction fn) {
    builtins.put(Symbol.get(fn.getName()), fn);
  }

  private void add(Entry entry) {     
    if (entry.isInternal()) {
      internalEntries.put(Symbol.get(entry.name), entry);
    } else {
      builtinEntries.put(Symbol.get(entry.name), entry);
    }
  }

  private void addInternal(String name, PrimitiveFunction fn) {
    internals.put(Symbol.get(fn.getName()), fn);
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

    public boolean isInternal() {
      return ((eval % 100) / 10) != 0;
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
}
