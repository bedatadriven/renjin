
library(hamcrest)

test.csv <- function() {

    df <- read.csv("tables/simple.csv")
    assertThat(names(df), identicalTo(c("A", "B", "C")))
}