
library(hamcrest)

test.simple <- function() {
    x <- y ~ z

    assertThat(typeof(x), identicalTo("language"))
    assertThat(deparse(x), identicalTo("y ~ z"))

    ff <- `~`

    xx <- ff(a, b)

    assertThat(deparse(xx), identicalTo("ff(a, b)"))
}

test.attributes.preserved <- function() {

    ff <- y ~ x
    attr(ff, 'foo') <- 'bar'

    ff2 <- eval(ff)

    assertThat(attr(ff2, 'foo'), identicalTo('bar'))
}
