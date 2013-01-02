

test.assert <- function() {
	assertThat(1, equalTo(1))
	assertThat(1:3, equalTo(c(1,2,3)))
}

test.fail <- function() {
	print(1:15)
	cat("testing\n")
	stop("fooo!")

}