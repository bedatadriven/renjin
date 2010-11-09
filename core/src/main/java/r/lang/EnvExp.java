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

package r.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import r.lang.exception.EvalException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static r.util.CDefines.*;

/**
 * The Environment data type.
 *
 * <p>
 * Environments can be thought of as consisting of two things.
 * A frame, consisting of a set of symbol-value pairs, and an enclosure,
 * a pointer to an enclosing environment.
 *
 * <p>
 * When R looks up the value for a symbol the frame is examined and if a
 * matching symbol is found its value will be returned. If not, the enclosing environment
 *  is then accessed and the process repeated.
 * Environments form a tree structure in which the enclosures play the role of parents.
 *  The tree of environments is rooted in an empty environment,
 * available through emptyenv(), which has no parent.
 * It is the direct parent of the environment of the base package
 * (available through the baseenv() function). Formerly baseenv() 
 * had the special value NULL, but as from version 2.4.0, the
 *  use of NULL as an environment is defunct.
 *
 */
public class EnvExp extends SEXP implements RecursiveExp {

  public static final int TYPE_CODE = 4;
  public static final String TYPE_NAME = "environment";

  private GlobalContext globalContext;
  private EnvExp enclosing;
  private Map<String, SEXP> frame = new HashMap<String, SEXP>();



  public EnvExp(EnvExp enclosing) {
    Preconditions.checkNotNull(enclosing);

    this.enclosing = enclosing;
    this.globalContext = enclosing.getGlobalContext();
  }

  protected EnvExp(GlobalContext globalContext) {
    this.globalContext = globalContext;
    this.enclosing = null;
  }

  public EnvExp getParent() {
    return enclosing;
  }

  public GlobalContext getGlobalContext() {
    return globalContext;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Collection<String> getSymbolNames() {
    return frame.keySet();
  }

  public void setVariable(SymbolExp symbol, SEXP value) {
    frame.put(symbol.getPrintName(), value);
  }

  public SEXP findFun(SEXP sexp) {
    return null;
  }

  public SEXP findVariable(SymbolExp symbol) {
    if (frame.containsKey(symbol.getPrintName())) {
      return frame.get(symbol.getPrintName());
    } else if (enclosing != null) {
      return enclosing.findVariable(symbol);
    } else {
      return R_UnboundValue;
    }
  }

  public boolean isEmpty() {
    return false;
  }

  private SEXP findVarInFrame3(SEXP symbol, boolean doGet) {
//    int hashcode;
//    SEXP frame, c;
//
//    SEXP rho = this;
//    if (rho == R_BaseNamespace || rho == R_BaseEnv)
//      return SYMBOL_BINDING_VALUE(symbol);
//
//    if (isEmpty())
//      return SymbolExp.UNBOUND_VALUE;
//
//    if(IS_USER_DATABASE(rho)) {
//      /* Use the objects function pointer for this symbol. */
//      R_ObjectTable *table;
//      SEXP val = R_UnboundValue;
//      table = (R_ObjectTable *) R_ExternalPtrAddr(HASHTAB(rho));
//      if(table->active) {
//        if(doGet)
//          val = table->get(CHAR(PRINTNAME(symbol)), NULL, table);
//        else {
//          if(table->exists(CHAR(PRINTNAME(symbol)), NULL, table))
//            val = table->get(CHAR(PRINTNAME(symbol)), NULL, table);
//          else
//            val = R_UnboundValue;
//        }
//      }
//      return(val);
//    } else if (HASHTAB(rho) == R_NilValue) {
//      frame = FRAME(rho);
//      while (frame != R_NilValue) {
//        if (TAG(frame) == symbol)
//          return BINDING_VALUE(frame);
//        frame = CDR(frame);
//      }
//    }
//    else {
//      c = PRINTNAME(symbol);
//      if( !HASHASH(c) ) {
//        SET_HASHVALUE(c, R_Newhashpjw(CHAR(c)));
//        SET_HASHASH(c, 1);
//      }
//      hashcode = HASHVALUE(c) % HASHSIZE(HASHTAB(rho));
//      /* Will return 'R_UnboundValue' if not found */
//      return(R_HashGet(hashcode, symbol, HASHTAB(rho)));
//    }
    return R_UnboundValue;
  }

  /* use the same bits (15 and 14) in symbols and bindings */
  private SEXP BINDING_VALUE(SEXP b) {
    return ((IS_ACTIVE_BINDING(b) ? getActiveValue(CAR(b)) : CAR(b)));
  }

  private SEXP SYMBOL_BINDING_VALUE(SEXP s) {
    return ((IS_ACTIVE_BINDING(s) ? getActiveValue(SYMVALUE(s)) : SYMVALUE(s)));
  }

  private boolean SYMBOL_HAS_BINDING(SEXP s) {
    return (IS_ACTIVE_BINDING(s) || (SYMVALUE(s) != R_UnboundValue));
  }

  private boolean IS_ACTIVE_BINDING(SEXP s) {
    return s.isActiveBinding();
  }


  private void setActiveValue(SEXP fun, SEXP val) {
//    SEXP arg = LCONS(R_QuoteSymbol, LCONS(val, R_NilValue));
//    SEXP expr = LCONS(fun, LCONS(arg, R_NilValue));
//    PROTECT(expr);
//    expr.evaluate(R_GlobalEnv);
//    UNPROTECT(1);
  }

  private SEXP getActiveValue(SEXP fun) {
//    SEXP expr = LCONS(fun, R_NilValue);
//    PROTECT(expr);
//    expr = evaluate(expr, R_GlobalEnv);
//    UNPROTECT(1);
//    return expr;
    return null;
  }

  private static boolean BINDING_IS_LOCKED(SEXP s) {
    return s.isBindingLocked();
  }

  private void SET_BINDING_VALUE(SEXP b, SEXP val) {
    SEXP __b__ = (b);
    SEXP __val__ = (val);
    if (BINDING_IS_LOCKED(__b__))
      throw new EvalException("cannot change value of locked binding for '%s'", CHAR(PRINTNAME(TAG(__b__))));
    if (IS_ACTIVE_BINDING(__b__))
      setActiveValue(CAR(__b__), __val__);
    else
      SETCAR(__b__, __val__);
  }


  private void SET_SYMBOL_BINDING_VALUE(SEXP sym, SEXP val) {
    SEXP __sym__ = (sym);
    SEXP __val__ = (val);
    if (BINDING_IS_LOCKED(__sym__))
      throw new EvalException( "cannot change value of locked binding for '%s'", CHAR(PRINTNAME(__sym__)));
    if (IS_ACTIVE_BINDING(__sym__))
      setActiveValue(SYMVALUE(__sym__), __val__);
    else
      SET_SYMVALUE(__sym__, __val__);
  }

  public SEXP ddfindVar(SymbolExp symbolExp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public Iterable<EnvExp> selfAndParents() {
    return new Iterable<EnvExp>() {
      @Override
      public Iterator<EnvExp> iterator() {
        return new EnvIterator(EnvExp.this);
      }
    };
  }

  private static class EnvIterator extends UnmodifiableIterator<EnvExp> {
    private EnvExp next;

    private EnvIterator(EnvExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public EnvExp next() {
      EnvExp toReturn = next;
      next = next.enclosing;
      return toReturn;
    }
  }
}

