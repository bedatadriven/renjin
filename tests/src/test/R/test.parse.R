
library(hamcrest)

test.keepsource <- function() {

    x <- parse(text = "x+y\ny+z", keep.source = TRUE)
    assertFalse(is.null(attr(x, 'srcref')))

    y <- parse(text = "x+y\ny+z", keep.source = FALSE)
    assertTrue(is.null(attr(y, 'srcref')))

}