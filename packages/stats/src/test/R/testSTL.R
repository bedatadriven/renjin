
library(datasets)
library(stats)
library(hamcrest)

test.stl <- function() {

    stmR <- stl(mdeaths, s.window = "per", robust = TRUE)
    outliers <- which(stmR $ weights  < 1e-8)

    assertThat(outliers, identicalTo(c(24L, 26L, 27L, 28L, 36L, 37L, 50L, 52L, 59L, 61L)))

}