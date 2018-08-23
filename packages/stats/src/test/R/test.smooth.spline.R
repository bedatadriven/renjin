library(stats)
library(graphics)
library(grDevices)

## partly moved from ../man/smooth.spline.Rd , quite system-specific.
##-- artificial example
y18 <- c(1:3, 5, 4, 7:3, 2*(2:5), rep(10, 4))
(use.l3 <- (Sys.info()[["machine"]] == "x86_64"))
## i386-Linux: Df ~= (even! > ) 18 : interpolating -- much smaller PRESS
## It is the too low 'low = -3' which "kills" the algo; low= -2.6 still ok
## On other platforms, e.g., x64, ends quite differently (and fine)
## typically with Df = 8.636
(s2. <- smooth.spline(y18, cv = TRUE,
                      control = list(trace=TRUE, tol = 1e-6,
                                     low = if(use.l3) -3 else -2)))
#plot(y18)
xx <- seq(1,length(y18), len=201)
#lines(predict(s2., xx), col = 4)
#mtext(deparse(s2.$call,200), side= 1, line= -1, cex= 0.8, col= 4)

(sdf8 <- smooth.spline(y18, df = 8, control=list(trace=TRUE)))# 11 iter.
sdf8$df - 8 # -0.0009159978
(sdf8. <- smooth.spline(y18, df = 8, control=list(tol = 1e-8)))# 14 iter.

## This gave error: "... spar 'way too large'" -- now sees in dpbfa() that it can't factorize
## --> and gives *warning* about too large spar only
## e <- try(smooth.spline(y18, spar = 50)) #>> error
## stopifnot(inherits(e, "try-error"))
ss50 <- try(smooth.spline(y18, spar = 50)) #>> warning only (in R >= 3.4.0) -- ?? FIXME
e <- try(smooth.spline(y18, spar = -9)) #>> error : .. too small'
stopifnot(inherits(e, "try-error"))
## "extreme" range of spar, i.e., 'lambda' directly  (" spar = c(lambda = *) "):
##  ---------------------  --> problem/bug for too large lambda
e10 <- c(-20, -10, -7, -4:4, 7, 10)
(lams <- setNames(10^e10, paste0("lambda = 10^", e10)))
lamExp <- as.expression(lapply(e10, function(E)
				substitute(lambda == 10^e, list(e = E))))
sspl <- lapply(lams, function(LAM) try(smooth.spline(y18, lambda = LAM)))
sspl
ok <- vapply(sspl, class, "") == "smooth.spline"
stopifnot(ok[e10 <= 7])
ssok <- sspl[ok]
ssGet  <- function(ch) t(sapply(ssok, `[` , ch))
ssGet1 <- function(ch)   sapply(ssok, `[[`, ch)
stopifnot(all.equal(ssGet1("crit"), ssGet1("cv.crit"), tol = 1e-10))# seeing rel.diff = 6.57e-12
## Interesting:  for really large lambda, solution "diverges" from the straight line
ssGet(c("lambda", "df", "crit", "pen.crit"))

#plot(y18); lines(predict(s2., xx), lwd = 5, col = adjustcolor(4, 1/4))
#invisible(lapply(seq_along(ssok), function(i) lines(predict(ssok[[i]], xx), col=i)))
i18 <- 1:18
#abline(lm(y18 ~ i18), col = adjustcolor('tomato',1/2), lwd = 5, lty = 3)
## --> lambda = 10^10 is clearly wrong: a *line* but not the L.S. one
#legend("topleft", lamExp[ok], ncol = 2, bty = "n", col = seq_along(ssok), lty=1)

##--- Explore 'all.knots' and 'keep.stuff'

s2   <- smooth.spline(y18, cv = TRUE, keep.stuff=TRUE)

s2.7  <- smooth.spline(y18, cv = TRUE, keep.stuff=TRUE, nknots = 7)
s2.11 <- smooth.spline(y18, cv = TRUE, keep.stuff=TRUE, nknots = 11)
#plot(y18)
#lines(predict(s2, xx), lwd = 5, col = adjustcolor(4, 1/4))
#lines(predict(s2.7,  xx), lwd = 3, col = adjustcolor("red", 1/4))
#lines(predict(s2.11, xx), lwd = 2, col = adjustcolor("forestgreen", 1/4))
## s2.11 is very close to 's2'
