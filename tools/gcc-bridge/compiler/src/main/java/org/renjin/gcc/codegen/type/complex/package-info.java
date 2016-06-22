/**
 * Code generation for complex numbers, with either double or single precision.
 * 
 * <p>Fortran, and now C99, provide intrinsic complex types. Supporting complex numbers
 * as value types requires different strategies depending on the context. For local
 * variables, for example, we just use two local variables for the real and imaginary parts.
 * When returning a complex value however, we have no choice but to allocate an array to store the two
 * components and then return the reference to the array.</p>
 */
package org.renjin.gcc.codegen.type.complex;