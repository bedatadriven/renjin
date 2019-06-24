package org.renjin.eval;

import org.renjin.sexp.Environment;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

public class ArgList {
  public final String[] names;
  public final SEXP[] values;

  public ArgList(String[] names, SEXP[] values) {
    this.names = names;
    this.values = values;
  }

  public int size() {
    return values.length;
  }

  public static ArgList forceExpand0(Context context, Environment rho) {
    PairList expando = (PairList) rho.getEllipsesVariable();
    int numArgs = expando.length();
    String[] names = new String[numArgs];
    SEXP[] values = new SEXP[numArgs];

    return expandInto(context, expando, names, values, 0);
  }

  /**
   * Creates a new argument list from one initial argument and forwarded arguments.
   */
  public static ArgList forceExpand1(Context context, Environment rho,
                                     String name0, SEXP arg0) {

    PairList expando = (PairList) rho.getEllipsesVariable();
    int numArgs = expando.length() + 1;
    String[] names = new String[numArgs];
    SEXP[] values = new SEXP[numArgs];

    names[0] = name0;
    values[0] = arg0;

    return expandInto(context, expando, names, values, 1);
  }

  public static ArgList of(String[] names, SEXP[] values) {
    return new ArgList(names, values);
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

  private static ArgList expandInto(Context context, PairList expando, String[] names, SEXP[] values, int argIndex) {
    while (expando instanceof PairList.Node) {
      PairList.Node expandNode = (PairList.Node) expando;
      names[argIndex] = expandNode.hasTag() ? null : expandNode.getName();
      values[argIndex] = expandNode.getValue().force(context);
      expando = expandNode.getNext();
    }

    return new ArgList(names, values);
  }
}
