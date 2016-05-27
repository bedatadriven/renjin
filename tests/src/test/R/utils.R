library(hamcrest)

test.utils.functions <- function() {
    res <- tools:::.get_internal_S3_generics()

    assertThat(typeof(res), identicalTo("character"))
    assertTrue(length(res) > 1)
}