
library(hamcrest)

# Note: not nested in functions as that affects
# the test.

x <- eval(quote(return(1)))

assertThat(x, identicalTo(x))


