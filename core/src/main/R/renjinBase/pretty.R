#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#

# ORIGINAL HEADER:

#  File src/library/base/R/pretty.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

pretty <- function(x, ...) UseMethod("pretty")

pretty.default <-
  function(x,
           n = 5,
           min.n = n %/% 3,
           shrink.sml = 0.75,
           high.u.bias = 1.5,
           u5.bias = .5 + 1.5 * high.u.bias,
           eps.correct = 0,
           ...)
  {
    x <- x[is.finite(x <- as.numeric(x))]
    if (length(x) == 0L)
      return(x)
    if (is.na(n <- as.integer(n[1L])) || n < 0L)
      # n=0 !!
      stop("invalid 'n' value")
    if (!is.numeric(shrink.sml) || shrink.sml <= 0)
      stop("'shrink.sml' must be numeric > 0")
    if ((min.n <- as.integer(min.n)) < 0 || min.n > n)
      stop("'min.n' must be non-negative integer <= n")
    if (!is.numeric(high.u.bias) || high.u.bias < 0)
      stop("'high.u.bias' must be non-negative numeric")
    if (!is.numeric(u5.bias) || u5.bias < 0)
      stop("'u5.bias' must be non-negative numeric")
    if ((eps.correct <-
         as.integer(eps.correct)) < 0L || eps.correct > 2L)
      stop("'eps.correct' must be 0, 1, or 2")
    
    lo <- as.double(min(x))
    up <- as.double(max(x))
    ndiv <- n
    shrink.sml <- as.double(shrink.sml)
    
    # From version 0.65 on, we had rounding_eps := 1e-5, before, r..eps = 0
    # 1e-7 is consistent with seq.default()
    
    rounding_eps <- 1e-7
    
    dx <- up - lo
    
    # cell := "scale"
    if (dx == 0 && up == 0) {
      # up == lo == 0
      cell = 1
      i_small = TRUE
    } else {
      cell = max(abs(lo), abs(up))
      
      # U = upper bound on cell/unit
      U <- if (1 + (u5.bias >= 1.5 * high.u.bias + .5)) {
        1 / (1 + high.u.bias)
      } else {
        1.5 / (1 + u5.bias)
      }
      # added times 3, as several calculations here
      i_small <- dx < cell * U * max(1,  ndiv) * .Machine$double.eps * 3
    }
    
    # OLD: cell = FLT_EPSILON+ dx / *ndiv; FLT_EPSILON = 1.192e-07 */
    if (i_small) {
      if (cell > 10) {
        cell <- 9 + cell / 10
      }
      cell <- cell * shrink.sml
      
      if (min.n > 1) {
        cell <- cell / min.n
      }
      
    } else {
      cell = dx
      
      if (ndiv > 1) {
        cell <- cell / ndiv
      }
    }
    
    if (cell < 20 * .Machine$double.xmin) {
      warning("pretty.default(): very small range.. corrected")
      cell <- 20 * .Machine$double.xmin
      
    } else if (cell * 10 > .Machine$double.xmax) {
      warning("pretty.default(): very large range.. corrected")
      cell <- .1 * .Machine$double.xmax
    }
    
    # NB: the power can be negative and this relies on exact
    # calculation, which glibc's exp10 does not achieve
    
    base = 10.0 ^ floor(log10(cell))  # base <= cell < 10*base
    
    # unit : from { 1,2,5,10 } * base
    #	 such that |u - cell| is small,
    #  favoring larger (if high.u.bias > 1, else smaller)  u  values;
    # favor '5' more than '2'  if u5.bias > high.u.bias  (default u5.bias = .5 + 1.5 high.u.bias) */
    unit <- base
    if ((U <- 2 * base) - cell <  high.u.bias * (cell - unit)) {
      unit = U
      if ((U <- 5 * base) - cell < u5.bias * (cell - unit)) {
        unit = U
        if ((U <- 10 * base) - cell <  high.u.bias * (cell - unit)) {
          unit = U
        }
      }
    }
    # Result: c := cell,  u := unit,  b := base
    #	c in [	1,	      (2+ h) /(1+h) ]     b ==> u=  b
    #	c in ( (2+ h)/(1+h),  (5+2h5)/(1+h5)] b ==> u= 2b
    #	c in ( (5+2h)/(1+h), (10+5h) /(1+h) ] b ==> u= 5b
    #	c in ((10+5h)/(1+h),	         10 ) b ==> u=10b
    #
    #	===>	2/5 *(2+h)/(1+h)  <=  c/u  <=  (2+h)/(1+h)	
    
    ns <- floor(lo / unit + rounding_eps)
    nu <- ceiling(up / unit - rounding_eps)
    
    if (eps.correct && (eps.correct > 1 || !i_small)) {
      if (lo != 0.) {
        lo = lo * (1 - .Machine$double.eps)
      } else {
        lo = .Machine$double.xmin
      }
      if (up != 0.) {
        up <-  up * (1 + .Machine$double.eps)
      } else {
        up <- +.Machine$double.xmin
      }
    }
    
    while (ns * unit > lo + rounding_eps * unit) {
      ns <- ns - 1
    }
    while (nu * unit < up - rounding_eps * unit) {
      nu <- nu + 1
    }
    
    k <- as.integer(0.5 + nu - ns)
    
    if (k < min.n) {
      # ensure that	nu - ns	 == min.n
      k <- min.n - k
      
      if (ns >= 0.) {
        nu <- nu + k / 2
        ns <-
          ns - (k / 2 + k %% 2) # ==> nu-ns = old(nu-ns) + min.n -k = min.n
      } else {
        ns <- ns - k / 2
        nu <- nu + k / 2 + k %% 2
      }
      ndiv <- min.n
      
    } else {
      ndiv = k
    }
  
    lo = ns * unit
    up = nu * unit

    s <- as.double(seq.int(lo, up, length.out = ndiv + 1))
    if (!eps.correct && ndiv) {
      # maybe zap smalls from seq() rounding errors
      ## better than zapsmall(s, digits = 14) :
      delta <- diff(range(lo, up)) / ndiv
      if (any(small <- abs(s) < 1e-14 * delta))
        s[small] <- 0
    }
    s
  }
