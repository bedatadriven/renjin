library(hamcrest)

ERROR <- 0.0001

test.dist <- function() {
	print(dist(1:10))
}

test.dist.matrix <- function() {
	m <- as.matrix(dist(1:10))
	assertThat(dim(m), equalTo(c(10,10)))
}

test.ns <- function() {
	print(ls(envir=.BaseNamespaceEnv$.__S3MethodsTable__.))
}

test.norm <- function() {
	assertThat(dnorm(0, 0, 1,  FALSE), closeTo(0.3989423, ERROR))
	assertThat(dnorm(0, 0, 1,  TRUE), closeTo(-0.9189385, ERROR))
	
	assertThat(pnorm(0.25, 0, 1,  TRUE,  FALSE), closeTo(0.5987063, ERROR))
	assertThat(pnorm(0.25, 0, 1,  FALSE,  FALSE), closeTo(0.4012937, ERROR))
	assertThat(pnorm(0.25, 0, 1,  TRUE,  TRUE), closeTo(-0.5129841, ERROR))
	assertThat(pnorm(0.25, 0, 1,  FALSE,  TRUE), closeTo(-0.9130618, ERROR))
	
	assertThat(qnorm(0.25, 0, 1,  TRUE,  FALSE), closeTo(-0.6744898, ERROR))
	assertThat(qnorm(0.25, 0, 1,  FALSE,  FALSE), closeTo(0.6744898, ERROR))
	assertThat(qnorm(0.99, 0, 1,  FALSE,  FALSE), closeTo(-2.326348, ERROR))
	assertThat(qnorm(0.99, 0, 1,  FALSE,  TRUE), is.nan)
	assertThat(qnorm(0, 0, 1,  TRUE,  FALSE), equalTo(-Inf))
	
}


test.beta <- function() {
	assertThat(dbeta(0.4, 5, 1, FALSE), closeTo(0.128, ERROR))
}


test.dnbeta <- function() {
	assertThat(dbeta(x=0.5, shape1=20.0, shape2=20.0, ncp=1.0, log=FALSE), closeTo(5.000253, ERROR))
	assertThat(dbeta(x=0.8, shape1=40.0, shape2=20.0, ncp=0.5,  log=TRUE), closeTo(-0.670098, ERROR))
}


test.binom <- function() {
	assertThat(dbinom(3, 5, 0.25, FALSE), closeTo(0.08789063, ERROR))
}


test.qbinom <- function() {
	assertThat(qbinom(0.2, 114, 0.55, TRUE, FALSE), closeTo(58, ERROR))
	assertThat(qbinom(0.1, 21, 0.2, TRUE, FALSE), closeTo(2, ERROR))
}


test.exp <- function() {
	assertThat(dexp(x=0.5, 1/0.25, log=FALSE), closeTo(0.5413411, ERROR))
}


test.hyper <- function() {
	assertThat(dhyper(x=3, m=5, n=2, 3, FALSE), closeTo(0.2857143, ERROR))
}

test.QLogNormal <- function() {
	assertThat(qlnorm(0.95, 0, 1,  TRUE,  FALSE), closeTo(5.180252, ERROR))
	assertThat(qlnorm(0.68, 0, 1,  FALSE,  FALSE), closeTo(0.6264422, ERROR))
}


test.PLogNormal <- function() {
	assertThat(plnorm(1.96, 0, 1,  TRUE,  FALSE), closeTo(0.7495087, ERROR))
	assertThat(plnorm(2.55, 0, 1,  FALSE,  FALSE), closeTo(0.1746126, ERROR))
}


test.DLogNormal <- function() {
	assertThat(dlnorm(1.96, 0, 1,   FALSE), closeTo(0.1622998, ERROR))
	assertThat(dlnorm(2.55, 0, 1,   TRUE), closeTo(-2.293167, ERROR))
}


test.dgeom <- function() {
	assertThat(dgeom(5, 0.5, FALSE), closeTo(0.015625, ERROR))
	assertThat(dgeom(10, 0.2, TRUE), closeTo(-3.840873, ERROR))
}


test.pgeom <- function() {
	assertThat(pgeom(3, 0.5, TRUE, FALSE), closeTo( 0.9375, ERROR))
	assertThat(pgeom(10, 0.2, FALSE,FALSE ), closeTo( 0.08589935, ERROR))
}


