library(hamcrest)

test.by <- function() {
  df <- data.frame(x = c(1, 1, 1, 2, 2, 3, 3),
                   y = c(1, 2, 3, 4, 5, 6, 7))
  res <- by(df$y, df$x, sum)

  assertThat(res, instanceOf("by"))
}
