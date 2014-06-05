library(hamcrest)

# Not yet running on renjin
ignore.test.vectorize <- function() {
  foo <- function(a) return(a)
  bar <- Vectorize(foo,"a")
  assertThat(names(formals(bar)), identicalTo("a"))
  assertThat(bar(1), equalTo(1))
}
