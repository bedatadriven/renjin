/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang.primitive;


import static r.lang.primitive.PPkind.*;
import static r.lang.primitive.PPprec.*;
import static r.util.CDefines.ArithOpType.*;
import static r.util.CDefines.RelOpType.*;

public class FunctionTable {


  private static class PPinfo {
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

    /* c-code address */
    public Class functionClass;
    public String methodName;
    public Object code;     /* offset within c-code */
    public int eval;     /* evaluate args? */
    public int arity;    /* function arity */
    public PPinfo gram;     /* pretty-print info */
  }

  private static Entry f(String name, Class cfun, Object offset, int eval, int arity, PPkind kind, PPprec prec, int rightassoc) {
    return new Entry(name, cfun, name, offset, eval, arity, new PPinfo(kind, prec, rightassoc));
  }

    private static Entry f(String name, Class cfun, String methodName, Object offset, int eval, int arity, PPkind kind, PPprec prec, int rightassoc) {
    return new Entry(name, cfun,methodName, offset, eval, arity, new PPinfo(kind, prec, rightassoc));
  }


  public static final Entry[] ENTRIES = new Entry[]{
      f("if", Evaluation.class, 0, 200, -1, PP_IF, PREC_FN, 1),
      f("while", Evaluation.class,  0, 100, -1, PP_WHILE, PREC_FN, 0),
      f("for", Evaluation.class,  0, 100, -1, PP_FOR, PREC_FN, 0),
      f("repeat", Evaluation.class, 0, 100, -1, PP_REPEAT, PREC_FN, 0),
      f("break", Evaluation.class, 0, 0, -1, PP_BREAK, PREC_FN, 0),
      f("next", Evaluation.class, 0,0, -1, PP_NEXT, PREC_FN, 0),
      f("return", Evaluation.class, 0, 0, -1, PP_RETURN, PREC_FN, 0),
      f("stop", Evaluation.class, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("warning", /*warning*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),

      f("gettext", /*gettext*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("ngettext", /*ngettext*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("bindtextdomain", /*sbindtextdomain*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f(".addCondHands", /*addCondHands*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f(".resetCondHands", /*resetCondHands*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f(".signalCondition", /*ignalCondition*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f(".dfltStop", /*dfltStop*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f(".dfltWarn", /*dfltWarn*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f(".addRestart", /*addRestart*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f(".getRestart", /*getRestart*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f(".invokeRestart", /*invokeRestart*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f(".addTryHandlers", /*addTryHandlers*/ null, 0, 111, 0, PP_FUNCALL, PREC_FN, 0),

      f("geterrmessage", /*geterrmessage*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("seterrmessage", /*seterrmessage*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("printDeferredWarnings", /*printDeferredWarnings*/ null, 0, 111, 0, PP_FUNCALL, PREC_FN, 0),
      f("interruptsSuspended", /*interruptsSuspended*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("restart", /*restart*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("function", Evaluation.class, "function", 0, 0, -1, PP_FUNCTION, PREC_FN, 0),
      f("as.function.default", /*asfunction*/ null, 0, 11, 2, PP_FUNCTION, PREC_FN, 0),
      f("<-", Evaluation.class, 1, 100, -1, PP_ASSIGN, PREC_LEFT, 1),
      f("=", /*set*/ null, 3, 100, -1, PP_ASSIGN, PREC_EQ, 1),
      f("<<-", /*set*/ null, 2, 100, -1, PP_ASSIGN2, PREC_LEFT, 1),
      f("{", Evaluation.class, 0, 200, -1, PP_CURLY, PREC_FN, 0),
      f("(", Evaluation.class, 0, 1, 1, PP_PAREN, PREC_FN, 0),
      f("subset", /*subset_dflt*/ null, 1, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f(".subset2", /*subset2_dflt*/ null, 2, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("[",Subset.class, 1, 0, -1, PP_SUBSET, PREC_SUBSET, 0),
      f("[[", Subset.class, 2, 0, -1, PP_SUBSET, PREC_SUBSET, 0),
      f("$", Subset.class, 3, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0),
      f("@", /*AT*/ null, 0, 0, 2, PP_DOLLAR, PREC_DOLLAR, 0),
      f("[<-", Subset.class, 0, 0, 3, PP_SUBASS, PREC_LEFT, 1),
      f("[[<-", /*subassign2*/ null, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1),
      f("$<-", Subset.class, 1, 0, 3, PP_SUBASS, PREC_LEFT, 1),
      f("switch", /*switch*/ null, 0, 210, -1, PP_FUNCALL, PREC_FN, 0),
      f("browser", /*browser*/ null, 0, 101, 3, PP_FUNCALL, PREC_FN, 0),
      f("debug", /*debug*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("undebug", /*debug*/ null, 1, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("isdebugged", /*debug*/ null, 2, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("debugonce", /*debug*/ null, 3, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f(".primTrace", /*trace*/ null, 0, 101, 1, PP_FUNCALL, PREC_FN, 0),
      f(".primUntrace", /*trace*/ null, 1, 101, 1, PP_FUNCALL, PREC_FN, 0),
      f(".Internal", Evaluation.class, "internal", 0, 200, 1, PP_FUNCALL, PREC_FN, 0),
      f("on.exit", Evaluation.class, "onExit", 0, 100, 1, PP_FUNCALL, PREC_FN, 0),
      f("Recall", /*recall*/ null, 0, 210, -1, PP_FUNCALL, PREC_FN, 0),
      f("delayedAssign", /*delayed*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("makeLazy", Connections.class, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f(".Primitive", /*primitive*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("identical", /*identical*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),


/* Binary Operators */
/* these are group generic and so need to eval args */
      f("+",  MathExt.class,"plus", PLUSOP, 1, 2, PP_BINARY, PREC_SUM, 0),
      f("-", MathExt.class, "minus", MINUSOP, 1, 2, PP_BINARY, PREC_SUM, 0),
      f("*", MathExt.class,  "multiply", TIMESOP, 1, 2, PP_BINARY, PREC_PROD, 0),
      f("/", MathExt.class, "divide", DIVOP, 1, 2, PP_BINARY2, PREC_PROD, 0),
      f("^", Math.class, "pow", POWOP, 1, 2, PP_BINARY2, PREC_POWER, 1),
      f("%%", /*arith*/ null, MODOP, 1, 2, PP_BINARY2, PREC_PERCENT, 0),
      f("%/%", /*arith*/ null, IDIVOP, 1, 2, PP_BINARY2, PREC_PERCENT, 0),

      f("%*%", /*matprod*/ null, 0, 1, 2, PP_BINARY, PREC_PERCENT, 0),
      f("crossprod", /*matprod*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("tcrossprod", /*matprod*/ null, 2, 11, 2, PP_FUNCALL, PREC_FN, 0),

/* these are group generic and so need to eval args */
      f("==", Comparison.class, "equalTo", EQOP, 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f("!=", Comparison.class, "notEqualTo", NEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f("<", Comparison.class, "lessThan", LTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f("<=", Comparison.class, "lessThanOrEqualTo", LEOP, 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f(">=", Comparison.class, "greaterThanOrEqualTo", 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f(">", Comparison.class, "greaterThan", GTOP, 1, 2, PP_BINARY, PREC_COMPARE, 0),
      f("&", Comparison.class, "bitwiseAnd", 1, 1, 2, PP_BINARY, PREC_AND, 0),
      f("|", Comparison.class, "bitwiseOr", 2, 1, 2, PP_BINARY, PREC_OR, 0),
      f("!", Comparison.class, "not", 3, 1, 1, PP_UNARY, PREC_NOT, 0),

      f("&&", Comparison.class, "and", 1, 0, 2, PP_BINARY, PREC_AND, 0),
      f("||", Comparison.class, "or", 2, 0, 2, PP_BINARY, PREC_OR, 0),
      f(":", Sequences.class, "colon", 0, 1, 2, PP_BINARY2, PREC_COLON, 0),
      f("~", /*tilde*/ null, 0, 0, 2, PP_BINARY, PREC_TILDE, 0),


/* Logic Related Functions */
/* these are group generic and so need to eval args */
      f("all", /*logic3*/ null, 1, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("any", /*logic3*/ null, 2, 1, -1, PP_FUNCALL, PREC_FN, 0),


/* Vectors, Matrices and Arrays */

/* printname	c-entry		offset	eval	arity	pp-kind	     precedence	rightassoc
 * ---------	-------		------	----	-----	-------      ----------	----------*/
      f("vector", Types.class, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("complex", /*complex*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("matrix", /*matrix*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("length", Types.class, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("length<-", /*lengthgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("row", /*rowscols*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("col", /*rowscols*/ null, 2, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("c", Combine.class, "combine",  0, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("unlist", /*unlist*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("cbind", /*bind*/ null, 1, 10, -1, PP_FUNCALL, PREC_FN, 0),
      f("rbind", /*bind*/ null, 2, 10, -1, PP_FUNCALL, PREC_FN, 0),
      f("drop", /*drop*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("oldClass", Types.class, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("oldClass<-", /*classgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("class", Types.class, "getClass", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("class<-", Types.class, "setClass", 0, 1, 2, PP_FUNCALL, PREC_FN, 0),
      f("unclass", /*unclass*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("names", Types.class,  "getNames", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("names<-", Types.class, "setNames", 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("dimnames", /*dimnames*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("dimnames<-", /*dimnamesgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("all.names", /*allnames*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("dim", /*dim*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("dim<-", /*dimgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("attributes", /*attributes*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("attributes<-", /*attributesgets*/ null, 0, 1, 1, PP_FUNCALL, PREC_LEFT, 1),
      f("attr", Types.class, 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("attr<-", Types.class, 0, 1, 3, PP_FUNCALL, PREC_LEFT, 1),
      f("comment", /*comment*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("comment<-", /*commentgets*/ null, 0, 11, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("levels<-", /*levelsgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("get", Types.class, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("mget", /*mget*/ null, 1, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("exists", Types.class, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("assign", Evaluation.class, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("remove", /*remove*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("duplicated", /*duplicated*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("unique", /*duplicated*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("anyDuplicated", /*duplicated*/ null, 2, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("which.min", /*first_min*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("pmin", /*pmin*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("pmax", /*pmin*/ null, 1, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("which.max", /*first_min*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("match", Match.class, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("pmatch", /*pmatch*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("charmatch", /*charmatch*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("match.call", /*matchcall*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("complete.cases", /*compcases*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),

      f("attach", /*attach*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("detach", /*detach*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("search", Types.class, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),


/* Mathematical Functions */
/* these are group generic and so need to eval args */
/* Note that the number of arguments for the primitives in the Math group
   only applies to the default method. */
      f("round", Math.class, 10001, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("signif", /*Math2*/ null, 10004, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("atan",Math.class, 10002, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("log", /*log*/ null, 10003, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("log10", /*log1arg*/ null, 10, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("log2", /*log1arg*/ null, 2, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("abs", Math.class, 6, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("floor", Math.class, 1, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("ceiling", Math.class, "ceil", 2, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("sqrt", Math.class, 3, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("sign", Math.class, "signnum", 4, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("trunc", /*trunc*/ null, 5, 1, -1, PP_FUNCALL, PREC_FN, 0),

      f("exp", Math.class, 10, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("expm1", /*math1*/ null, 11, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("log1p", /*math1*/ null, 12, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("cos", Math.class, 20, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("sin", Math.class, 21, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("tan", Math.class, 22, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("acos", Math.class, 23, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("asin", Math.class, 24, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("cosh", Math.class, 30, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("sinh", Math.class, 31, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("tanh", Math.class, 32, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("acosh", /*math1*/ null, 33, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("asinh", /*math1*/ null, 34, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("atanh", /*math1*/ null, 35, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("lgamma", org.apache.commons.math.special.Gamma.class, "logGamma", 40, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("gamma", cern.jet.stat.Gamma.class, 41, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("digamma", org.apache.commons.math.special.Gamma.class, 42, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("trigamma",org.apache.commons.math.special.Gamma.class, 43, 1, 1, PP_FUNCALL, PREC_FN, 0),
/* see "psigamma" below !*/

/* Mathematical Functions of Two Numeric (+ 1-2 int) Variables */

      f("atan2", /*math2*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("lbeta", /*math2*/ null, 2, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("beta", /*math2*/ null, 3, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("lchoose", /*math2*/ null, 4, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("choose", /*math2*/ null, 5, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("dchisq", /*math2*/ null, 6, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pchisq", /*math2*/ null, 7, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qchisq", /*math2*/ null, 8, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dexp", /*math2*/ null, 9, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pexp", /*math2*/ null, 10, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qexp", /*math2*/ null, 11, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dgeom", /*math2*/ null, 12, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pgeom", /*math2*/ null, 13, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qgeom", /*math2*/ null, 14, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dpois", /*math2*/ null, 15, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("ppois", /*math2*/ null, 16, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qpois", /*math2*/ null, 17, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dt", /*math2*/ null, 18, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pt", /*math2*/ null, 19, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qt", /*math2*/ null, 20, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dsignrank", /*math2*/ null, 21, 11, 2 + 1, PP_FUNCALL, PREC_FN, 0),
      f("psignrank", /*math2*/ null, 22, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qsignrank", /*math2*/ null, 23, 11, 2 + 2, PP_FUNCALL, PREC_FN, 0),

      f("besselJ", /*math2*/ null, 24, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("besselY", /*math2*/ null, 25, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("psigamma", /*math2*/ null, 26, 11, 2, PP_FUNCALL, PREC_FN, 0),


/* Mathematical Functions of a Complex Argument */
/* these are group generic and so need to eval args */

      f("Re", /*cmathfuns*/ null, 1, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("Im", /*cmathfuns*/ null, 2, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("Mod", /*cmathfuns*/ null, 3, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("Arg", /*cmathfuns*/ null, 4, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("Conj", /*cmathfuns*/ null, 5, 1, 1, PP_FUNCALL, PREC_FN, 0),


/* Mathematical Functions of Three Numeric (+ 1-2 int) Variables */

      f("dbeta", /*math3*/ null, 1, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pbeta", /*math3*/ null, 2, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qbeta", /*math3*/ null, 3, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dbinom", /*math3*/ null, 4, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pbinom", /*math3*/ null, 5, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qbinom", /*math3*/ null, 6, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dcauchy", /*math3*/ null, 7, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pcauchy", /*math3*/ null, 8, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qcauchy", /*math3*/ null, 9, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("df", /*math3*/ null, 10, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pf", /*math3*/ null, 11, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qf", /*math3*/ null, 12, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dgamma", /*math3*/ null, 13, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pgamma", /*math3*/ null, 14, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qgamma", /*math3*/ null, 15, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dlnorm", /*math3*/ null, 16, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("plnorm", /*math3*/ null, 17, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qlnorm", /*math3*/ null, 18, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dlogis", /*math3*/ null, 19, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("plogis", /*math3*/ null, 20, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qlogis", /*math3*/ null, 21, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnbinom", /*math3*/ null, 22, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnbinom", /*math3*/ null, 23, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnbinom", /*math3*/ null, 24, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnorm", /*math3*/ null, 25, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnorm", /*math3*/ null, 26, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnorm", /*math3*/ null, 27, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dunif", /*math3*/ null, 28, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("punif", /*math3*/ null, 29, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qunif", /*math3*/ null, 30, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dweibull", /*math3*/ null, 31, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pweibull", /*math3*/ null, 32, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qweibull", /*math3*/ null, 33, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnchisq", /*math3*/ null, 34, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnchisq", /*math3*/ null, 35, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnchisq", /*math3*/ null, 36, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnt", /*math3*/ null, 37, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnt", /*math3*/ null, 38, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnt", /*math3*/ null, 39, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dwilcox", /*math3*/ null, 40, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pwilcox", /*math3*/ null, 41, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qwilcox", /*math3*/ null, 42, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),

      f("besselI", /*math3*/ null, 43, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("besselK", /*math3*/ null, 44, 11, 3, PP_FUNCALL, PREC_FN, 0),

      f("dnbinom_mu", /*math3*/ null, 45, 11, 3 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnbinom_mu", /*math3*/ null, 46, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnbinom_mu", /*math3*/ null, 47, 11, 3 + 2, PP_FUNCALL, PREC_FN, 0),


/* Mathematical Functions of Four Numeric (+ 1-2 int) Variables */

      f("dhyper", /*math4*/ null, 1, 11, 4 + 1, PP_FUNCALL, PREC_FN, 0),
      f("phyper", /*math4*/ null, 2, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qhyper", /*math4*/ null, 3, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnbeta", /*math4*/ null, 4, 11, 4 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnbeta", /*math4*/ null, 5, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnbeta", /*math4*/ null, 6, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dnf", /*math4*/ null, 7, 11, 4 + 1, PP_FUNCALL, PREC_FN, 0),
      f("pnf", /*math4*/ null, 8, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qnf", /*math4*/ null, 9, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),

      f("dtukey", /*math4*/ null, 10, 11, 4 + 1, PP_FUNCALL, PREC_FN, 0),
      f("ptukey", /*math4*/ null, 11, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),
      f("qtukey", /*math4*/ null, 12, 11, 4 + 2, PP_FUNCALL, PREC_FN, 0),

/* Random Numbers */

      f("rchisq", /*random1*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rexp", /*random1*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rgeom", /*random1*/ null, 2, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rpois", /*random1*/ null, 3, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rt", /*random1*/ null, 4, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rsignrank", /*random1*/ null, 5, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("rbeta", /*random2*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rbinom", /*random2*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rcauchy", /*random2*/ null, 2, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rf", /*random2*/ null, 3, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rgamma", /*random2*/ null, 4, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rlnorm", /*random2*/ null, 5, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rlogis", /*random2*/ null, 6, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rnbinom", /*random2*/ null, 7, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rnbinom_mu", /*random2*/ null, 13, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rnchisq", /*random2*/ null, 12, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rnorm", /*random2*/ null, 8, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("runif", /*random2*/ null, 9, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rweibull", /*random2*/ null, 10, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rwilcox", /*random2*/ null, 11, 11, 3, PP_FUNCALL, PREC_FN, 0),

      f("rhyper", /*random3*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),

      f("rmultinom", /*rmultinom*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("sample", /*sample*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),

      f("RNGkind", /*RNGkind*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("set.seed", /*setseed*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),

/* Data Summaries */
/* sum, min, max, prod, range are group generic and so need to eval args */
      f("sum", /*summary*/ null, 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("mean", /*summary*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("min", /*summary*/ null, 2, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("max", /*summary*/ null, 3, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("prod", /*summary*/ null, 4, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("range", /*range*/ null, 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("cov", /*cov*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("cor", /*cov*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),

/* Note that the number of arguments in this group only applies
   to the default method */
      f("cumsum", /*cum*/ null, 1, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("cumprod", /*cum*/ null, 2, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("cummax", /*cum*/ null, 3, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("cummin", /*cum*/ null, 4, 1, 1, PP_FUNCALL, PREC_FN, 0),

/* Type coercion */

      f("as.character", Types.class, "asCharacter", 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("as.integer", Types.class, "asInteger", 1, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("as.double", Types.class, "asDouble",  2, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("as.complex", /*ascharacter*/ null, 3, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("as.logical", Types.class, "asLogical", 4, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("as.raw", /*ascharacter*/ null, 5, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("as.vector", /*asvector*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("paste", Text.class, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("file.path", /*filepath*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("format", /*format*/ null, 0, 11, 8, PP_FUNCALL, PREC_FN, 0),
      f("format.info", /*formatinfo*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("cat", /*cat*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("call", /*call*/ null, 0, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("do.call", /*docall*/ null, 0, 211, 3, PP_FUNCALL, PREC_FN, 0),
      f("as.call", /*ascall*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("type.convert", /*typecvt*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("as.environment", Types.class, "asEnvironment", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("storage.mode<-", /*storage_mode*/ null, 0, 1, 2, PP_FUNCALL, PREC_FN, 0),


/* String Manipulation */

      f("nchar", /*nchar*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("nzchar", /*nzchar*/ null, 1, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("substr", /*substr*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("substr<-", /*substrgets*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("strsplit", /*strsplit*/ null, 1, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("abbreviate", /*abbrev*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("make.names", /*makenames*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("grep", /*grep*/ null, 0, 11, 9, PP_FUNCALL, PREC_FN, 0),
      f("grepl", /*grep*/ null, 1, 11, 9, PP_FUNCALL, PREC_FN, 0),
      f("sub", /*gsub*/ null, 0, 11, 8, PP_FUNCALL, PREC_FN, 0),
      f("gsub", /*gsub*/ null, 1, 11, 8, PP_FUNCALL, PREC_FN, 0),
      f("regexpr", /*regexpr*/ null, 1, 11, 7, PP_FUNCALL, PREC_FN, 0),
      f("gregexpr", /*gregexpr*/ null, 1, 11, 7, PP_FUNCALL, PREC_FN, 0),
      f("agrep", /*agrep*/ null, 1, 11, 9, PP_FUNCALL, PREC_FN, 0),
      f("tolower", /*tolower*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("toupper", /*tolower*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("chartr", /*chartr*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("sprintf", /*sprintf*/ null, 1, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("make.unique", /*makeunique*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("charToRaw", /*charToRaw*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("rawToChar", /*rawToChar*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("rawShift", /*rawShift*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("intToBits", /*intToBits*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("rawToBits", /*rawToBits*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("packBits", /*packBits*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("utf8ToInt", /*utf8ToInt*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("intToUtf8", /*intToUtf8*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("encodeString", /*encodeString*/ null, 1, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("iconv", /*iconv*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("strtrim", /*strtrim*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

/* Type Checking (typically implemented in ./coerce.c ) */

      f("is.null", Types.class, "isNull", 0 /*NILSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.logical", Types.class, "isLogical" ,0 /*LGLSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.integer", Types.class, "isInteger", 0 /*INTSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.real", Types.class, "isReal", 0/*REALSXP */, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.double", Types.class, "isDouble", 0 /*REALSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.complex", Types.class, "isComplex", 0/*CPLXSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.character", Types.class,"isCharacter", 0 /*STRSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.symbol", Types.class, "isSymbol", 0 /*SYMSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.environment", Types.class, "isEnvironment", 0/* ENVSXP */, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.list", Types.class,"isList", 0/* VECSXP */, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.pairlist", Types.class, "isPairList", 0 /*LISTSXP */, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.expression", Types.class, "isExpression",  0 /* EXPRSXP*/, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.raw", /*is*/ null,0 /* RAWSXP */, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.object", /*is*/ null, 50, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.numeric", Types.class, "isNumeric", 100, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.matrix", /*is*/ null, 101, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.array", /*is*/ null, 102, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.atomic", Types.class, "isAtomic", 200, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.recursive", Types.class, "isRecursive",201, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.call",  Types.class, "isCall", 300, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.language", Types.class, "isLanguage",  301, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.function", Types.class, "isFunction", 302, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.single", Types.class,"isSingle", 999, 1, 1, PP_FUNCALL, PREC_FN, 0),

      f("is.vector", null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("is.na", Types.class, "isNA", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.nan", Types.class, "isNaN", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.finite", Types.class, "isFinite", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("is.infinite", Types.class, "isInfinite",  0, 1, 1, PP_FUNCALL, PREC_FN, 0),


/* Miscellaneous */

      f("proc.time", /*proctime*/ null, 0, 1, 0, PP_FUNCALL, PREC_FN, 0),
      f("gc.time", /*gctime*/ null, 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("Version", /*version*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("machine", /*machine*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("commandArgs", /*commandArgs*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("unzip", /*unzip*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("parse", /*parse*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("parse_Rd", /*parseRd*/ null, 0, 11, 7, PP_FUNCALL, PREC_FN, 0),
      f("save", /*save*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("saveToConn", /*saveToConn*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("load", /*load*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("loadFromConn2", /*loadFromConn2*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("serializeToConn", /*serializeToConn*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("unserializeFromConn", Connections.class, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("deparse", /*deparse*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("deparseRd", /*deparseRd*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("dput", /*dput*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("dump", /*dump*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("substitute", Evaluation.class, 0, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("quote", Evaluation.class, 0, 0, 1, PP_FUNCALL, PREC_FN, 0),
      f("quit", /*quit*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("interactive", /*interactive*/ null, 0, 0, 0, PP_FUNCALL, PREC_FN, 0),
      f("readline", /*readln*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("menu", /*menu*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("print.default", /*printdefault*/ null, 0, 111, 9, PP_FUNCALL, PREC_FN, 0),
      f("print.function", /*printfunction*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("prmatrix", /*prmatrix*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("invisible", /*invisible*/ null, 0, 101, 1, PP_FUNCALL, PREC_FN, 0),
      f("gc", /*gc*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("gcinfo", /*gcinfo*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("gctorture", /*gctorture*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("memory.profile", /*memoryprofile*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("rep", /*rep*/ null, 0, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("rep.int", /*rep_int*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("seq.int", /*seq*/ null, 0, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("seq_len", /*seq_len*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("seq_along", Sequences.class, "seqAlong", 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("list", Types.class, "list", 1, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("split", /*split*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("is.loaded", /*isloaded*/ null, 0, 11, -1, PP_FOREIGN, PREC_FN, 0),
      f(".C", /*dotCode*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f(".Fortran", /*dotCode*/ null, 1, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f(".External", /*External*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f(".Call", /*dotcall*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f(".External.graphics", /*Externalgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f(".Call.graphics", /*dotcallgr*/ null, 0, 1, -1, PP_FOREIGN, PREC_FN, 0),
      f("recordGraphics", /*recordGraphics*/ null, 0, 211, 3, PP_FOREIGN, PREC_FN, 0),
      f("dyn.load", /*dynload*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("dyn.unload", /*dynunload*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("ls", /*ls*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("typeof", Types.class, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("eval", Evaluation.class, 0, 211, 3, PP_FUNCALL, PREC_FN, 0),
      f("eval.with.vis", /*eval*/ null, 1, 211, 3, PP_FUNCALL, PREC_FN, 0),
      f("withVisible", /*withVisible*/ null, 1, 10, 1, PP_FUNCALL, PREC_FN, 0),
      f("expression", /*expression*/ null, 1, 0, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.parent", /*sys*/ null, 1, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.call", /*sys*/ null, 2, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.frame", /*sys*/ null, 3, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.nframe", /*sys*/ null, 4, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.calls", /*sys*/ null, 5, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.frames", /*sys*/ null, 6, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.on.exit", /*sys*/ null, 7, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.parents", /*sys*/ null, 8, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sys.function", /*sys*/ null, 9, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("browserText", /*sysbrowser*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("browserCondition", /*sysbrowser*/ null, 2, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("browserSetDebug", /*sysbrowser*/ null, 3, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("parent.frame", Types.class, "parentFrame", 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("sort", /*sort*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("is.unsorted", /*isunsorted*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("psort", /*psort*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("qsort", /*qsort*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("radixsort", /*radixsort*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("order", /*order*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("rank", /*rank*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("missing", Evaluation.class, "missing", 1, 0, 1, PP_FUNCALL, PREC_FN, 0),
      f("nargs", /*nargs*/ null, 1, 0, 0, PP_FUNCALL, PREC_FN, 0),
      f("scan", /*scan*/ null, 0, 11, 18, PP_FUNCALL, PREC_FN, 0),
      f("count.fields", /*countfields*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("readTableHead", /*readtablehead*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("t.default", /*transpose*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("aperm", /*aperm*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("builtins", /*builtins*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("edit", /*edit*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("dataentry", /*dataentry*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("dataviewer", /*dataviewer*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("args", /*args*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("formals", /*formals*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("body", /*body*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("bodyCode", /*bodyCode*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("emptyenv", /*emptyenv*/ null, 0, 1, 0, PP_FUNCALL, PREC_FN, 0),
      f("baseenv", Types.class, "baseEnv", 0, 1, 0, PP_FUNCALL, PREC_FN, 0),
      f("globalenv", Types.class, "globalEnv", 0, 1, 0, PP_FUNCALL, PREC_FN, 0),
      f("environment", Types.class, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("environment<-", /*envirgets*/ null, 0, 1, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("environmentName", /*envirName*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("env2list", /*env2list*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("reg.finalizer", /*regFinaliz*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("options", /*options*/ null, 0, 211, 1, PP_FUNCALL, PREC_FN, 0),
      f("sink", /*sink*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("sink.number", /*sinknumber*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("lib.fixup", /*libfixup*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("pos.to.env", /*pos2env*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("eapply", /*eapply*/ null, 0, 10, 4, PP_FUNCALL, PREC_FN, 0),
      f("lapply", /*lapply*/ null, 0, 10, 2, PP_FUNCALL, PREC_FN, 0),
      f("rapply", /*rapply*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("islistfactor", /*islistfactor*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("colSums", /*colsum*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("colMeans", /*colsum*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("rowSums", /*colsum*/ null, 2, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("rowMeans", /*colsum*/ null, 3, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("Rprof", /*Rprof*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("Rprofmem", /*Rprofmem*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("tracemem", /*memtrace*/ null, 0, 1, 1, PP_FUNCALL, PREC_FN, 0),
      f("retracemem", /*memretrace*/ null, 0, 1, -1, PP_FUNCALL, PREC_FN, 0),
      f("untracemem", /*memuntrace*/ null, 0, 101, 1, PP_FUNCALL, PREC_FN, 0),
      f("object.size", /*objectsize*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("inspect", /*inspect*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("mem.limits", /*memlimits*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("merge", /*merge*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("capabilities", /*capabilities*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("capabilitiesX11", /*capabilitiesX11*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("new.env", Types.class, "newEnv", 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("parent.env", Types.class, "getParentEnv", 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("parent.env<-", Types.class, "setParentEnv", 0, 11, 2, PP_FUNCALL, PREC_LEFT, 1),
      f("visibleflag", /*visibleflag*/ null, 0, 1, 0, PP_FUNCALL, PREC_FN, 0),
      f("l10n_info", /*l10n_info*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("Cstack_info", /*Cstack_info*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("startHTTPD", /*startHTTPD*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("stopHTTPD", /*stopHTTPD*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),

/* Functions To Interact with the Operating System */

      f("file.show", /*fileshow*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("file.edit", /*fileedit*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("file.create", /*filecreate*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("file.remove", /*fileremove*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("file.rename", /*filerename*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("file.append", /*fileappend*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("codeFiles.append", /*fileappend*/ null, 1, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("file.symlink", /*filesymlink*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("file.copy", /*filecopy*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("list.files", /*listfiles*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("file.exists", /*fileexists*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("file.choose", /*filechoose*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("file.info", System.class, "fileInfo", 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("file.access", /*fileaccess*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("dir.create", /*dircreate*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("tempfile", /*tempfile*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("tempdir", /*tempdir*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("R.home", System.class, "getRHome", 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("date", /*date*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("index.search", /*indexsearch*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("Sys.getenv", /*getenv*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("Sys.setenv", /*setenv*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("Sys.unsetenv", /*unsetenv*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("getwd", /*getwd*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("setwd", /*setwd*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("basename", /*basename*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("dirname", /*dirname*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("dirchmod", /*dirchmod*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.chmod", /*syschmod*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("Sys.umask", /*sysumask*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.readlink", /*readlink*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.info", /*sysinfo*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("Sys.sleep", /*syssleep*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.getlocale", /*getlocale*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.setlocale", /*setlocale*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("Sys.localeconv", /*localeconv*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("path.expand", System.class, "pathExpand", 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.getpid", /*sysgetpid*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("normalizePath", /*normalizepath*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("Sys.glob", System.class, "glob", 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("unlink", /*unlink*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),

/* Complex Valued Functions */
      f("fft", /*fft*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("mvfft", /*mvfft*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("nextn", /*nextn*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("polyroot", /*polyroot*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),

/* Device Drivers */


/* Graphics */

      f("dev.control", /*devcontrol*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.displaylist", /*devcontrol*/ null, 1, 111, 0, PP_FUNCALL, PREC_FN, 0),
      f("dev.copy", /*devcopy*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.cur", /*devcur*/ null, 0, 111, 0, PP_FUNCALL, PREC_FN, 0),
      f("dev.next", /*devnext*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.off", /*devoff*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.prev", /*devprev*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.set", /*devset*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("rgb", /*rgb*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("rgb256", /*rgb*/ null, 1, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("rgb2hsv", /*RGB2hsv*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("hsv", /*hsv*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("hcl", /*hcl*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("gray", /*gray*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("colors", /*colors*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("col2rgb", /*col2RGB*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("palette", /*palette*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("plot.new", /*plot_new*/ null, 0, 111, 0, PP_FUNCALL, PREC_FN, 0),
      f("plot.window", /*plot_window*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("axis", /*axis*/ null, 0, 111, 13, PP_FUNCALL, PREC_FN, 0),
      f("plot.xy", /*plot_xy*/ null, 0, 111, 7, PP_FUNCALL, PREC_FN, 0),
      f("text", /*text*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0),
      f("mtext", /*mtext*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("title", /*title*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("abline", /*abline*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("box", /*box*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("rect", /*rect*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("polygon", /*polygon*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("xspline", /*xspline*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0),
      f("par", /*par*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("segments", /*segments*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0),
      f("arrows", /*arrows*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0),
      f("layout", /*layout*/ null, 0, 111, 10, PP_FUNCALL, PREC_FN, 0),
      f("locator", /*locator*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("identify", /*identify*/ null, 0, 211, 8, PP_FUNCALL, PREC_FN, 0),
      f("strheight", /*strheight*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("strwidth", /*strwidth*/ null, 0, 11, -1, PP_FUNCALL, PREC_FN, 0),
      f("contour", /*contour*/ null, 0, 11, 12, PP_FUNCALL, PREC_FN, 0),
      f("contourLines", /*contourLines*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("image", /*image*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("dend", /*dend*/ null, 0, 111, 6, PP_FUNCALL, PREC_FN, 0),
      f("dend.window", /*dendwindow*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("erase", /*erase*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("persp", /*persp*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("filledcontour", /*filledcontour*/ null, 0, 111, 5, PP_FUNCALL, PREC_FN, 0),
      f("getSnapshot", /*getSnapshot*/ null, 0, 111, 0, PP_FUNCALL, PREC_FN, 0),
      f("playSnapshot", /*playSnapshot*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("symbols", /*symbols*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0),
      f("getGraphicsEvent", /*getGraphicsEvent*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("devAskNewPage", /*devAskNewPage*/ null, 0, 211, 1, PP_FUNCALL, PREC_FN, 0),
      f("dev.size", /*devsize*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("clip", /*clip*/ null, 0, 111, 4, PP_FUNCALL, PREC_FN, 0),
      f("grconvertX", /*convertXY*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("grconvertY", /*convertXY*/ null, 1, 11, 3, PP_FUNCALL, PREC_FN, 0),

/* Objects */
      f("inherits", Types.class, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("UseMethod", /*usemethod*/ null, 0, 200, -1, PP_FUNCALL, PREC_FN, 0),
      f("NextMethod", /*nextmethod*/ null, 0, 210, -1, PP_FUNCALL, PREC_FN, 0),
      f("standardGeneric", /*standardGeneric*/ null, 0, 201, -1, PP_FUNCALL, PREC_FN, 0),

/* Modelling Functionality */

      f("nlm", /*nlm*/ null, 0, 11, 11, PP_FUNCALL, PREC_FN, 0),
      f("fmin", /*fmin*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("zeroin", /*zeroin*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("zeroin2", /*zeroin2*/ null, 0, 11, 7, PP_FUNCALL, PREC_FN, 0),
      f("optim", /*optim*/ null, 0, 11, 7, PP_FUNCALL, PREC_FN, 0),
      f("optimhess", /*optimhess*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("terms.formula", /*termsform*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("update.formula", /*updateform*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("model.frame", /*modelframe*/ null, 0, 11, 8, PP_FUNCALL, PREC_FN, 0),
      f("model.matrix", /*modelmatrix*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("D", /*D*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("deriv.default", /*deriv*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),

/* History manipulation */
      f("loadhistory", /*loadhistory*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("savehistory", /*savehistory*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("addhistory", /*addhistory*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),

/* date-time manipulations */
      f("Sys.time", System.class, "sysTime", 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("as.POSIXct", /*asPOSIXct*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("as.POSIXlt", /*asPOSIXlt*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("format.POSIXlt", /*formatPOSIXlt*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("strptime", DateTime.class,/*strptime*/  0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("Date2POSIXlt", /*D2POSIXlt*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("POSIXlt2Date", /*POSIXlt2D*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),


/* Connections */
      f("stdin", /*stdin*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("stdout", /*stdout*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("stderr", /*stderr*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("readLines", /*readLines*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("writeLines", /*writelines*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("readBin", /*readbin*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("writeBin", /*writebin*/ null, 0, 211, 5, PP_FUNCALL, PREC_FN, 0),
      f("readChar", /*readchar*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("writeChar", /*writechar*/ null, 0, 211, 5, PP_FUNCALL, PREC_FN, 0),
      f("open", /*open*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("isOpen", /*isopen*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("isIncomplete", /*isincomplete*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("isSeekable", /*isseekable*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("close", Connections.class, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("flush", /*flush*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("file", /*url*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("url", /*url*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("pipe", /*pipe*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("fifo", /*fifo*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("gzfile", Connections.class, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("bzfile", /*gzfile*/ null, 1, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("xzfile", /*gzfile*/ null, 2, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("unz", /*unz*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("seek", /*seek*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("truncate", /*truncate*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("pushBack", /*pushback*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("clearPushBack", /*clearpushback*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("pushBackLength", /*pushbacklength*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("rawConnection", /*rawconnection*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("rawConnectionValue", /*rawconvalue*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("textConnection", /*textconnection*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("textConnectionValue", /*textconvalue*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("socketConnection", /*sockconn*/ null, 0, 11, 6, PP_FUNCALL, PREC_FN, 0),
      f("sockSelect", /*sockselect*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("getConnection", /*getconnection*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("getAllConnections", /*getallconnections*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("summary.connection", /*sumconnection*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("download", /*download*/ null, 0, 11, 5, PP_FUNCALL, PREC_FN, 0),
      f("nsl", /*nsl*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("gzcon", /*gzcon*/ null, 0, 11, 3, PP_FUNCALL, PREC_FN, 0),
      f("memCompress", /*memCompress*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("memDecompress", /*memDecompress*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("readDCF", /*readDCF*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),

      f("getNumRtoCConverters", /*getNumRtoCConverters*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("getRtoCConverterDescriptions", /*getRtoCConverterDescriptions*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("getRtoCConverterStatus", /*getRtoCConverterStatus*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("setToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("removeToCConverterActiveStatus", /*setToCConverterActiveStatus*/ null, 1, 11, 1, PP_FUNCALL, PREC_FN, 0),

      f("lockEnvironment", /*lockEnv*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("environmentIsLocked", /*envIsLocked*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("lockBinding", /*lockBnd*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("unlockBinding", /*lockBnd*/ null, 1, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("bindingIsLocked", /*bndIsLocked*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("makeActiveBinding", /*mkActiveBnd*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("bindingIsActive", /*bndIsActive*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
/* looks like mkUnbound is unused in base R */
      f("mkUnbound", /*mkUnbound*/ null, 0, 111, 1, PP_FUNCALL, PREC_FN, 0),
      f("isNamespaceEnv", /*isNSEnv*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("registerNamespace", /*regNS*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("unregisterNamespace", /*unregNS*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("getRegisteredNamespace", /*getRegNS*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("getNamespaceRegistry", /*getNSRegistry*/ null, 0, 11, 0, PP_FUNCALL, PREC_FN, 0),
      f("importIntoEnv", /*importIntoEnv*/ null, 0, 11, 4, PP_FUNCALL, PREC_FN, 0),
      f("env.profile", /*envprofile*/ null, 0, 211, 1, PP_FUNCALL, PREC_FN, 0),

      f("write.table", /*writetable*/ null, 0, 111, 11, PP_FUNCALL, PREC_FN, 0),
      f("Encoding", /*encoding*/ null, 0, 11, 1, PP_FUNCALL, PREC_FN, 0),
      f("setEncoding", /*setencoding*/ null, 0, 11, 2, PP_FUNCALL, PREC_FN, 0),
      f("lazyLoadDBfetch", Connections.class, 0, 1, 4, PP_FUNCALL, PREC_FN, 0),
      f("setTimeLimit", /*setTimeLimit*/ null, 0, 111, 3, PP_FUNCALL, PREC_FN, 0),
      f("setSessionTimeLimit", /*setSessionTimeLimit*/ null, 0, 111, 2, PP_FUNCALL, PREC_FN, 0),
      f("icuSetCollate", /*ICUset*/ null, 0, 111, -1, PP_FUNCALL, PREC_FN, 0)

  };
}
