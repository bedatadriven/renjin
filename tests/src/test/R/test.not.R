
library(hamcrest)


test.not.preserves.attributes <- function() {

    x <- structure(TRUE, class="baz", foo = "blarb")

    assertThat(!x, identicalTo(structure(FALSE, class="baz", foo = "blarb")))
}
