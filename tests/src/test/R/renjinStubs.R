library(hamcrest)

test.paste0 <- function() {
    s1 <- paste0("foo", "bar")
    s2 <- paste0(c("A", "B"), collapse = ", ")

    assertThat(s1, equalTo("foobar"))
    assertThat(s2, equalTo("A, B"))
}

test.rep_len <- function() {
    assertThat(rep_len("a", 2), identicalTo(c("a", "a")))
}

test.anyNA <- function() {
    assertTrue(anyNA(c(1, NA, 3)))
    assertTrue(anyNA(c(1, NA, 3), recursive = TRUE))
    assertFalse(anyNA(list(a = c(1, NA, 3), b = "a")))
    assertTrue(anyNA(list(a = c(1, NA, 3), b = "a"), recursive = TRUE))
    assertFalse(anyNA(as.POSIXlt(Sys.time())))
}

test.lengths <- function() {
    assertThat(lengths(seq(10)), identicalTo(rep(1L, times = 10)))
    x <- list(a = c(1, 2), b = "foobar")
    assertThat(names(lengths(x)), identicalTo(c("a", "b")))
    assertThat(lengths(x), equalTo(c(2,1)))
    assertTrue(is.null(names(lengths(x, use.names = FALSE))))
}

