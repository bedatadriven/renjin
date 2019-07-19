
library(hamcrest)

x <- y ~ z

assertThat(typeof(x), identicalTo("language"))
assertThat(deparse(x), identicalTo("y ~ z"))

ff <- `~`

xx <- ff(a, b)

assertThat(deparse(xx), identicalTo("ff(a, b)"))
