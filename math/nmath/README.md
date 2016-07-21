
# NMath

`nmath` is a collection of well-documented mathematical functions related to probability distributions as 
well as other "special" mathematical functions like the Gamma functions, Beta functions, and the 
Bessel family of functions. The `nmath` library is an integral part of GNU R and its C sources are copied 
here more or less verbatim.

The C sources have been updated to be thread safe. In particular, all the random number generation functions
have been updated to accept a `unif_rand` function pointer as their first argument to avoid relying on the 
global state of a random number generator.




