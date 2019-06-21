
library(hamcrest)

x <- quote(g(a=41))
y <- quote(f(a=43))

x["a"] <- y["a"]

assertThat(x, identicalTo(quote(g(a=43))))

z <- list(b = 91, a = 92)
z["b"] <- y["a"]

assertThat(z, identicalTo(list(b = 43, a = 92)))
