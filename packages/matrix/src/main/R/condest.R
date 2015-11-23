#### This is a "translation" of GNU octave's
####      ~/src/octave-3.2.4/scripts/linear-algebra/condest.m
#### and  ~/src/octave-3.2.4/scripts/linear-algebra/onenormest.m
####      which have identical copyright and references (see below):
####
##__\begin{copyright clause}______________________________________________
## Copyright (C) 2007, 2008, 2009 Regents of the University of California
##
## This file is part of Octave.
##
## Octave is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 3 of the License, or (at
## your option) any later version.
##
## Octave is distributed in the hope that it will be useful, but
## WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with Octave; see the file COPYING.  If not, see
## <http://www.gnu.org/licenses/>.

## Code originally licensed under
##
##  Copyright (c) 2007, Regents of the University of California
##  All rights reserved.
##
##  Redistribution and use in source and binary forms, with or without
##  modification, are permitted provided that the following conditions
##  are met:
##
##     * Redistributions of source code must retain the above copyright
##       notice, this list of conditions and the following disclaimer.
##
##     * Redistributions in binary form must reproduce the above
##       copyright notice, this list of conditions and the following
##       disclaimer in the documentation and/or other materials provided
##       with the distribution.
##
##     * Neither the name of the University of California, Berkeley nor
##       the names of its contributors may be used to endorse or promote
##       products derived from this software without specific prior
##       written permission.
##
##  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS''
##  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
##  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
##  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND
##  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
##  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
##  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
##  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
##  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
##  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
##  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
##  SUCH DAMAGE.

## Author: Jason Riedy <ejr@cs.berkeley.edu>
## Keywords: linear-algebra norm estimation
## Version: 0.2

##__\end{copyright clause}________________________________________________

condest <- function(A, t = min (n, 5), normA = norm(A, "1"),
                    silent = FALSE, quiet = TRUE)
{

    ## Octave has further optional args and "calling sequences"
    ## may be implement at a later time point
  ##
  if(length(d <- dim(A)) != 2 || (n <- d[1]) != d[2])
      stop("'A' must be a square matrix")

  luA <- lu(A)
  i.n <- seq_len(n)
  isSparse <- is(A, "sparseMatrix")
  if(isSparse) {
### FIXME: if A is not a Matrix, but already a "CHMfactor" as resulting from
###        Cholesky() , then we can procede more efficiently , notably
###        because of the  solve(A, b, system = ".*")  options !

      ## luA = "sparseLU": slots (L, U, p,q, Dim);
      ## expand(luA) == list(P, L, U, Q)  <---->  A = P' L U Q
      ##  where  P A == A[p +1,]  and  A Q' == A[, q +1]
      ## <==> A^(-1) x = Q' U^-1 L^-1 P x = Q'y
      ## and  A^(-T) x =(Q' U^-1 L^-1 P)' x = P' L^-T U^-T Q x = P'z
      q. <- q.i <- luA@q + 1L; q.i[q.i] <- i.n
      p. <- p.i <- luA@p + 1L; p.i[p.i] <- i.n
      ## q.i := inv(q.) &  p.i := inv(p.), the inverse permutations
      Ut <- t(luA@U)
      Lt <- t(luA@L)
      f.solve   <- function(x) solve(luA@U, solve(luA@L, x[p.,]))[q.i,]
      f.solve_t <- function(x) solve(Lt,    solve(Ut,    x[q.,]))[p.i,]

      ##Oct     [L, U, P, Pc] = lu (A);
      ##Oct     solve   = @(x) Pc' * (U \ (L \ (P * x)));
      ##Oct     solve_t = @(x) P'  * (L' \ (U' \ (Pc * x)));

  } else {
      ## luA is  "denseLU" :
      e.A <- expand(luA) ##  == list(L, U, P),  where  A = PLU
      p. <- p.i <- luA@perm; p.i[p.i] <- i.n
      ## p.i := inv(p.), the inverse permutation
      Ut <- t(e.A$U)
      Lt <- t(e.A$L)
      ##  A = PLU  <--> A^{-1} x = U^-1 L^-1 P x
      ##                A^{-T} x = (U^-1 L^-1 P)' x = P' L^-T U^-T x = P'z
      f.solve   <- function(x) solve(e.A$U, solve(e.A$L, x[p.,]))
      f.solve_t <- function(x) solve(Lt, solve(Ut, x))[p.i,]

      ##Oct     [L, U, P] = lu (A);
      ##Oct     solve = @(x) U \ (L \ (P*x));
      ##Oct     solve_t = @(x) P' * (L' \ (U' \ x));
  }

  n1.res <- ## onenormest (A^{-1}, t=t) -- of course,that's *NOT* what we want
      onenormest (A.x = f.solve, At.x = f.solve_t,
                  t=t, n=n, quiet=quiet, silent=silent)
  ## [Ainv_norm, v, w] = onenormest (solve, solve_t, n, t);
  w <- n1.res[["w"]]

  list(est = normA * n1.res[["est"]],
       v = w / sum(abs(w))) # sum(|w|) = norm(w, "1")
}


