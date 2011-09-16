/*
 * unif_rand() produces zero
 * I think i did something wrong!!!
 * I plan to remove this, if somebody does not fix it!
 */
package r.base.random;

import r.jvmi.annotations.Primitive;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.exception.EvalException;

public class RNG {

  /*
   * Default RNG and N01 objects.
   */
  public static RNGtype RNG_kind = RNGtype.SUPER_DUPER; //default
  public static N01type N01_kind = N01type.INVERSION; //default
  /*
   * R uses a int vector of length 625
   * for Random seeds.
   */
  static int[] dummy = new int[625];
  static int[] mt = new int[dummy.length - 1];
  
  /*
   * This sould be global variable in R environment.
   */
  static DoubleVector seeds; 
  
  static double d2_32 = 4294967296.;/* = (double) */

  static double i2_32m1 = 2.328306437080797e-10;/* = 1/(2^32 - 1) */
  

  static double KT = 9.31322574615479e-10; /*= 2^-30 */

  static int MATRIX_A = 0x9908b0df;
  static int N = 624;
  static int M = 397;
  static int mti = RNG.N + 1;
  static int UPPER_MASK = 0x80000000; /* most significant w-r bits */

  static int LOWER_MASK = 0x7fffffff; /* least significant r bits */

  static int TEMPERING_MASK_B = 0x9d2c5680;
  static int TEMPERING_MASK_C = 0xefc60000;
  static RNGTAB[] RNG_Table = new RNGTAB[]{
    new RNGTAB(RNGtype.WICHMANN_HILL, N01type.BUGGY_KINDERMAN_RAMAGE, "Wichmann-Hill", 3, dummy),
    new RNGTAB(RNGtype.MARSAGLIA_MULTICARRY, N01type.BUGGY_KINDERMAN_RAMAGE, "Marsaglia-MultiCarry", 2, dummy),
    new RNGTAB(RNGtype.SUPER_DUPER, N01type.BUGGY_KINDERMAN_RAMAGE, "Super-Duper", 2, dummy),
    new RNGTAB(RNGtype.MERSENNE_TWISTER, N01type.BUGGY_KINDERMAN_RAMAGE, "Mersenne-Twister", 1 + 624, dummy),
    new RNGTAB(RNGtype.KNUTH_TAOCP, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP", 1 + 100, dummy),
    new RNGTAB(RNGtype.USER_UNIF, N01type.BUGGY_KINDERMAN_RAMAGE, "User-supplied", 0, dummy),
    new RNGTAB(RNGtype.KNUTH_TAOCP2, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP-2002", 1 + 100, dummy)};

  @Primitive("RNGkind")
  public static IntVector RNGkind(int kind, int normalkind) {
    /*
     * This method requires more than those codes. I will implement them as soon as possible.
     */
    try {
      RNG.RNG_kind = RNGtype.values()[kind];
    } catch (Exception e) {
      throw new EvalException("RNGkind: unimplemented RNG kind " + kind);
    }

    try {
      RNG.N01_kind = N01type.values()[normalkind];
    } catch (Exception e) {
      throw new EvalException("invalid Normal type in RNGkind");
    }
    
    RNG.RNG_kind = RNGtype.values()[kind];
    RNG.N01_kind = N01type.values()[normalkind];
    System.out.println("Random generator is set to "+RNG.RNG_kind+" and "+RNG.N01_kind);
    return (new IntVector(RNG.RNG_kind.ordinal(), RNG.N01_kind.ordinal()));
  }

  static double fixup(double x) {
    if (x <= 0.0) {
      return 0.5 * i2_32m1;
    }
    if ((1.0 - x) <= 0.0) {
      return 1.0 - 0.5 * i2_32m1;
    }
    return x;
     
  }
  
  public static int hack32bit(int num){
    return(num);
  }

  public static double unif_rand() {
    double value;

    switch (RNG_kind) {

      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        /* This is Reeds et al (1984) implementation;
         * modified using __unsigned__  seeds instead of signed ones
         */
        RNG_Table[RNG_kind.ordinal()].i_seed[0] ^= (((RNG_Table[RNG_kind.ordinal()].i_seed[0]) >> 15) & 0377777);
        RNG_Table[RNG_kind.ordinal()].i_seed[0] ^= (int)(RNG_Table[RNG_kind.ordinal()].i_seed[0] << 17);
        RNG_Table[RNG_kind.ordinal()].i_seed[1] = (int)(RNG_Table[RNG_kind.ordinal()].i_seed[1] * 69069);
        /* in [0,1) */
       return fixup( ((RNG_Table[RNG_kind.ordinal()].i_seed[0] ^ RNG_Table[RNG_kind.ordinal()].i_seed[1])) * i2_32m1); /* in [0,1) */
        

      case MERSENNE_TWISTER:
        return fixup(MT_genrand());

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }

  static void FixupSeeds(RNGtype RNG_kind, boolean initial) {
    /*
     * #define I1 (RNG_Table[RNG_kind].i_seed[0])
     * #define I2 (RNG_Table[RNG_kind].i_seed[1])
     * #define I3 (RNG_Table[RNG_kind].i_seed[2])
     */

    int j, notallzero = 0;

    switch (RNG_kind) {
      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");
      case SUPER_DUPER:
        if (RNG_Table[RNG_kind.ordinal()].i_seed[0] == 0) {
          RNG_Table[RNG_kind.ordinal()].i_seed[0] = 1;
        }
        /* I2 = Congruential: must be ODD */
        RNG_Table[RNG_kind.ordinal()].i_seed[1] |= 1;
        break;

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (initial) {
          RNG_Table[RNG_kind.ordinal()].i_seed[0] = 624;
        }
        /* No action unless user has corrupted .Random.seed */
        if (RNG_Table[RNG_kind.ordinal()].i_seed[0] <= 0) {
          RNG_Table[RNG_kind.ordinal()].i_seed[0] = 624;
        }
        /* check for all zeroes */
        for (j = 1; j <= 624; j++) {
          if (RNG_Table[RNG_kind.ordinal()].i_seed[j] != 0) {
            notallzero = 1;
            break;
          }
        }
        if (notallzero == 0) {
          Randomize(RNG_kind);
        }
        break;

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        break;
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }

  /*
   * not fully implemented. would be buggy
   */
  static void GetRNGstate() {
    if (seeds == null) {
      Randomize(RNG_kind);
    }else {
      for (int j = 1; j <= RNG_Table[RNG_kind.ordinal()].n_seed; j++) {
        RNG_Table[RNG_kind.ordinal()].i_seed[j - 1] = seeds.getElementAsInt(j);
      }
      FixupSeeds(RNG_kind, false);
    }
  }

  
  
  static void PutRNGstate(){
    int len_seed;
    
     len_seed = RNG_Table[RNG_kind.ordinal()].n_seed;

    //seeds = allocVector(INTSXP, len_seed + 1));
     //INTEGER(seeds)[0] = RNG_kind + 100 * N01_kind;
    DoubleVector.Builder ib = new DoubleVector.Builder();
    ib.add(RNG_kind.ordinal() + 100 * N01_kind.ordinal()); 
    
    for(int j = 0; j < len_seed; j++){
        //INTEGER(seeds)[j+1] = RNG_Table[RNG_kind].i_seed[j];
      ib.add(RNG_Table[RNG_kind.ordinal()].i_seed[j]);
    }

    /* assign only in the workspace */
    //defineVar(R_SeedsSymbol, seeds, R_GlobalEnv);
    RNG.seeds = ib.build();
  }

  /*
   * This part of R is platform dependent. this formula is random itself :)
   */
  static void Randomize(RNGtype kind) {
    int sseed;
    sseed = (int)(new java.util.Date()).getTime();
    RNG_Init(kind, sseed);
  }

  static void RNG_Init(RNGtype kind, int seed) {
    System.out.println("RNG_Iinit");
    int j;
    int sseed = seed;
    /* Initial scrambling */
    for (j = 0; j < 50; j++) {
      sseed = (69069 * sseed + 1);
    }
    switch (kind) {
      case WICHMANN_HILL:
      case MARSAGLIA_MULTICARRY:
      case SUPER_DUPER:
      case MERSENNE_TWISTER:
        /* i_seed[0] is mti, *but* this is needed for historical consistency */
        for (j = 0; j < RNG_Table[kind.ordinal()].n_seed; j++) {
          seed = (69069 * sseed + 1);
          RNG_Table[kind.ordinal()].i_seed[j] = sseed;
        }
        FixupSeeds(kind, true);
        break;
      case KNUTH_TAOCP:
        throw new EvalException(RNG_kind + " not implemented yet");
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }


  /*
   * Methods for MERSENNE_TWISTER
   */
  static int TEMPERING_SHIFT_U(int y) {
    return (y >> 11);
  }

  static int TEMPERING_SHIFT_S(int y) {
    return (y << 7);
  }

  static int TEMPERING_SHIFT_T(int y) {
    return (y << 15);
  }

  static int TEMPERING_SHIFT_L(int y) {
    return (y >> 18);
  }

  static double MT_genrand() {
    int y = 0;
    int[] mag01 = new int[]{0x0, RNG.MATRIX_A};
    for (int i = 0; i < mt.length; i++) {
      mt[i] = (int) dummy[i + 1];
    }


    RNG.mti = (int) dummy[0];

    if (RNG.mti >= RNG.N-1) {
      int kk;
      if (RNG.mti == RNG.N + 1) {
        MT_sgenrand(4357);
      }

      for (kk = 1; kk < RNG.N - RNG.M; kk++) {
        y = (mt[kk] & RNG.UPPER_MASK) | (mt[kk + 1] & RNG.LOWER_MASK);
        mt[kk] = mt[1 + kk + RNG.M] ^ (y >> 1) ^ mag01[y & 0x1];
      }
      for (; kk < RNG.N - 1; kk++) {
        y = (mt[kk] & RNG.UPPER_MASK) | (mt[kk + 1] & RNG.LOWER_MASK);
        mt[kk] = mt[kk + (RNG.M - RNG.N)] ^ (y >> 1) ^ mag01[y & 0x1];
      }
      y = (mt[RNG.N - 1] & RNG.UPPER_MASK) | (mt[0] & RNG.LOWER_MASK);
      mt[RNG.N - 1] = mt[RNG.M - 1] ^ (y >> 1) ^ mag01[y & 0x1];

      RNG.mti = 0;
    }
    y = mt[RNG.mti++];
    y ^= TEMPERING_SHIFT_U(y);
    y ^= TEMPERING_SHIFT_S(y) & TEMPERING_MASK_B;
    y ^= TEMPERING_SHIFT_T(y) & TEMPERING_MASK_C;
    y ^= TEMPERING_SHIFT_L(y);
    RNG.dummy[0] = mti;
    //return ((double) y * 2.3283064365386963e-10); /* reals: [0,1)-interval */
    return ((double) y * i2_32m1); /* reals: [0,1)-interval */
  }

  static void MT_sgenrand(int seed) {
    int i;
    javax.swing.JOptionPane.showMessageDialog(null, "sgenrand");
    for (i = 0; i < N; i++) {
      mt[i] = seed & 0xffff0000;
      seed = 69069 * seed + 1;
      mt[i] |= (seed & 0xffff0000) >> 16;
      seed = 69069 * seed + 1;
    }
    RNG.mti = RNG.N;
  }

  public static void dumpRNGTAB(RNGTAB[] tab) {
    for (int i = 0; i < tab.length; i++) {
      System.out.println(tab[i].toString());
    }
  }
}
