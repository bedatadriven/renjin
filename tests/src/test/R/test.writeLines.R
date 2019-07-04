

library(hamcrest)

test.append <- function() {

    tmp <- tempfile()
    c <- file(tmp, open = 'w')
    writeLines(con = c, text = 'Hello world')
    close(c)

    c2 <- file(tmp, open = 'a')
    writeLines(con = c2, text = 'Hello again')
    close(c2)

    assertThat(readLines(tmp), identicalTo(c("Hello world", "Hello again")))
}

test.overwrite <- function() {

    tmp <- tempfile()
    c <- file(tmp, open = 'w')
    writeLines(con = c, 'Hello world')
    close(c)

    c2 <- file(tmp, open = 'w')
    writeLines(con = c2, 'Hello again')
    close(c2)

    assertThat(readLines(tmp), identicalTo('Hello again'))
}

