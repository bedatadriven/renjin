package org.renjin.invocation;

/* Some methods with different parameters that we can call from R to make sure invocation works */
public class StringParams {

   public String concatCharSeq(CharSequence one, String two) {
      return one + two;
   }

   public String concatString(String one, String two) {
      return one + two;
   }
}
