
library(hamcrest)

test.assert <- function() {
	assertThat(1, equalTo(1))
	assertThat(1:3, equalTo(c(1,2,3)))
}

test.identicalTol.NaN <- function() {

    matcher <- identicalTo(NaN, tol = 1e-6)
    
    assertFalse(matcher(1L))
    assertFalse(matcher(1.5))
    assertTrue(matcher(NaN))
       
}

test.equals <- function() {
    assertThat( c(15, -2.5, -2.5, -2.5, -2.5), equalTo(c(15, -2.5, -2.5, -2.5, -2.5)))
}