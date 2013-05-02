
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