## %!demo
## %!  N = 100;
## %!  A = randn (N) + eye (N);
## %!  condest (A)
## %!  [L,U,P] = lu (A);
## %!  condest (A, @(x) U\ (L\ (P*x)), @(x) P'*(L'\ (U'\x)))
## %!  condest (@(x) A*x, @(x) A'*x, @(x) U\ (L\ (P*x)), @(x) P'*(L'\ (U'\x)), N)
## %!  norm (inv (A), 1) * norm (A, 1)

### Yes, these test bounds are really loose.  There's
### enough randomization to trigger odd cases with hilb().

## %!test
## %!  N = 6;
## %!  A = hilb (N);
## %!  cA = condest (A);
## %!  cA_test = norm (inv (A), 1) * norm (A, 1);
## %!  assert (cA, cA_test, -2^-8);

## %!test
## %!  N = 6;
## %!  A = hilb (N);
## %!  solve = @(x) A\x; solve_t = @(x) A'\x;
## %!  cA = condest (A, solve, solve_t);
## %!  cA_test = norm (inv (A), 1) * norm (A, 1);
## %!  assert (cA, cA_test, -2^-8);

## %!test
## %!  N = 6;
## %!  A = hilb (N);
## %!  apply = @(x) A*x; apply_t = @(x) A'*x;
## %!  solve = @(x) A\x; solve_t = @(x) A'\x;
## %!  cA = condest (apply, apply_t, solve, solve_t, N);
## %!  cA_test = norm (inv (A), 1) * norm (A, 1);
## %!  assert (cA, cA_test, -2^-6);

## %!test
## %!  N = 12;
## %!  A = hilb (N);
## %!  [rcondA, v] = condest (A);
## %!  x = A*v;
## %!  assert (norm(x, inf), 0, eps);


##------------ onenormest ------------------------------------------

onenormest <- function(A, t = min(n, 5),
                       A.x, At.x, n,
                       silent = FALSE, quiet = silent,
                       iter.max = 10, eps = 4* .Machine$double.eps)
{
    mi.A <- missing(A)
    mi.A.x  <- missing(A.x)
    mi.At.x <- missing(At.x)
    no.A.x  <- mi.A.x  || !is.function(A.x)
    no.At.x <- mi.At.x || !is.function(At.x)
    if(mi.A && (no.A.x || no.At.x))
        stop("must either specify 'A' or the functions 'A.x' and 'At.x'")
    if(!mi.A && (!mi.A.x || !mi.At.x))
        warning("when 'A' is specified, 'A.x' and 'At.x' are disregarded")
    if(mi.A) {
        stopifnot(is.numeric(n), length(n) == 1, n == round(n), n >= 0)
    }
    else { ## using 'A'
        if(length(d <- dim(A)) != 2 || (n <- d[1]) != d[2])
            stop("'A' must be a square matrix")
        rm(d)
    }
    stopifnot(is.numeric(t), length(t) == 1, t >= 1,
              iter.max >= 1)

    ## Initial test vectors X.
    X <- matrix(runif(n*t), n,t)        #   X = rand (n, t);
    ## scale X  to have column sums == 1 :
    X <- X / rep(colSums(X), each=n)

    ## Track if a vertex has been visited.
    been_there <- logical(n)            # zeros (n, 1);
    I.t <- diag(nrow = t)

    ## To check if the estimate has increased.
    est_old <- 0

    ## Normalized vector of signs.
    S <- matrix(0, n, t)

    for(iter in 1:(iter.max + 1)) {
        Y <- if(mi.A) A.x(X) else A %*% X ## is  n x t

        ## Find the initial estimate as the largest A*x.
        ## [est, imax] = max (sum (abs (Y), 1))
        imax <- which.max(cY <- colSums(abs(Y)))
        est <- cY[imax]
        if (est > est_old || iter == 2)
            w <- Y[, imax]
        if (iter >= 2 && est < est_old) {
            ## No improvement, so stop.
            est <- est_old
            break
        }

        est_old <- est
        S_old <- S
        if (iter > iter.max) {
            ## Gone too far.  Stop.
            if(!silent) warning(gettextf("not converged in %d iterations",
					 iter.max), domain = NA)
            break
        }

        S <- sign (Y) ## n x t

        ## Test if any of S are approximately parallel to previous S
        ## vectors or current S vectors.  If everything is parallel,
        ## stop. Otherwise, replace any parallel vectors with
        ## rand{-1,+1}.
        partest <- apply(abs(crossprod(S_old, S) - n) < eps*n,
                         2, any)
        if (all(partest)) {
            ## All the current vectors are parallel to old vectors.
            ## We've hit a cycle, so stop.
            if(!quiet) message("hit a cycle (1) -- stop iterations")
            break
        }
        if (any(partest)) {
            ## Some vectors are parallel to old ones and are cycling,
            ## but not all of them.  Replace the parallel vectors with
            ## rand{-1,+1}.
            numpar <- sum (partest)
            replacements <- matrix(sample(c(-1,1), n*numpar,replace=TRUE),
                                   n, numpar)
            S[,partest] <- replacements
        }

        ## Now test for parallel vectors within S.
        partest <- apply(crossprod(S) - I.t == n, 2, any)
        if (any(partest)) {
            numpar <- sum(partest)
            replacements <- matrix(sample(c(-1,1), n*numpar,replace=TRUE),
                                   n, numpar)
            S[,partest] <- replacements
        }

        Z <- if(mi.A) At.x(S) else crossprod(A, S) ## -- n x t

        ## Now find the largest non-previously-visted index per vector.
        ## h = max(2, abs(Z)) ## --  n x t
        h <- pmax.int(2, as(abs(Z),"matrix")); dim(h) <- dim(Z) ## --  n x t
        ## [mh, mhi] = max (h) : for each column h[,j]:
        ##             mh[j] = max(h[,j]); mhi = argmax(..)
        mhi <- apply(h, 2, which.max) ## mh <- h[cbind(mhi,1:t)]

        if (iter >= 2 && all(mhi == imax)) {
            ## (mhi == imax) : in octave this is only true when it's for all()
            ## Hit a cycle, stop.
            if(!quiet) message("hit a cycle (2) -- stop iterations")
            break
        }
        ## [h, ind] = sort (h, 'descend'):
        r <- apply(h, 2, sort.int, decreasing=TRUE, index.return=TRUE) #-> list
        h  <- sapply(r, `[[`, "x")
        ind <- sapply(r, `[[`, "ix") #->  n x t {each column = permutation of 1:n}
        if (t > 1) {
            firstind <- ind[1:t]
            if (all (been_there[firstind])) {
                ## Visited all these before, so stop.
                break
            }
            ind <- ind[!been_there[ind]] ##-> now ind is a simple vector
            if(length(ind) < t) {
                ## There aren't enough new vectors, so we're practically
                ## in a cycle. Stop.
                if(!quiet) message("not enough new vecs -- stop iterations")
                break
            }
        }

        ## Visit the new indices.
        X <- matrix(0, n, t)
        X[cbind(ind[1:t], 1:t)] <- 1
        ## for(zz in 1:t) X[ind[zz],zz] <- 1

        been_there [ind[1:t]] <- TRUE
    } ## for(iter ...)

    ## The estimate est and vector w are set in the loop above. The
    ## vector v selects the imax column of A.
    v <- integer(n)
    v[imax] <- 1L
    list(est=est, v=v, w=w, iter=iter)
}## {onenormest}


