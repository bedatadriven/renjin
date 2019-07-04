
library(hamcrest)

assertThat(summary(stdin()), identicalTo(list(
    description = "stdin",
    class = "terminal",
    mode = "r",
    text = "text",
    opened = "opened",
    "can read" = "yes",
    "can write" = "no")))

conn <- file(tempfile(), open = "wb")
x <- summary(conn)

assertThat(x$class, identicalTo("file"))
assertThat(x$mode, identicalTo("wb"))
assertThat(x$text, identicalTo("binary"))
assertThat(x$opened, identicalTo("opened"))
assertThat(x$`can read`, identicalTo("no"))
assertThat(x$`can write`, identicalTo("yes"))


# When the openSpec is left blank, potentially both read and write
z <- summary(file(tempfile()))
assertThat(z$mode, identicalTo("r"))
assertThat(z$text, identicalTo("text"))
assertThat(z$opened, identicalTo("closed"))
assertThat(z$`can read`, identicalTo("yes"))
assertThat(z$`can write`, identicalTo("yes"))

# however if 'r' is explicitly provided, then no write
z <- summary(file("lines.txt", open = "r"))
assertThat(z$mode, identicalTo("r"))
assertThat(z$text, identicalTo("text"))
assertThat(z$opened, identicalTo("opened"))
assertThat(z$`can read`, identicalTo("yes"))
assertThat(z$`can write`, identicalTo("no"))
