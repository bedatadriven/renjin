library(hamcrest)

# Ensure that we can read serialized data.frames from GNU R
# With the row.names hack

test.readGnuDataFrames <- function() {
  
  df <- readRDS("gnuRowNames.rds")
  assertThat(nrow(df), identicalTo(10L))
  assertThat(attr(df, 'row.names'), identicalTo(as.character(1:10)))
  assertThat(df$a, identicalTo(1:10))
  assertThat(df$b, identicalTo(factor(letters[1:10])))
}