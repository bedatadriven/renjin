package org.renjin.eval;

import org.renjin.sexp.Environment;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import java.util.Arrays;

public class ArgList {
  public String[] names;
  public SEXP[] values;

  public ArgList(String[] names, SEXP[] values) {
    this.names = names;
    this.values = values;
  }

  public int size() {
    return values.length;
  }


  private void insert(int forwardedIndex, int numForwardedArgs) {
    int numFixedArgs = values.length;
    int numArgs = numFixedArgs + numForwardedArgs;
    String[] expandedNames = Arrays.copyOf(names, numArgs);
    SEXP[] expandedValues = Arrays.copyOf(values, numArgs);

    // Fixed Args:
    // 0  1  2  3  4
    // A  B  C  D  E

    //    ^---- forwardedIndex: where to insert the ... arguments X, Y, Z

    // 0  1  2  3  4  5  6  7
    // A  X  Y  Z  B  C  D  E
    //             ~~~~~~~~~~
    //             These arguments need to be moved over.
    //             From forwardedIndex -> (forwardedIndex + numForwardedArgs)

    int moveStart = forwardedIndex;
    int moveCount = numFixedArgs - forwardedIndex;
    if(moveCount > 0) {
      System.arraycopy(expandedNames, forwardedIndex, names, moveStart, moveCount);
      System.arraycopy(expandedValues, forwardedIndex, values, moveStart, moveCount);
    }
    this.names = expandedNames;
    this.values = expandedValues;
  }

  public static ArgList of(String[] names, SEXP[] values) {
    return new ArgList(names, values);
  }

  public static ArgList of() {
    return new ArgList(new String[0], new SEXP[0]);
  }

  public static ArgList of(
      String name0, SEXP arg0) {

    return new ArgList(
        new String[]{name0},
        new SEXP[]{arg0});
  }


  public static ArgList of(
      String name0, SEXP arg0,
      String name1, SEXP arg1) {

    return new ArgList(
        new String[]{name0, name1},
        new SEXP[]{arg0, arg1});
  }

  public static ArgList of(
      String name0, SEXP arg0,
      String name1, SEXP arg1,
      String name2, SEXP arg2) {

    return new ArgList(
        new String[]{name0, name1, name2},
        new SEXP[]{arg0, arg1, arg2});
  }

  public static ArgList of(
      String name0, SEXP arg0,
      String name1, SEXP arg1,
      String name2, SEXP arg2,
      String name3, SEXP arg3) {

    return new ArgList(
        new String[]{name0, name1, name2, name3},
        new SEXP[]{arg0, arg1, arg2, arg3});
  }

  public static ArgList of(
      String name0, SEXP arg0,
      String name1, SEXP arg1,
      String name2, SEXP arg2,
      String name3, SEXP arg3,
      String name4, SEXP arg4) {

    return new ArgList(
        new String[]{name0, name1, name2, name3, name4 },
        new SEXP[]{arg0, arg1, arg2, arg3, arg4 });
  }


  public static ArgList forceExpand(Context context, Environment rho) {
    PairList expando = (PairList) rho.getEllipsesVariable();
    int numArgs = expando.length();
    ArgList argList = new ArgList(new String[numArgs], new SEXP[numArgs]);

    forceExpandInto(context, argList, 0, expando);

    return argList;
  }

  /**
   * Expandes, and forces, forwarded arguments into the provided {@link ArgList}.
   *
   * <p><strong>The provided {@link ArgList} will be mutated iff the number of forwarded arguments is
   * greater than zero</strong></p>
   */
  public static ArgList forceExpand(Context context, Environment rho, ArgList argList, int forwardedIndex) {

    PairList expando = (PairList) rho.getEllipsesVariable();
    int numForwardedArgs = expando.length();

    if(numForwardedArgs == 0) {
      return argList;
    }

    argList.insert(forwardedIndex, numForwardedArgs);

    forceExpandInto(context, argList, forwardedIndex, expando);

    return argList;
  }

  private static void forceExpandInto(Context context, ArgList argList, int forwardedIndex, PairList expando) {
    int argIndex = forwardedIndex;
    while (expando instanceof PairList.Node) {
      PairList.Node expandNode = (PairList.Node) expando;
      argList.names[argIndex] = expandNode.hasTag() ? expandNode.getName() : null;
      argList.values[argIndex] = expandNode.getValue().force(context);
      expando = expandNode.getNext();
      argIndex++;
    }
  }

  /**
   * Creates a new argument list from one initial argument and forwarded arguments.
   *
   * <p><strong>The provided {@link ArgList} will be mutated iff the number of forwarded arguments is
   * greater than zero</strong></p>
   */
  public static ArgList expand(Environment rho, ArgList argList, int forwardedIndex) {
    PairList expando = (PairList) rho.getEllipsesVariable();
    int numForwardedArgs = expando.length();

    if(numForwardedArgs == 0) {
      return argList;
    }

    argList.insert(forwardedIndex, numForwardedArgs);

    return expandInto(argList, forwardedIndex, expando);
  }

  private static ArgList expandInto(ArgList argList, int forwardedIndex, PairList expando) {
    int argIndex = forwardedIndex;
    while (expando instanceof PairList.Node) {
      PairList.Node expandNode = (PairList.Node) expando;
      argList.names[argIndex] = expandNode.hasTag() ? expandNode.getName() : null;
      argList.values[argIndex] = expandNode.getValue();
      expando = expandNode.getNext();
      argIndex++;
    }
    return argList;
  }

}
