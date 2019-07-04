
library(hamcrest)

conn <- file("tobin.txt.gz", open = "rt")
s <- readChar(conn, nchars = 7)

assertThat(s, identicalTo("durable"))
