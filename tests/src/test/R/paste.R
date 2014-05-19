library(hamcrest)

test.paste <- function() {
    s1 <- paste("foo", "bar")
    s2 <- paste("x", 0, sep = "")
    s3 <- paste(c("A", "B"), collapse = ", ")

    assertThat(s1, equalTo("foo bar"))
    assertThat(s2, equalTo("x0"))
    assertThat(s3, equalTo("A, B"))
}

test.paste0 <- function() {
    s1 <- paste0("foo", "bar")
    s2 <- paste0(c("A", "B"), collapse = ", ")

    assertThat(s1, equalTo("foobar"))
    assertThat(s2, equalTo("A, B"))
}
