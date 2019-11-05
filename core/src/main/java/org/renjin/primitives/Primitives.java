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
package org.renjin.primitives;

import org.renjin.base.Base;
import org.renjin.base.Lapack;
import org.renjin.base.internals.AllNamesVisitor;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.methods.Methods;
import org.renjin.primitives.combine.ColumnBindFunction;
import org.renjin.primitives.combine.Combine;
import org.renjin.primitives.combine.RowBindFunction;
import org.renjin.primitives.files.Files;
import org.renjin.primitives.io.Cat;
import org.renjin.primitives.io.DebianControlFiles;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.match.Duplicates;
import org.renjin.primitives.match.Match;
import org.renjin.primitives.matrix.Matrices;
import org.renjin.primitives.packaging.Namespaces;
import org.renjin.primitives.packaging.Packages;
import org.renjin.primitives.sequence.RepFunction;
import org.renjin.primitives.sequence.Sequences;
import org.renjin.primitives.special.*;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.primitives.text.StrSignIf;
import org.renjin.primitives.text.Text;
import org.renjin.primitives.time.Time;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.s4.S4;
import org.renjin.serialization.Serialization;
import org.renjin.sexp.*;
import org.renjin.stats.internals.Distributions;
import org.renjin.stats.internals.distributions.RNG;
import org.renjin.stats.internals.distributions.Sampling;
import org.renjin.stats.internals.optimize.Optimizations;
import org.renjin.stats.internals.optimize.Roots;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Primitives {

  private IdentityHashMap<Symbol, PrimitiveFunction> reserved = new IdentityHashMap<>();

  private ConcurrentHashMap<Symbol, PrimitiveFunction> builtins = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Symbol, PrimitiveFunction> internals = new ConcurrentHashMap<>();

  // these are loaded on demand
  private IdentityHashMap<Symbol, Entry> builtinEntries = new IdentityHashMap<Symbol, Entry>();
  private IdentityHashMap<Symbol, Entry> internalEntries = new IdentityHashMap<Symbol, Entry>();
  
  private static final Primitives INSTANCE = new Primitives();
  
  public static PrimitiveFunction getBuiltin(String name) {
    return getBuiltin(Symbol.get(name));
  }

  public static PrimitiveFunction getReservedBuiltin(Symbol symbol) {
    assert symbol.isReservedWord();
    PrimitiveFunction fn = INSTANCE.reserved.get(symbol);
    assert fn != null : "missing reserved: " + symbol;
    return fn;
  }

  public static PrimitiveFunction getBuiltin(Symbol symbol) {
    return getPrimitive(INSTANCE.builtinEntries, INSTANCE.builtins, symbol);
  }

  public static PrimitiveFunction getInternal(Symbol symbol) {
    return getPrimitive(INSTANCE.internalEntries, INSTANCE.internals, symbol);
  }


  public static PrimitiveFunction getPrimitive(Symbol symbol) {
    if(INSTANCE.internalEntries.containsKey(symbol)) {
      return getInternal(symbol);
    } else {
      return getBuiltin(symbol);
    }
  }

  public static boolean isBuiltin(String opName) {
    Symbol symbol = Symbol.get(opName);
    return INSTANCE.builtinEntries.containsKey(symbol);
  }

  private static PrimitiveFunction getPrimitive(IdentityHashMap<Symbol, Entry> entryMap,
                                                ConcurrentHashMap<Symbol, PrimitiveFunction> cache,
                                                Symbol symbol) {

    PrimitiveFunction existing = cache.get(symbol);
    if(existing != null) {
      return existing;
    }

    Entry entry = entryMap.get(symbol);
    if(entry == null) {
      // No such primitive
      return null;
    }

    PrimitiveFunction newFunction = createFunction(entry);
    existing = cache.putIfAbsent(symbol, newFunction);
    if(existing != null) {
      return existing;
    }

    return newFunction;
  }

  public static List<Entry> getEntries() {
    List<Entry> set = Lists.newArrayList();
    set.addAll(INSTANCE.internalEntries.values());
    set.addAll(INSTANCE.builtinEntries.values());
    return set;
  }

  public static Entry getBuiltinEntry(Symbol name) {
    return INSTANCE.builtinEntries.get(name);
  }
  
  public static Entry getInternalEntry(Symbol name) {
    return INSTANCE.internalEntries.get(name);
  }

  public static Entry getBuiltinEntry(String name) {
    return getBuiltinEntry(Symbol.get(name));
  }

  
  public static Set<Symbol> getBuiltinSymbols() {
    return Sets.union(INSTANCE.builtins.keySet(), INSTANCE.builtinEntries.keySet());
  }

  private static PrimitiveFunction createFunction(final Entry entry) {
    try {
      return (PrimitiveFunction) Class.forName(WrapperGenerator2.toFullJavaName(entry.name)).newInstance();
    } catch(final Exception e) {
      return new BuiltinFunction(entry.name) {

        @Override
        public SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] evaluatedArguments, DispatchTable dispatchTable) {
          throw new EvalException("Sorry! " + entry.name + " not yet implemented!", e);
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
    add(new UseMethod());
    add(new MissingFunction());

    f("stop", Conditions.class, 11);
    f("warning", Warning.class, 111);

    f("gettext", Text.class, 11);
    f("ngettext", Text.class, 11);
    f("bindtextdomain", Text.class, 11);
    f("enc2utf8", Text.class, 1);
    f(".addCondHands", Conditions.class, 111);
    f(".resetCondHands", /*resetCondHands*/ null, 111);
    f(".signalCondition", Conditions.class, 11);
    f(".dfltStop", Conditions.class, 11);
    f(".dfltWarn", Warning.class, 11);
    f(".addRestart", Conditions.class, 11);
    f(".getRestart", Conditions.class, 11);
    f(".invokeRestart", Conditions.class, 11);
    f(".addTryHandlers", /*addTryHandlers*/ null, 111);

    f("geterrmessage", Conditions.class, 11);
    f("seterrmessage", Conditions.class, 111);
    f("printDeferredWarnings", Warning.class, 111);
    f("interruptsSuspended", /*interruptsSuspended*/ null, 11);

    addInternal(new RestartFunction());
    add(new ClosureFunction());

    f("as.function.default", Types.class, 11);

    add(new AssignLeftFunction());
    add(new AssignFunction());

    add(new ReassignLeftFunction());
    add(new BeginFunction());
    add(new ParenFunction());

    add(new AssignSlotFunction());

    f(".subset", Subsetting.class, 1);
    f(".subset2", Subsetting.class, 1);
    f("[",Subsetting.class, -1);
    f("[[", Subsetting.class, -1);
    add(new DollarFunction());
    add(new DollarAssignFunction());
    add(new AtFunction());
    f("[<-", Subsetting.class, 3);
    f("[[<-", Subsetting.class, 3);

    add(new SwitchFunction());

    f("browser", /*browser*/ null, 101);
    f("debug", /*debug*/ null, 111);
    f("undebug", /*debug*/ null, 111);
    f("isdebugged", /*debug*/ null, 11);
    f("debugonce", /*debug*/ null, 111);
    f(".primTrace", /*trace*/ null, 101);
    f(".primUntrace", /*trace*/ null, 101);

    f("traceOnOff", /*traceOnOff*/ null, 11);
    f("debugOnOff", /*traceOnOff*/ null, 11);

    add(new InternalFunction());
    add(new OnExitFunction());

    add(new RecallFunction());
    f("delayedAssign", Evaluation.class, 111);
    f("makeLazy", Serialization.class, 111);
    f(".Primitive", Evaluation.class, 1);
    f("identical",  Identical.class, 11);


/* Binary Operators */
/* these are group generic and so need to eval args */
    f("+",  Ops.class,  /* PLUSOP, */  2);
    f("-", Ops.class,   /* MINUSOP, */  2);
    f("*", Ops.class,  /*TIMESOP ,*/  2);
    f("/", Ops.class,  /*DIVOP,*/  2);
    f("^", Ops.class,   /*POWOP,*/  2);


    f("%%", Ops.class,  /* MODOP */ 2);
    f("%/%", Ops.class,  /* IDIVOP */ 2);
    f("%*%", Matrices.class, 2);
    f("crossprod", Matrices.class, 11);
    f("tcrossprod", Matrices.class, 11);


/* these are group generic and so need to eval args */
    f("==", Ops.class, 2);
    f("!=", Ops.class, 2);
    f("<", Ops.class, 2);
    f("<=", Ops.class, 2);
    f(">=", Ops.class, 2);
    f(">", Ops.class, 2);
    f("&", Ops.class, 2);
    f("|", Ops.class, 2);
    f("!", Ops.class, 1);

    add(new AndFunction());
    add(new OrFunction());
    f(":", Sequences.class, "colon", 1);

    add(new TildeFunction());

/* Logic Related Functions */
/* these are group generic and so need to eval args */
    f("all", Summary.class, 1);
    f("any", Summary.class, 1);


/* Vectors, Matrices and Arrays */

/* printname  c-entry   offset  eval  arity pp-kind      precedence rightassoc
 * ---------  -------   ------  ----  ----- -------      ---------- ----------*/
    f("vector", Vectors.class, 11);
    f("complex", Vectors.class, 11);
    f("matrix", Matrices.class, 11);
    f("length", Vectors.class, 1);
    f("length<-", Vectors.class, 2);
    f("row", Matrices.class, 11);
    f("col", Matrices.class, 11);
    f("c", Combine.class, 1);
    f("...elt", Evaluation.class, 1);
    f("unlist", Combine.class, 11);
    addInternal(new ColumnBindFunction());
    addInternal(new RowBindFunction());
    f("drop", Vectors.class, 11);
    f("class", Attributes.class, "getClass", 1);
    f(".cache_class", Methods.class, 2);
    f("unclass", Attributes.class, 1);
    f("names", Attributes.class,  "getNames", 1);
    f("names<-", Attributes.class, "setNames", 1);
    f("dimnames", Attributes.class, 1);
    f("dimnames<-", Attributes.class, 2);
    f("all.names", AllNamesVisitor.class, 11);
    f("dim", Attributes.class, 1);
    f("dim<-", Attributes.class, 2);
    f("attributes", Attributes.class, 1);
    f("attributes<-", Attributes.class, null, 1);
    f("attr", Attributes.class, 1);
    f("attr<-", Attributes.class, 3);
    f("copyDFattr", Attributes.class, 11); /* used in dataframe.R */
    f("comment", Attributes.class, 11);
    f("comment<-", Attributes.class, 11);
    f("levels<-", Attributes.class, 2);
    f("get", Environments.class, 11);
    f("get0", Environments.class, 11);
    f("mget", Environments.class, 11);
    f("exists", Environments.class, 11);
    f("assign", Evaluation.class, 111);
    f("list2env", Environments.class, 11);
    f("remove", Evaluation.class, 111);
    f("duplicated", Duplicates.class, 11);
    f("unique", Duplicates.class, 11);
    f("anyDuplicated", Duplicates.class, 11);
    f("which.min", Sort.class, 11);
    f("which", Match.class, 11);
    f("pmin", Summary.class, 11);
    f("pmax", Summary.class, 11);
    f("which.max", Sort.class, 11);
    f("match", Match.class, 11);
    f("pmatch", Match.class, 11);
    f("charmatch", Match.class, 11);
    f("match.call", Match.class, 11);

    f("attach", Environments.class, 111);
    f("detach", Environments.class, 111);
    f("search", Environments.class, 11);


/* Mathematical Functions */
/* these are group generic and so need to eval args */
/* Note that the number of arguments for the primitives in the Math group
   only applies to the default method. */
    f("round", MathGroup.class, 0);
    f("signif", MathGroup.class, 0);
    f("atan",MathGroup.class, 1);
    f("log", MathGroup.class, 0);
    f("log10", MathGroup.class, 1);
    f("log2", MathGroup.class, 1);
    f("abs", MathGroup.class, 1);
    f("floor", MathGroup.class, 1);
    f("ceiling", MathGroup.class, 1);
    f("sqrt", MathGroup.class, 1);
    f("sign", MathGroup.class, 1);
    f("trunc", MathGroup.class, 1);

    f("exp", MathGroup.class, 1);
    f("expm1", MathGroup.class, 1);
    f("log1p", MathGroup.class, 1);

    f("cos", MathGroup.class, 1);
    f("sin", MathGroup.class, 1);
    f("tan", MathGroup.class, 1);
    f("acos", MathGroup.class, 1);
    f("asin", MathGroup.class, 1);

    f("cosh", MathGroup.class, 1);
    f("sinh", MathGroup.class, 1);
    f("tanh", MathGroup.class, 1);
    f("acosh", MathGroup.class, 1);
    f("asinh", MathGroup.class, 1);
    f("atanh", MathGroup.class, 1);

    f("lgamma", MathGroup.class, 1);
    f("gamma", MathGroup.class, 1);

    f("digamma", MathGroup.class, 1);
    f("trigamma", MathGroup.class, 1);
/* see "psigamma" below !*/


    f("cospi", MathGroup.class, 1);
    f("sinpi", MathGroup.class, 1);
    f("tanpi", MathGroup.class, 1);

/* Mathematical Functions of Two Numeric (+ 1-2 int) Variables */

    f("atan2", MathGroup.class, 11);

    f("lbeta", Special.class, 11);
    f("beta", Special.class, 11);
    f("lchoose", Special.class, 11);
    f("choose", Special.class, 11);

    f("dchisq", Distributions.class, 11);
    f("pchisq", Distributions.class, 11);
    f("qchisq", Distributions.class, 11);

    f("dexp", Distributions.class, 11);
    f("pexp", Distributions.class, 11);
    f("qexp", Distributions.class, 11);

    f("dgeom", Distributions.class, 11);
    f("pgeom", Distributions.class, 11);
    f("qgeom", Distributions.class, 11);

    f("dpois", Distributions.class, 11);
    f("ppois", Distributions.class, 11);
    f("qpois", null , 11);

    f("dt", Distributions.class, 11);
    f("pt", Distributions.class, 11);
    f("qt", Distributions.class, 11);

    f("dsignrank", Distributions.class, 11);
    f("psignrank", Distributions.class, 11);
    f("qsignrank", Distributions.class, 11);

    f("besselJ", Special.class, 11);
    f("besselY", Special.class, 11);

    f("psigamma", Special.class, 11);


/* Mathematical Functions of a Complex Argument */
/* these are group generic and so need to eval args */

    f("Re", ComplexGroup.class, 1);
    f("Im", ComplexGroup.class, 1);
    f("Mod", ComplexGroup.class, 1);
    f("Arg", ComplexGroup.class, 1);
    f("Conj", ComplexGroup.class, 1);


/* Mathematical Functions of Three Numeric (+ 1-2 int) Variables */

    f("dbeta", Distributions.class, 11);
    f("pbeta", Distributions.class, 11);
    f("qbeta", Distributions.class, 11);

    f("dbinom", Distributions.class, 11);
    f("pbinom", Distributions.class, 11);
    f("qbinom", Distributions.class, 11);

    f("dcauchy", Distributions.class, 11);
    f("pcauchy", Distributions.class, 11);
    f("qcauchy", Distributions.class, 11);

    f("df", Distributions.class, 11);
    f("pf", Distributions.class, 11);
    f("qf", Distributions.class, 11);

    f("dgamma", Distributions.class, 11);
    f("pgamma", Distributions.class, 11);
    f("qgamma", Distributions.class, 11);

    f("dlnorm", Distributions.class, 11);
    f("plnorm", Distributions.class, 11);
    f("qlnorm", Distributions.class, 11);

    f("dlogis", Distributions.class, 11);
    f("plogis", Distributions.class, 11);
    f("qlogis", Distributions.class, 11);

    f("dnbinom", Distributions.class, 11);
    f("pnbinom", Distributions.class, 11);
    f("qnbinom", Distributions.class, 11);

    f("dnorm", Distributions.class, 11);
    f("pnorm", Distributions.class, 11);
    f("qnorm", Distributions.class, 11);

    f("dunif", Distributions.class, 11);
    f("punif", Distributions.class, 11);
    f("qunif", Distributions.class, 11);

    f("dweibull", Distributions.class, 11);
    f("pweibull", Distributions.class, 11);
    f("qweibull", Distributions.class, 11);

    f("dnchisq", Distributions.class, 11);
    f("pnchisq", Distributions.class, 11);
    f("qnchisq", Distributions.class, 11);

    f("dnt", Distributions.class, 11);
    f("pnt", Distributions.class , 11);
    f("qnt", Distributions.class, 11);

    f("dwilcox", Distributions.class, 11);
    f("pwilcox", Distributions.class, 11);
    f("qwilcox", Distributions.class, 11);

    f("besselI", Special.class, 11);
    f("besselK", Special.class, 11);

    f("dnbinom_mu", Distributions.class, 11);
    f("pnbinom_mu", Distributions.class, 11);
    f("qnbinom_mu", Distributions.class, 11);


/* Mathematical Functions of Four Numeric (+ 1-2 int) Variables */

    f("dhyper", Distributions.class, 11);
    f("phyper", Distributions.class, 11);
    f("qhyper", Distributions.class, 11);

    f("dnbeta", Distributions.class, 11);
    f("pnbeta", Distributions.class, 11);
    f("qnbeta", Distributions.class, 11);

    f("dnf", Distributions.class , 11);
    f("pnf", Distributions.class, 11);
    f("qnf", Distributions.class, 11);

    f("ptukey", Distributions.class, 11);
    f("qtukey", Distributions.class, 11);

/* Random Numbers */

    f("rchisq", Distributions.class, 11);
    f("rexp", Distributions.class, 11);
    f("rgeom", Distributions.class, 11);
    f("rpois", Distributions.class, 11);
    f("rt", Distributions.class, 11);
    f("rsignrank", Distributions.class, 11);

    f("rbeta", Distributions.class, 11);
    f("rbinom", Distributions.class, 11);
    f("rcauchy",Distributions.class, 11);
    f("rf", Distributions.class, 11);
    f("rgamma", Distributions.class, 11);
    f("rlnorm", Distributions.class, 11);
    f("rlogis", Distributions.class, 11);
    f("rnbinom",Distributions.class, 11);
    f("rnbinom_mu", Distributions.class , 11);
    f("rnchisq", Distributions.class, 11);
    f("rnorm", Distributions.class, 11);
    f("runif", Distributions.class, 11);
    f("rweibull", Distributions.class, 11);
    f("rwilcox", Distributions.class, 11);

    f("rhyper", Distributions.class, 11);

    f("rmultinom", Distributions.class, 11);
    f("sample", Sampling.class, 11);

    f("RNGkind", RNG.class, 11);
    f("set.seed", RNG.class, 11);

/* Data Summaries */
/* sum, min, max, prod, range are group generic and so need to eval args */
    f("sum", Summary.class, 1);
    f("mean", Summary.class, 11);
    f("min", Summary.class, 1);
    f("max", Summary.class, 1);
    f("prod", Summary.class, 1);
    f("range", Summary.class, 1);

/* Note that the number of arguments in this group only applies
   to the default method */
    f("cumsum", MathGroup.class, 1);
    f("cumprod", MathGroup.class, 1);
    f("cummax", MathGroup.class, 1);
    f("cummin", MathGroup.class, 1);

/* Type coercion */

    f("as.integer", Vectors.class, "asInteger", 1);
    f("as.double", Vectors.class, "asDouble", 1);
    f("as.complex", Vectors.class, 1);
    f("as.logical", Vectors.class, 1);
    f("as.raw", Vectors.class, 1);
    f("as.vector", Vectors.class, 11);
    f("paste", Text.class, 11);
    f("format", Text.class, 11);
    f("formatC", StrSignIf.class, 11);
    f("format.info", Text.class, 11);
    f("cat", Cat.class, 111);
    add(new CallFunction());
    f("do.call", Evaluation.class, 211);
    f("as.call", Types.class, 1);
    f("type.convert", Scan.class, 11);
    f("as.environment", Environments.class, "asEnvironment", 1);


/* String Manipulation */

    f("nchar", Text.class, 11);
    f("substr", Text.class, 11);
    f("substr<-", Text.class, 11);
    f("strsplit", Text.class, 11);
    f("abbreviate", Text.class, 11);
    f("make.names", Text.class, 11);
    f("pcre_config", null, 11);
    f("grep", Text.class, 11);
    f("grepl", Text.class, 11);
    f("sub", Text.class, 11);
    f("gsub", Text.class, 11);
    f("regexpr", Text.class, 11);
    f("regexec", Text.class, 11);
    f("gregexpr", Text.class, 11);
    f("agrep", Text.class, 11);
    f("tolower", Text.class, 11);
    f("toupper", Text.class, 11);
    f("chartr", Text.class, 11);
    f("sprintf", Text.class, 11);
    f("make.unique", Text.class, 11);
    f("charToRaw", Vectors.class, 11);
    f("rawToChar", Vectors.class, 11);
    f("rawShift", Vectors.class , 11);
    f("intToBits", Vectors.class, 11);
    f("rawToBits", Vectors.class , 11);
    f("packBits", /*packBits*/ null, 11);
    f("utf8ToInt", Text.class, 11);
    f("intToUtf8", Text.class, 11);
    f("encodeString",Text.class, 11);
    f("iconv", Text.class, 11);
    f("strtrim", Text.class, 11);
    f("strtoi", Text.class, 11);

/* Type Checking (typically implemented in ./coerce.c ) */

    f("is.null", Types.class,   /*NILSXP*/ 1);
    f("is.logical", Types.class , /*LGLSXP*/ 1);
    f("is.integer", Types.class,  /*INTSXP*/ 1);
    f("is.real", Types.class,  /*REALSXP */ 1);
    f("is.double", Types.class,  /*REALSXP*/ 1);
    f("is.complex", Types.class, /*CPLXSXP*/ 1);
    f("is.character", Types.class,  /*STRSXP*/ 1);
    f("is.symbol", Types.class,   /*SYMSXP*/ 1);
    f("is.environment", Types.class, "isEnvironment", /* ENVSXP */ 1);
    f("is.list", Types.class,"isList", /* VECSXP */ 1);
    f("is.pairlist", Types.class, "isPairList",  /*LISTSXP */ 1);
    f("is.expression", Types.class, "isExpression",   /* EXPRSXP*/ 1);
    f("is.raw", Types.class , /* RAWSXP */ 1);

    f("is.object", Types.class, 1);

    f("is.numeric", Types.class, 1);
    f("is.matrix", Types.class, 1);
    f("is.array", Types.class, 1);

    f("is.atomic", Types.class, 1);
    f("is.recursive", Types.class, 1);

    f("is.call",  Types.class, 1);
    f("is.language", Types.class, 1);
    f("is.function", Types.class, 1);

    f("is.single", Types.class, 1);

    f("is.vector", Types.class, 11);
    f("is.na", Types.class, 1);
    f("is.nan", Types.class, 1);
    f("is.finite", Types.class, 1);
    f("is.infinite", Types.class, 1);

    f("isS4", Types.class, 1);
    f("setS4Object", Types.class, 11);
    f(".isMethodsDispatchOn", Methods.class, 1);

/* Miscellaneous */

    f("proc.time", System.class, 1);
    f("gc.time", /*gctime*/ null, 1);
    f("Version", System.class, 11);
    f("machine", System.class, 11);
    f("commandArgs", System.class, 11);
    f("unzip", Files.class, 111);
    f("system", System.class, 211);
    f("parse", Evaluation.class, 11);
    f("parse_Rd", /*parseRd*/ null, 11);
    f("save", Serialization.class, 111);
    f("saveToConn", Serialization.class, 111);
    f("load", /*load*/ null, 111);
    f("loadFromConn2", Serialization.class, 111);
    f("serializeToConn", Serialization.class, 111);
    f("unserializeFromConn", Serialization.class, 111);
    f("deparse", Deparse.class, 11);
    f("deparseRd", /*deparseRd*/ null, 11);
    f("dump", /*dump*/ null, 111);
    add(new SubstituteFunction());
    add(new QuoteFunction());
    f("quit", Sessions.class, 111);
    f("interactive", Sessions.class, 0);
    f("readline", Sessions.class, 11);
    f("print.default", Print.class, 111);
    f("print.function", Print.class, 111);
    f("prmatrix", /*prmatrix*/ null, 111);
    f("invisible", Types.class, 101);

    f("memory.profile", /*memoryprofile*/ null, 11);
    add(new RepFunction());
    f("rep.int", Sequences.class, 11);
    f("seq_len", Sequences.class, 1);
    f("list", Vectors.class, "list", 1);
    f("is.loaded", Native.class, 11);
    f(".C", Native.class, -1);
    f(".Fortran", Native.class, -1);
    f(".External",  Native.class, -1);
    f(".External2",  Native.class, -1);
    f(".Call", Native.class, -1);
    f("getSymbolInfo", Native.class, 11);
    f("getLoadedDLLs", Native.class, 11);
    f("getRegisteredSymbols", Native.class, 11);
    f("getRegisteredRoutines", Native.class, 11);

    f("dyn.load", null, 111);
    f("dyn.unload", null, 111);
    f("ls", Environments.class, 11);
    f("typeof", Types.class, 11);
    f("eval", Evaluation.class, 211);
    f("eval.with.vis",Evaluation.class, 211);
    f("withVisible", Evaluation.class, 10);
    add(new ExpressionFunction());
    f("sys.parent", Contexts.class, 11);
    f("sys.call", Contexts.class, 11);
    f("sys.frame", Contexts.class, 11);
    f("sys.nframe", Contexts.class, 11);
    f("sys.calls", Contexts.class, 11);
    f("sys.frames", Contexts.class, 11);
    f("sys.on.exit", Contexts.class, 11);
    f("sys.parents", Contexts.class, 11);
    f("sys.function", Contexts.class, 11);
    f("browserText", /*sysbrowser*/ null, 11);
    f("browserCondition", /*sysbrowser*/ null, 11);
    f("browserSetDebug", /*sysbrowser*/ null, 111);
    f("parent.frame", Contexts.class, "parentFrame", 11);
    f("sort", Sort.class, 11);
    f("xtfrm", Sort.class, 1);
    f("is.unsorted", Sort.class, 11);
    f("psort", Sort.class, null, 11);
    f("qsort", Sort.class, 11);
    f("radixsort", /*radixsort*/ null, 11);
    f("order", Sort.class, 11);
    f("rank", Sort.class, 11);
    f("findInterval", Sort.class, 11111);
    f("nargs", Evaluation.class, 0);
    f("scan", Scan.class, 11);
    f("t.default", Matrices.class, 11);
    f("aperm", Matrices.class, 11);
    f("builtins", /*builtins*/ null, 11);
    f("edit", /*edit*/ null, 11);
    f("dataentry", /*dataentry*/ null, 11);
    f("dataviewer", /*dataviewer*/ null, 111);
    f("args", Args.class, 11);
    f("formals", Types.class, 11);
    f("body", Types.class, 11);
    f("bodyCode", /*bodyCode*/ null, 11);
    f("emptyenv", Environments.class, 1);
    f("baseenv", Environments.class, 1);
    f("globalenv", Environments.class, 1);
    f("environment", Environments.class, 11);
    f("environment<-", Environments.class, 2);
    f("environmentName", Environments.class, 11);
    f("reg.finalizer", Environments.class, 11);
    f("options", Types.class, 211);
    f("sink", Connections.class, 111);
    f("sink.number", Connections.class, 11);
    f("lib.fixup", Types.class, 111);
    f("lapply", Evaluation.class, 10);
    f("vapply", Evaluation.class, 10);
    f("mapply", Evaluation.class, 10);
    f("rapply", Evaluation.class, 11);

    f("islistfactor",  Types.class, 11);
    f("colSums", Matrices.class, 11);
    f("colMeans", Matrices.class, 11);
    f("rowSums", Matrices.class, 11);
    f("rowMeans", Matrices.class, 11);
    f("Rprof", /*Rprof*/ null, 11);
    f("Rprofmem", /*Rprofmem*/ null, 11);
    f("tracemem", /*memtrace*/ null, 1);
    f("retracemem", /*memretrace*/ null, 1);
    f("untracemem", /*memuntrace*/ null, 101);
    f("object.size", /*objectsize*/ null, 11);
    f("inspect", /*inspect*/ null, 111);
    f("mem.limits", /*memlimits*/ null, 11);
 // Internal merge function is replaced with pure R code //  f("merge", /*merge*/ null, 11);
    f("capabilities", System.class, 11);
    f("capabilitiesX11", /*capabilitiesX11*/ null, 11);
    f("new.env", Environments.class, 11);
    f("parent.env", Environments.class, 11);
    f("parent.env<-", Environments.class, 11);
    f("visibleflag", /*visibleflag*/ null, 1);
    f("Cstack_info", /*Cstack_info*/ null, 11);
    f("startHTTPD", /*startHTTPD*/ null, 11);
    f("stopHTTPD", /*stopHTTPD*/ null, 11);

/* Functions To Interact with the Operating System */

    f("file.show", /*fileshow*/ null, 111);
    f("file.edit", /*fileedit*/ null, 111);
    f("file.create", Files.class, 11);
    f("file.remove", Files.class, 11);
    f("file.rename", Files.class, 11);
    f("file.append", Files.class, 11);
    f("codeFiles.append", /*fileappend*/ null, 11);
    f("file.symlink", /*filesymlink*/ null, 11);
    f("file.copy", Files.class, 11);
    f("list.files", Files.class, 11);
    f("file.exists", Files.class, 11);
    f("file.choose", /*filechoose*/ null, 11);
    f("file.info", Files.class, 11);
    f("file.access", Files.class, 11);
    f("dir.create", Files.class, 11);
    f("dir.exists", Files.class, 11);
    f("tempfile", Files.class, 11);
    f("tempdir", Files.class, 11);
    f("R.home", System.class, "getRHome", 11);
    f("date", System.class, 11);
    f("index.search", /*indexsearch*/ null, 11);
    f("Sys.getenv", System.class, 11);
    f("Sys.setenv", System.class, 111);
    f("Sys.unsetenv", System.class, 111);
    f("getwd", Files.class, 11);
    f("setwd", Files.class, 111);
    f("basename", Files.class, 11);
    f("dirname", Files.class, 11);
    f("dirchmod", System.class, 111);
    f("Sys.chmod", System.class, 111);
    f("Sys.umask", System.class, 111);
    f("Sys.readlink", /*readlink*/ null, 11);
    f("Sys.info", System.class, 11);
    f("Sys.sleep", System.class, 11);
    f("Sys.getlocale", System.class, 11);
    f("Sys.setlocale", System.class, 11);
    f("Sys.localeconv", System.class, 11);
    f("path.expand", Files.class, "pathExpand", 11);
    f("Sys.getpid",System.class, 11);
    f("normalizePath", Files.class, 11);
    f("Sys.glob", Files.class, "glob", 11);
    f("Sys.which", Files.class, 11);
    f("unlink", Files.class, 111);
    f("local.file", Files.class, 111);

/* Complex Valued Functions */
    f("polyroot", Polyroot.class, 11);

/* Objects */
    f("inherits", Attributes.class, 11);
    f("NextMethod", S3.class, 210);
    f("invalidateS4Cache", S4.class, 1);
    f("invalidateS4MethodCache", S4.class, 1);
    f("standardGeneric", Methods.class, 201);
    f("getClassDef", Methods.class, 11);
    f("getClass", Methods.class, 11);
    f("selectMethod", Methods.class, 11);

/* Modelling Functionality */

    f("nlm", Optimizations.class, 11);
    f("fmin", Optimizations.class, 11);
    f("zeroin", /*zeroin*/ null, 11);
    f("zeroin2", Roots.class, 11);
    
    f("D", /*D*/ null, 11);
    f("deriv.default", /*deriv*/ null, 11);

/* History manipulation */
    f("loadhistory", /*loadhistory*/ null, 11);
    f("savehistory", /*savehistory*/ null, 11);
    f("addhistory", /*addhistory*/ null, 11);

/* date-time manipulations */
    f("Sys.time", Time.class, 11);
    f("as.POSIXct", Time.class, 11);
    f("as.POSIXlt", Time.class, 11);
    f("format.POSIXlt", Time.class, 11);
    f("strptime", Time.class, 11);
    f("Date2POSIXlt", Time.class, 11);
    f("POSIXlt2Date", Time.class, 11);
    f("OlsonNames", Time.class, 11);


/* Connections */
    f("stdin", Connections.class, 11);
    f("stdout", Connections.class, 11);
    f("stderr", Connections.class, 11);
    f("isatty", Connections.class, 11);
    f("readLines",Connections.class, 11);
    f("writeLines", Connections.class, 11);
    f("readBin", Connections.class, 11);
    f("writeBin", Connections.class, 211);
    f("readChar", Connections.class, 11);
    f("writeChar", /*writechar*/ null, 211);
    f("open", Connections.class, 11);
    f("isOpen", Connections.class, 11);
    f("isIncomplete", Connections.class, 11);
    f("isSeekable", /*isseekable*/ null, 11);
    f("close", Connections.class, 11);
    f("flush", Connections.class, 11);
    f("file", Connections.class, 11);
    f("url", Connections.class, 11);
    f("pipe", /*pipe*/ null, 11);
    f("fifo", /*fifo*/ null, 11);
    f("gzfile", Connections.class, 11);
    f("bzfile", Connections.class, 11);
    f("xzfile", Connections.class, 11);
    f("unz", /*unz*/ null, 11);
    f("seek", /*seek*/ null, 11);
    f("truncate", /*truncate*/ null, 11);
    f("pushBack", Connections.class, 11);
    f("clearPushBack", null, 11);
    f("pushBackLength", Connections.class, 11);
    f("rawConnection", /*rawconnection*/ null, 11);
    f("rawConnectionValue", /*rawconvalue*/ null, 11);
    f("textConnection",  Connections.class, 11);
    f("textConnectionValue", /*textconvalue*/ null, 11);
    f("socketConnection", Connections.class, 11);
    f("sockSelect", /*sockselect*/ null, 11);
    f("getConnection", /*getconnection*/ null, 11);
    f("getAllConnections", /*getallconnections*/ null, 11);
    f("summary.connection", Connections.class, 11);
    f("download", /*download*/ null, 11);
    f("nsl", /*nsl*/ null, 11);
    f("gzcon", /*gzcon*/ null, 11);
    f("memCompress", /*memCompress*/ null, 11);
    f("memDecompress", /*memDecompress*/ null, 11);

    f("readDCF", DebianControlFiles.class, 11);

    f("getNumRtoCConverters", /*getNumRtoCConverters*/ null, 11);
    f("getRtoCConverterDescriptions", /*getRtoCConverterDescriptions*/ null, 11);
    f("getRtoCConverterStatus", /*getRtoCConverterStatus*/ null, 11);
    f("setToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 11);
    f("removeToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 11);

    f("lockEnvironment", Environments.class, 111);
    f("environmentIsLocked", Environments.class, 11);
    f("lockBinding", Environments.class, 111);
    f("unlockBinding", Environments.class, 111);
    f("bindingIsLocked", Environments.class, 11);
    f("makeActiveBinding", /*mkActiveBnd*/ Environments.class, 111);
    f("bindingIsActive", Environments.class, 11);
/* looks like mkUnbound is unused in base R */
    f("mkUnbound", /*mkUnbound*/ null, 111);
    f("isNamespace", Namespaces.class, 0);
  // hiding:  f("registerNamespace", Namespaces.class, 0, 11, 2);
   // hiding: f("unregisterNamespace", Namespaces.class, 0, 11, 1);
    f("getNamespace", Namespaces.class, 0);
    f("getRegisteredNamespace",Namespaces.class, 11);
    f("loadedNamespaces", Namespaces.class, 0);
    f("getNamespaceName", Namespaces.class, 0);
    f("getNamespaceExports", Namespaces.class, 0);
    f("getNamespaceImports", Namespaces.class, 0);
    f("registerS3method", Namespaces.class, 11);

    f("getNamespaceRegistry", Namespaces.class, 11);

   // hiding f("importIntoEnv", Namespaces.class, 0, 11, 4);
    f("env.profile", /*envprofile*/ null, 211);
    add(new NamespaceFunction("::"));
    add(new NamespaceFunction(":::"));
    f("getDataset", Namespaces.class, 11);
    f("find.package", Namespaces.class, 11);
    f("Encoding", Types.class, 11);
    f("setEncoding", Types.class, 11);
  // REMOVED: f("lazyLoadDBfetch", Serialization.class, 0, 1, 4);
    f("setTimeLimit", /*setTimeLimit*/ null, 111);
    f("setSessionTimeLimit", /*setSessionTimeLimit*/ null, 111);
    f("icuSetCollate", /*ICUset*/ null, 111) ;

    // jvm specific
    add(new ImportFunction());
    f("jload", Jvmi.class, 0);
    f("library", Packages.class, 11);
    f("require", Packages.class, 11);

    f("library.dynam", Namespaces.class, 11);
    f("library.dynam.unload", Namespaces.class, 11);

    // bitwise
    f("bitwiseNot", Bitwise.class, 11);
    f("bitwiseXor", Bitwise.class, 11);
    f("bitwiseShiftL", Bitwise.class, 11);
    f("bitwiseShiftR", Bitwise.class, 11);
    f("bitwiseAnd", Bitwise.class, 11);
    f("bitwiseOr", Bitwise.class, 11);

    // Add LAPACK wrappers as internals
    f("La_chol", Lapack.class, 11);
    f("La_chol2inv", Lapack.class, 11);
    f("La_dlange", Lapack.class, 11);
    f("La_dtrcon", Lapack.class, 11);
    f("La_dgecon", Lapack.class, 11);
    f("La_zgecon", Lapack.class, 11);
    f("La_ztrcon", Lapack.class, 11);
    f("La_solve", Lapack.class, 11);
    f("La_solve_cmplx", Lapack.class, 11);
    f("backsolve", Lapack.class, 11);


    f("tabulate", Base.class, 11);

    // Build map of reserved functions
    for (Map.Entry<Symbol, PrimitiveFunction> entry : builtins.entrySet()) {
      if(entry.getKey().isReservedWord()) {
        reserved.put(entry.getKey(), entry.getValue());
      }
    }
    for (Map.Entry<Symbol, Entry> entry : builtinEntries.entrySet()) {
      if(entry.getKey().isReservedWord()) {
        PrimitiveFunction fn = createFunction(entry.getValue());
        builtins.put(entry.getKey(), fn);
        reserved.put(entry.getKey(), fn);
      }
    }
  }

  private void add(PrimitiveFunction fn) {
    Symbol name = Symbol.get(fn.getName());
    builtins.put(name, fn);
  }

  private void add(Entry entry) {     
    if (entry.isInternal()) {
      internalEntries.put(Symbol.get(entry.name), entry);
    } else {
      builtinEntries.put(Symbol.get(entry.name), entry);
    }
  }

  private void addInternal(PrimitiveFunction fn) {
    internals.put(Symbol.get(fn.getName()), fn);
  }


  private void f(String name, Class clazz, int eval) {
    Entry e = new Entry();
    e.name = name;
    e.functionClass = clazz;
    e.eval = eval;
    add(e);
  }

  private void f(String name, Class clazz, String methodName, int eval) {
    Entry e = new Entry();
    e.name = name;
    e.functionClass = clazz;
    e.methodName = methodName;
    e.eval = eval;
    add(e);
  }



  public static class Entry {

    private Entry() {

    }

    public boolean isInternal() {
      return ((eval % 100) / 10) != 0;
    }

    private Entry(String name, Class functionClass, String methodName, int eval) {
      this.name = name;
      this.functionClass = functionClass;
      this.methodName = methodName;
      this.eval = eval;
    }

    /**
     * print name
     */
    public String name;

    public String group;

    /* c-code address */
    public Class functionClass;
    public String methodName;
    public int eval;     /* evaluate args? */

    public boolean isSpecial() {
      return eval % 10 == 0;
    }

  }
}
