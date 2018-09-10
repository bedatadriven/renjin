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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.Context.Type;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Functions that provide access to the call Context stack.
 */
public class Contexts {

  private Contexts() {}

  /**
   * Returns the index of the current frame.
   *
   * .GlobalEnv is given number 0 in the list of frames.
   * Each subsequent function evaluation increases the frame stack by 1.
   *
   * @param context the current {@code Context}
   * @return the index of the current frame.
   */
  @Internal("sys.nframe")
  public static int sysFrameCount(@Current Context context) {
    return findCallingContext(context).getFrameDepth();
  }

  /**
   * @param context the current call context
   * @param n generations to ascend the call tree (1=parent, 2=grandparent, etc)
   * @return the Environment of the parent environment
   *
   */
  @Internal("parent.frame")
  public static Environment parentFrame(@Current Context context,  int n) {
    if(n < 1) {
      throw new EvalException("invalid 'n' value");
    }

    // note that we actually climb n+1 parents in order to
    // skip the closure that makes the .Internal(parent.frame()) call

    Context cptr = context;
    Environment t = cptr.getCallingEnvironment();
    while (!cptr.isTopLevel()){
      if (cptr.getType() == Type.FUNCTION ) {
        if (cptr.getEnvironment() == t) {
          if (n == 1) {
            return cptr.getCallingEnvironment();
          }
          n--;
          t = cptr.getCallingEnvironment();
        }
      }
      cptr = cptr.getParent();
    }
    return context.getGlobalEnvironment();
  }



  @Internal("sys.parent")
  public static int sysParent(@Current Context context, int n) {

    Context cptr = findCallingContext(context);

    int i, nframe;
    i = nframe = cptr.getFrameDepth();
    /* This is a pretty awful kludge, but the alternative would be
       a major redesign of everything... -pd */
    while (n-- > 0) {
      i = R_sysparent(nframe - i + 1, cptr);
    }
    return i;

  }

  @Internal("sys.parents")
  public static SEXP sysParents(@Current Context context) {

    Context cptr = findCallingContext(context);

    int nframe = cptr.getFrameDepth();
    int[] rval = new int[nframe];

    for(int i = 0; i < nframe; i++) {
      rval[i] = R_sysparent(nframe - i, cptr);
    }

    return new IntArrayVector(rval);
  }

  /** Find the environment that can be returned by sys.frame
   * (so it needs to be on the cloenv pointer of a context) that matches
   * the environment where the closure arguments are to be evaluated.
   * It would be much simpler if sysparent just returned cptr->sysparent
   * but then we wouldn't be compatible with S.
   **/
  private static int R_sysparent(int n, Context cptr) {
    int j;
    if(n <= 0) {
      throw new EvalException("only positive values of 'n' are allowed");
    }
    while (!cptr.isTopLevel() && n > 1) {
      if (cptr.getType() == Type.FUNCTION ) {
        n--;
      }
      cptr = cptr.getParent();
    }
    /* make sure we're looking at a return context */
    while (!cptr.isTopLevel()  && cptr.getType() != Type.FUNCTION)  {
      cptr = cptr.getParent();
    }

    Environment s = cptr.getCallingEnvironment();

    if(s == cptr.getGlobalEnvironment()) {
      return 0;
    }
    j = 0;
    while (true ) {
      if (cptr.getType() == Type.FUNCTION) {
        j++;
        if( cptr.getEnvironment() == s ) {
          n = j;
        }
      }
      if(cptr.isTopLevel()) {
        break;
      }
      cptr = cptr.getParent();
    }
    n = j - n + 1;
    if (n < 0) {
      n = 0;
    }
    return n;
  }

