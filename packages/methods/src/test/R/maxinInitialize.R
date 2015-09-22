# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)
library(stats)

test.setClassInheritRepresentationAndContains <- function() {
		setClass("A", representation(a="numeric"))
		a1 <- new("A", a=1.5)
		m1 <- as.matrix(1)
		setClass("M", contains = "matrix", representation(fuzz = "numeric"))
		set.seed(113)
		f1 <- runif(3)

		assertThat(
			as(new("M", 1:12, nrow = 3, fuzz = f1), "matrix"),
			identicalTo(matrix(1:12, nrow=3))
			)

		assertThat(
			as(new("M", 1:12, 3, fuzz = f1), "matrix"),
			identicalTo(matrix(1:12, 3))
			)

		assertThat(            ########### PASSED! ###########
			as(new("M", 1:12, ncol = 3, fuzz = f1), "matrix"),
			identicalTo(matrix(1:12, ncol=3))
			)

}

test.setClassCallSlots <- function() {
		setClass("A", representation(a="numeric"))
		a1 <- new("A", a=1.5)
		m1 <- as.matrix(1)
		setClass("M", contains = "matrix", representation(fuzz = "numeric"))
		set.seed(113)
		f1 <- runif(3)
		setClass("B", contains = c("matrix", "A"))

		assertThat(
			new("B", m1, a1)@a,
			identicalTo(a1@a)
			)

		assertThat(
			as(new("B", m1),"matrix"),
			identicalTo(m1)
			)

		assertThat(
			new("B", matrix(m1, nrow = 2), a1, a=pi)@a,
			identicalTo(pi)
			)
}


test.setClassValidateSlots <- function() {
		setClass("A", representation(a="numeric"))
		a1 <- new("A", a=1.5)
		m1 <- as.matrix(1)
		setClass("M", contains = "matrix", representation(fuzz = "numeric"))
		set.seed(113)
		f1 <- runif(3)
		setClass("B", contains = c("matrix", "A"))
		setClass("C", contains = "B", representation(c = "character"))

		## verify that validity tests work (PR#14284)
		setValidity("B", function(object) {
            if(all(is.na(object@a) | (object@a > 0)))
              TRUE
            else
              FALSE
        })

		assertThat(            ########### PASSED! ###########
			try(validObject(new("B", a= c(NA,3, -1, 2))),silent=TRUE),
			instanceOf("try-error")
			)

		assertThat(
			try(validObject(new("A", a= c(NA,3, -1, 2))),silent=TRUE),
			instanceOf("try-error")
			)

		assertThat(            ########### PASSED! ###########
			try(validObject(new("B", m1, a2)),silent=TRUE),
			instanceOf("try-error")
			)
}



test.setClassValidateSlots <- function() {
	a3 <- array(1:24, 2:4)
	a2 <- array(1:12, 3:4)

	assertTrue(
		is(tryCatch(as(a3, "matrix"), error = function(e)e), "error")
		)

	assertTrue(            ########### PASSED! ###########
		is(as(a2 <- array(1:12, 3:4), "matrix"), "matrix")
		)

	assertTrue(            ########### PASSED! ###########
		is(a2, "matrix")
		)

	assertTrue(            ########### PASSED! ###########
		is(a2, "array")
		)

	assertTrue(            ########### PASSED! ###########
		is(a3, "array")
		)

	assertTrue(            ########### PASSED! ###########
		!is(a3, "matrix")
		)

	assertTrue(            ########### PASSED! ###########
		identical(a2, matrix(1:12, 3) )
		)

}

test.setClassUnion <- function() {

	assertFalse(            ########### PASSED! ###########
		is(tryCatch(setClassUnion("mn", c("matrix","numeric")), error = function(e)e), "error")
		)

	assertFalse(            ########### PASSED! ###########
		is(tryCatch(setClassUnion("an", c("array", "integer")), error = function(e)e), "error")
		)

	assertFalse(            ########### PASSED! ###########
		is(tryCatch(setClassUnion("AM", c("array", "matrix")), error = function(e)e), "error")
		)

}

test.setClassMix <- function() {
	f = setClass("BAR", slots = c(y="integer"))
	a1 <- new("BAR")
	a1@y <- 10L

	assertFalse(
		is(tryCatch(setClass("BAR", slots = c(y="integer")), error = function(e)e), "error")
		)

	assertTrue(
		is(f, "classGeneratorFunction")
		)

	assertTrue(
		is(a1@y, "integer")
		)

	assertTrue(
		is(a1@y, "numeric")
		)

	assertTrue(
		is(a1, "BAR")
		)

	assertThat(
		a1@y,
		identicalTo(10L)
		)

	f = function() { setMethod("initialize", "BAR", function(.Object, Y) {.Object@y <- Y; .Object}) }
	f_out = f()
	a2 <- new("BAR", 5L)

	assertFalse(            ########### PASSED! ###########
		is(tryCatch(f(), error = function(e)e), "error")
		)

	assertThat(            ########### PASSED! ###########
		f_out,
		identicalTo("initialize")
		)

	assertTrue(
		is(a2, "BAR")
		)

	assertTrue(
		is(a2@y, "integer")
		)

	assertTrue(
		is(a2@y, "numeric")
		)

	g = setClass("BAR3", contains = "BAR")
	b <- new("BAR3", 7L)

	assertFalse(
		is(tryCatch(setClass("BAR3", contains = "BAR"), error = function(e)e), "error")
		)

	assertTrue(
		is(g, "classGeneratorFunction")
		)

	assertTrue(
		is(b@y, "integer")
		)

	assertTrue(
		is(b@y, "numeric")
		)

	assertTrue(
		is(b, "BAR3")
		)

	assertThat(
		b@y,
		identicalTo(7L)
		)

	assertTrue(            ########### PASSED! ###########
		is(tryCatch(new("BAR3", 7), error = function(e)e), "error")
		)

	assertTrue(            ########### PASSED! ###########
		is(tryCatch(b@y <- 9, error = function(e)e), "error")
		)

	assertTrue(            ########### PASSED! ###########
		is(tryCatch(b@y <- "CHAR", error = function(e)e), "error")
		)


}
