
library(hamcrest)

# Write to the variable 'foo'

zz <- textConnection("foo", "w")

assertThat(foo, identicalTo(character(0)))

writeLines(c('testit1', 'testit2'), zz)

assertThat(foo, identicalTo(c("testit1", "testit2")))

writeLines(c('a\nb\n', 'c'), zz)

assertThat(foo, identicalTo(c("testit1", "testit2", "a", "b", "", "c")))


# Write to an existing variable

bar <- 33

xy <- textConnection("bar", "w")

writeLines(c('testit1', 'testit2'), xy)

assertThat(bar, identicalTo(c("testit1", "testit2")))