
library(hamcrest)

test.csv <- function() {

    df <- read.csv("tables/simple.csv")
    assertThat(names(df), identicalTo(c("A", "B", "C")))
}

test.txt.with.comments <- function() {

    df <- read.table("tables/comments.txt", header = TRUE)
    assertThat(names(df), identicalTo(c("buf", "pH", "NaCl", "con", "ra", "det",
        "MgCl2", "temp", "prot.act1", "prot.act2", "prot.act3", "prot.act4")))

}