## %!demo
## %!  N = 100;
## %!  A = randn(N) + eye(N);
## %!  [L,U,P] = lu(A);
## %!  nm1inv = onenormest(@(x) U\(L\(P*x)), @(x) P'*(L'\(U'\x)), N, 30)
## %!  norm(inv(A), 1)

## %!test
## %!  N = 10;
## %!  A = ones (N);
## %!  [nm1, v1, w1] = onenormest (A);
## %!  [nminf, vinf, winf] = onenormest (A', 6);
## %!  assert (nm1, N, -2*eps);
## %!  assert (nminf, N, -2*eps);
## %!  assert (norm (w1, 1), nm1 * norm (v1, 1), -2*eps)
## %!  assert (norm (winf, 1), nminf * norm (vinf, 1), -2*eps)

## %!test
## %!  N = 10;
## %!  A = ones (N);
## %!  [nm1, v1, w1] = onenormest (@(x) A*x, @(x) A'*x, N, 3);
## %!  [nminf, vinf, winf] = onenormest (@(x) A'*x, @(x) A*x, N, 3);
## %!  assert (nm1, N, -2*eps);
## %!  assert (nminf, N, -2*eps);
## %!  assert (norm (w1, 1), nm1 * norm (v1, 1), -2*eps)
## %!  assert (norm (winf, 1), nminf * norm (vinf, 1), -2*eps)

## %!test
## %!  N = 5;
## %!  A = hilb (N);
## %!  [nm1, v1, w1] = onenormest (A);
## %!  [nminf, vinf, winf] = onenormest (A', 6);
## %!  assert (nm1, norm (A, 1), -2*eps);
## %!  assert (nminf, norm (A, inf), -2*eps);
## %!  assert (norm (w1, 1), nm1 * norm (v1, 1), -2*eps)
## %!  assert (norm (winf, 1), nminf * norm (vinf, 1), -2*eps)

## ## Only likely to be within a factor of 10.
## %!test
## %!  N = 100;
## %!  A = rand (N);
## %!  [nm1, v1, w1] = onenormest (A);
## %!  [nminf, vinf, winf] = onenormest (A', 6);
## %!  assert (nm1, norm (A, 1), -.1);
## %!  assert (nminf, norm (A, inf), -.1);
## %!  assert (norm (w1, 1), nm1 * norm (v1, 1), -2*eps)
## %!  assert (norm (winf, 1), nminf * norm (vinf, 1), -2*eps)
