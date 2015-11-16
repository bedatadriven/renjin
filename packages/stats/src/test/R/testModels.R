
library(hamcrest)

test.simplest <- function() {
	formula <- ~1
	t <- terms.formula(formula)
	
	assertThat(typeof(t), equalTo("language"))
	assertThat(t[[1]], equalTo(quote(`~`)))
	assertThat(t[[2]], equalTo(1))
	
	assertThat(attr(t, 'variables'), identicalTo(quote(list())))
	assertThat(attr(t, 'factors'), identicalTo(integer(0)))
	assertThat(environment(t), identicalTo(environment()))
}

test.predict <- function() {
  x <- 1:10
  df <- data.frame(x=x, y=2*x)
  model <- lm(y~x, df)

  y <- predict(model, data.frame(x=200))

  assertThat(y, closeTo(400,0.1))
}


test.one.dep.var <- function() {
	t <- terms(~births)
	print(t)

	assertThat(class(t), equalTo(c("terms", "formula")))
	assertThat(attr(t, 'variables'), identicalTo(quote(list(births))))
	assertThat(attr(t, 'term.labels'), equalTo("births"))
	assertThat(attr(t, 'factors'), equalTo(1L))
	
}

test.model.matrix.with.interactions <- function() {
	data <- data.frame(age=c(18,20,22,25), height=c(110,100,75,120), row.names=c('a','b','c','d'))
	m <- model.matrix(~ age * height, data=data)
	
	assertThat(dim(m), equalTo(c(4L,4L)))
	assertThat(m, equalTo(c(1,1,1,1,18,20,22,25,110,100,75,120,1980,2000,1650,3000)))
	assertThat(colnames(m), equalTo(c("(Intercept)", "age", "height", "age:height")))
	assertThat(rownames(m), equalTo(c("a","b","c","d")))
	
	print(m)
}

test.modelMatrixSimple <- function() {
	data <- data.frame(age=c(18,20,22,25), height=c(110,100,75,120), row.names=c('a','b','c','d'))
	m <- model.matrix(~ age + height, data=data)
	
	assertThat(dim(m), equalTo(c(4,3)))
	assertThat(m, equalTo(c(1,1,1,1,18,20,22,25,110,100,75,120)))
	assertThat(colnames(m), equalTo(c("(Intercept)", "age", "height")))
	assertThat(rownames(m), equalTo(c("a","b","c","d")))
	
	print(m)	
}

test.model.matrix.factors <- function() {
	
	x <- c('Good', 'Bad', 'Ugly', 'Good');
	mm <- model.matrix(~x)
	
	print(mm)
}

test.model.frame <- function() {

	x <- 1:5
	y <- 5:1
	z <- c(0.4, -0.2, -1.2, -0.03, 1.6)



#  @Test
#  public void simpleModelMatrix() {
#    eval("x <- 1:10 ");
#    eval("y <- 10:1 ");
#    eval("z <- rnorm(10) ");
#    
#    eval("print(mm <- model.matrix(~x+y+z))");
#    assertThat(eval("attr(mm, 'assign')"), equalTo(c_i(0, 1, 2, 3)));
#  }

}

test.model.frame.including.functions <- function() {
	f <- function(x) 2 * x
	x <- 1:5
	y <- 1:5
	
	formula <- f(y) ~ x
	
	mf <- model.frame(formula)

	
	assertThat(names(mf), identicalTo(c("f(y)", "x")))
	assertThat(mf[[1]], identicalTo(c(2, 4, 6, 8, 10)))
  assertThat(mf[[2]], identicalTo(1:5))
  assertThat(row.names(mf), identicalTo(as.character(1:5)))
  
}

test.model.frame.with.matrices <- function() {
  
  Conversions <- matrix(c(23, 100, 21, 100, 17, 100, 15, 100), nrow=4, ncol=2)
  X <- c(1, 1, 0, 0)
  Y <- c(1, 0, 1, 0)
  
  mf <- model.frame(Conversions ~ X + Y)

  assertThat(names(mf), identicalTo(c("Conversions", "X", "Y")))
}

test.model.frame.with.na.omit <- function() {

  X <- c(1,  NA, 3, 4)
  Y <- c(NA,  1, 2, 5)
  
  mf <- model.frame(X ~ Y, na.action = na.omit)
  
  assertThat(nrow(mf), equalTo(2))
}

test.models.with.extras <- function() {
  X <- c(1, 1, 0, 0)
  Y <- c(1, 0, 1, 0)
  W <- c(1, 2, 3, 4)
  
  mf <- model.frame(X ~ Y, weights = W)
  
  assertThat(names(mf), identicalTo(c("X", "Y", "(weights)")))
  assertThat(mf[["(weights)"]], identicalTo(W))
}

test.models.with.null.extras <- function() {
  X <- c(1, 1, 0, 0)
  Y <- c(1, 0, 1, 0)
  
  mf <- model.frame(X ~ Y, weights = NULL)
  
  assertThat(names(mf), identicalTo(c("X", "Y")))
}



#  @Test
#  public void modelMatrixFactors() {
#    eval("x <- c('Good', 'Bad', 'Ugly', 'Good') ");
#    
#    eval("print(mm <- model.matrix(~x))");
#    
#    assertThat(eval("mm[,2]"), equalTo(c(1,0,0,1)));
#    assertThat(eval("mm[,3]"), equalTo(c(0,0,1,0)));
#    assertThat(eval("colnames(mm)"), equalTo(c("(Intercept)", "xGood", "xUgly")));
#    assertThat(eval("attr(mm, 'contrasts')$x"), equalTo(c("contr.treatment")));
#  }
#  
#  @Test
#  public void simpleInteraction() {
#    eval("x <- 1:10 ");
#    eval("y <- 10:1 ");
#    
#    eval("print(mm <- model.matrix(~x*y))"); 
#  }
# 
#  
#  @Test
#  public void interactionWithFactors() {
#    eval("x <- 1:10 ");
#    eval("y <- rep(c('Good', 'Bad', 'Ugly'), length=10) ");
#    
#    eval("print(mm <- model.matrix(~x*y))"); 
#  }