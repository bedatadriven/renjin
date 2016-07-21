
# Math Modules

This directory contains a number of pure-math libraries, primarily imported from elsewhere.

They include:

  * [nmath](nmath/) A C library of probability distributions and special functions, imported from GNU R and
    compiled with GCC-Bridge to Java classes.
    
  * [appl](appl) A Fortran and C library consistening of miscellaneous math functions, also imported from 
     GNU R.
  
  * [blas](blas) The Basic Linear Algebra Subprograms (BLAS) Fortran reference library.
  

These libraries have no special dependencies on R or Renjin and can be used independently. They have been
updated where necessary to be thread safe.