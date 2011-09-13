package r.base.random;

import java.util.Arrays;

public class RNGTAB {

  RNGtype kind;
  N01type nkind;
  String name;
  int n_seed;
  int[] i_seed;

  public RNGTAB(RNGtype kind, N01type nkind, String name, int n_seed, int[] i_seed) {
    this.kind = kind;
    this.nkind = nkind;
    this.name = name;
    this.n_seed = n_seed;
    this.i_seed = Arrays.copyOf(i_seed, i_seed.length);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(name).append(":  ").append(kind).append("-").append(nkind).append("\n");
    for (int i = 0; i < i_seed.length; i++) {
      buf.append(i_seed[i]);
    }
    return (buf.toString());
  }
}
