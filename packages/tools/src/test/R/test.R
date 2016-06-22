
library(hamcrest)
library(tools)

test.md5 <- function() {
    hashes <- md5sum(files=c("test.png", "test.txt", "non-existant.bin"))
    print(hashes)
    assertThat(hashes, identicalTo(
        c("test.png" = "8c107678aa972b6c832e610fb4cb54f8",
          "test.txt" = "891f7875f6ea70116d6864512a3d1c84",
          "non-existant.bin" = NA)))
}


test.utils.functions <- function() {
    res <- tools:::.get_internal_S3_generics()

    assertThat(typeof(res), identicalTo("character"))
    assertTrue(length(res) > 1)
}