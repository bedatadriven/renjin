package org.renjin.gcc.runtime;

public class Builtins {

	public static double powi(double base, int exponent) {
		if(exponent == 1) {
			return base;
		} else if(exponent == 2) {
			return base * base;
		} else {
			return Math.pow(base, (double)exponent);
		}
	}
	
	public static int _gfortran_pow_i4_i4(int base, int exponent) {
		 if(exponent < 0) {
			 throw new IllegalArgumentException("exponent must be > 0: " + exponent);
		 }
		 int result = 1;
		 for(int i=0;i<exponent;++i) {
			 result *= base;
		 }
		 return result;
	}
	
}
