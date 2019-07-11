

library(hamcrest)

f <- function(n, ...) ...elt(n)


assertThat(f(0, 1, 2, 3), throwsError())
assertThat(f(-1, 1, 2, 3), throwsError())
assertThat(f(1, 91, 92, 93), identicalTo(91))
assertThat(f(2, 91, 92, 93), identicalTo(92))
assertThat(f(3, 91, 92, 93), identicalTo(93))
assertThat(f(4, 91, 92, 93), throwsError())