test.qgeom <- function() {
	assertThat(qgeom(0.9, 0.6, TRUE, FALSE), closeTo( 2.0, ERROR))
	assertThat(qgeom(0.4, 0.1, FALSE,FALSE ), closeTo( 8.0, ERROR))
}



test.dnbinom <- function() {
	assertThat(dnbinom(x=3, size=5, prob=0.25, log=FALSE), closeTo(0.01441956, ERROR))
}


test.dnbinom_mu <- function() {
	assertThat(dnbinom(x=4, size=10, mu=10, log=FALSE), closeTo( 0.04364014, ERROR))
	assertThat(dnbinom(x=6, size=10, mu=10, log=TRUE), closeTo(-2.572162, ERROR))
}

test.pnbinom <- function() {
	assertThat(pnbinom(q=3, size=5, prob=0.5, lower.tail=FALSE, log.p=FALSE), closeTo(0.6367187, ERROR))
}


test.qnbinom <- function() {
	assertThat(qnbinom(p=0.4, size=900, prob=0.9, lower.tail=TRUE, log.p=FALSE), closeTo(97, ERROR))
	assertThat(qnbinom(p=0.01, size=900, prob=0.9, lower.tail=TRUE, log.p=FALSE), closeTo(76, ERROR))
	assertThat(qnbinom(p=0.1, size=900, prob=0.3, lower.tail=TRUE, log.p=TRUE), is.nan)
}


test.qnbinom_mu <- function() {
	assertThat(qnbinom(p=0.6, size=20, mu=8, lower.tail=TRUE, log.p=FALSE), closeTo(9.0, ERROR))
}


test.plogis <- function() {
	assertThat(plogis(2.55, 0, 1, FALSE, FALSE), closeTo(0.07242649, ERROR))
}


test.dlogis <- function() {
	assertThat(dlogis(3, 5, 0.25, FALSE), closeTo(0.001340951, ERROR))
}


test.qlogis <- function() {
	assertThat(qlogis(0.7, 0, 1, FALSE, FALSE), closeTo(-0.8472979, ERROR))
}


test.qsignrank <- function(){
	assertThat(qsignrank(0.7, 10, FALSE, FALSE), closeTo(22, ERROR))
	assertThat(qsignrank(0.7, 10, TRUE, FALSE), closeTo(33, ERROR))
}


test.psignrank <- function(){
	assertThat(psignrank(0.7, 10, FALSE, FALSE), closeTo(0.99902, ERROR))
	assertThat(psignrank(0.7, 10, TRUE, FALSE), closeTo(0.0009765, ERROR))
}


test.dsignrank <- function(){
	assertThat(dsignrank(2, 5, FALSE), closeTo(0.03125, ERROR))
	assertThat(dsignrank(2, 5, TRUE), closeTo(-3.465736, ERROR))
}


test.dwilcox <- function(){
	assertThat(dwilcox(x=10, m=5,3, FALSE), closeTo(0.08928571, ERROR))
	assertThat(dwilcox(x=20, m=6,4, TRUE), closeTo(-3.73767, ERROR))
}


test.pwilcox <- function(){
	assertThat(pwilcox(2,10,5, FALSE, FALSE), closeTo(0.998668, ERROR))
	assertThat(pwilcox(7,15,7, TRUE, FALSE), closeTo(0.0002638615, ERROR))
}

test.qwilcox <- function(){
	assertThat(qwilcox(0.5,10,4, TRUE, FALSE), closeTo(20.0, ERROR))
	assertThat(qwilcox(0.1,4, 10, FALSE, FALSE), closeTo(29.0, ERROR))
}

test.pnchisquare <- function() {
	assertThat(pchisq(q=0.75, df=4, ncp=1, lower.tail=TRUE, log.p=FALSE), closeTo(0.03540971, ERROR))
}


test.qnchisquare <- function() {
	assertThat(qchisq(p=0.75, df=4, ncp=1, lower.tail=TRUE, log.p=FALSE), closeTo(6.737266, ERROR))
}


