
library(hamcrest)

test.Agrep <- function() {
	assertThat( agrep("lasy", "1 lazy 2"), equalTo(1) )
	#assertThat( agrep("lasy", c(" 1 lazy 2", "1 lasy 2"), max = list(sub = 0)), equalTo(2) )
	assertThat( agrep("laysy", c("1 lazy", "1", "1 LAZY"), max = 2, value = TRUE), equalTo("1 lazy") )
	assertThat( agrep("laysy", c("1 lazy", "1", "1 LAZY"), max = 2, ignore.case = TRUE), equalTo(c(1,3)) )
}