  @Internal("sys.frames")
  public static SEXP sysFrames(@Current Context context) {
    Context current = findCallingContext(context);
    List<Environment> environments = new ArrayList<>();
    while(!current.isTopLevel()) {
      if(current.getEnvironment() != Environment.EMPTY) {
        environments.add(current.getEnvironment());
      }
      current = current.getParent();
    }

    ListVector.Builder frames = new ListVector.Builder();
    for(int i = environments.size()-1; i >= 0; i--) {
      frames.add(environments.get(i));
    }

    return frames.build();
  }

  @Internal("sys.frame")
  public static Environment sysFrame(@Current Context context, int which) {
    if(which == 0) {
      return context.getGlobalEnvironment();
    }

    return findContext(context, which).getEnvironment();
  }

  @Internal("sys.calls")
  public static PairList sysCalls(@Current Context context) {
    Context current = findCallingContext(context);
    PairList head = Null.INSTANCE;
    while(!current.isTopLevel()) {
      if(current.getCall() != null) {
        head = new PairList.Node(current.getCall(), head);
      }
      current = current.getParent();
    }
    return head;
  }

  @Internal("sys.call")
  public static SEXP sysCall(@Current Context context, int which) {
    FunctionCall functionCall = findContext(context, which).getCall();
    if(functionCall == null){
      return Null.INSTANCE;
    } else {
      return functionCall;
    }
  }

  @Internal("sys.function")
  public static SEXP sysFunction(@Current Context context, int which) {
    return findContext(context, which).getFunction();
  }

  /**
   * Finds the context specified by 'which'.
   *
   * Positive numbers count down from the top-level context, where 0 is the top-level
   * context, 1 is the first function call, 2 the next, etc.
   *
   * Negative numbers count up from the current context. -1 is the parent context,
   * -2, is the parent paren't context, etc.
   *
   * If you have a series of functions, for example:
   *
   * sys.function (which = 0L) .Internal(sys.call(which))
   * f <- function(n) sys.call(n)
   * g <- function(n) f(n)
   * h <- function(n) g(n)
   *
   * Then the call h(-1) from the top-level will result in the following call stack:
   *
   * 0: TOP
   * 1: h(-1)
   * 2: g(-1)
   * 3: f(-1)
   * 4: sys.function(-1)
   *
   * The findCallingContext() routine will skip context #4 and return context #3.
   *
   * From there, count up 1 frame because which is negative, and we return frame #2: g(-1)
   *
   * On the other hand, if which > 1, then we treat it as an absolute frame number and count
   * down from the top-level context. So h(1) would return the call at context frame #1 (`h(1)`) and
   * h(2) would return context frame #2 (`g(2)`)
   *
   * This method treats which = 0 as relative and returns the current context frame, but sys.frame
   * treats it as absolute and handles which = 0 as a special case.
   *
   */
  private static Context findContext(@Current Context callingContext, int which) {

    // Find the context from which sys.function() was invoked, rather than
    // sys.function's closure wrapper's context.

    callingContext = findCallingContext(callingContext);

    // Compute how many contexts we need to climb
    // from the calling context

    int n;
    if (which > 0) {

      // For positive numbers, we count *down* from the top-level
      // context.

      n = callingContext.getFrameDepth() - which;

    } else {

      // For negative numbers, we count *up* from the current context
      n = -which;
    }

    if(n < 0 || n > callingContext.getFrameDepth() + 1) {
      throw new EvalException("not that many frames on the stack");
    }

    Context whichContext = callingContext;
    while(n > 0) {
      whichContext = whichContext.getParent();
      n--;
    }

    return whichContext;
  }


  /**
   * Finds the evaluation context in which {@code sys.parent} or {@code sys.frame}
   * was called.
   */
  public static Context findCallingContext(Context context) {
    Environment callingEnvironment = context.getCallingEnvironment();
    return findCallingContext(context, callingEnvironment);
  }

  public static Context findCallingContext(Context context, Environment callingEnvironment) {
    while (!context.isTopLevel()) {
      if(context.getEnvironment() == callingEnvironment) {
        break;
      }
      context = context.getParent();
    }
    return context;
  }


}