test.pnt <- function() {
	assertThat(pt(q=1.96, df=20, ncp=2, lower.tail=TRUE, log.p=FALSE), closeTo(0.4752101, ERROR))
}

test.qnt <- function(){
	assertThat(qt(p=0.8, df=20, ncp=2, lower.tail=TRUE, log.p=FALSE), closeTo(2.965995, ERROR))
}


test.dnt <- function(){
	assertThat(dt(x=2, df=20, ncp=3, log=FALSE), closeTo(0.2435572, ERROR))
}


test.dnchisq <- function(){
	assertThat(dchisq(x=2, df=10, ncp=5, log=FALSE), closeTo(0.001017647, ERROR))
	assertThat(dchisq(x=5, df=9, ncp=10, log=TRUE), closeTo(-5.125956, ERROR))
}


test.pnbinom_mu <- function(){
	assertThat(pnbinom(q=0.25, size=10, mu=4, lower.tail=TRUE, log.p=FALSE), closeTo( 0.03457161, ERROR))
	assertThat(pnbinom(q=0.25, size=10, mu=4, lower.tail=TRUE, log.p=TRUE), closeTo( -3.364722, ERROR))
	assertThat(pnbinom(q=0.25, size=10, mu=4, lower.tail=FALSE, log.p=FALSE), closeTo(0.9654284, ERROR))
}

test.pnbeta <- function() {
	assertThat(pbeta(q=0.25, shape1=1, shape2=6, ncp=1, lower.tail=TRUE, log.p=FALSE), closeTo(0.6935046, ERROR))
	assertThat(pbeta(q=0.25, shape1=1, shape2=6, ncp=1, lower.tail=TRUE, log.p=TRUE), closeTo(-0.3659974, ERROR))
}

test.dnf <- function() {
	assertThat(df(x=1, df1=6, df2=6, ncp=1, log=FALSE), closeTo(0.4621278, ERROR))
	assertThat(df(x=1, df1=6, df2=6, ncp=1, log=TRUE), closeTo(-0.7719139, ERROR))
}

test.dnf <- function() {
	assertThat(df(0, 6,6 , 1, FALSE), closeTo(0, ERROR))
	assertThat(df(1, 6,6 , 1, FALSE), closeTo(0.4621278, ERROR))
	assertThat(df(2, 6,6 , 1, TRUE), closeTo(-1.662094, ERROR))
}

#
# qnbeta() and qnf() functions sometimes return different values when compared to
# original interpreter. This is about accuracy and should be corrected at next level.
# mhsatman.


test.qnbeta <- function(){
	assertThat(qbeta(p=0.05, shape1=12, shape2=8, ncp=1, lower.tail=TRUE, log.p=FALSE), closeTo(0.428099, ERROR))
}


test.qnf <- function(){
	assertThat(qf(p=0.05, df1=4, df2=2, ncp=1, lower.tail=TRUE, log.p=FALSE), closeTo( 0.1835066, ERROR))
}


test.tukeys <- function() {
	#This is confusing. Because location of parameters are replaced in R calls
	
	assertThat(ptukey(q=5.20, nmeans=14, df=12, nranges=5, lower.tail=TRUE, log.p=FALSE), closeTo(0.7342322, ERROR))
	assertThat(ptukey(q=4.9, nmeans=21, df=9, nranges=2, lower.tail=T, log.p=T), closeTo(-0.406227, ERROR))
	assertThat(qtukey(p=0.90, nmeans=6, df=3, nranges=4, lower.tail=T, log.p=F), closeTo(8.001985, ERROR))
}


test.chisquareZeroDf <- function() {
	assertThat(pchisq(0.5, df=0, lower.tail=TRUE, log.p=FALSE), equalTo(1))
	assertThat(pchisq(0.5, df=0, lower.tail=FALSE, log.p=FALSE), equalTo(0))
	assertThat(pchisq(0.5, df=0, ncp=0.5, lower.tail=TRUE, log.p=FALSE), closeTo(0.8225176, ERROR))
	assertThat(qchisq(0.5, df=0, lower.tail=TRUE, log.p=FALSE), equalTo(0))
	assertThat(is.infinite(qchisq(1, df=0, lower.tail=TRUE, log.p=FALSE)), equalTo(TRUE))	
}
