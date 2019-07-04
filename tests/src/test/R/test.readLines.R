
library(hamcrest)

assertThat(readLines("lines-null.txt", n = 2), identicalTo(c("A", "B")))
assertThat(readLines("lines-null.txt"), identicalTo(c("A", "B", "", "EFGH", "", "QZ\u000023", "")))
assertThat(readLines("lines-null.txt", skipNul = TRUE), identicalTo(c("A", "B", "", "EFGH", "", "QZ23", "")))

assertThat(readLines("lines.txt"), identicalTo(c(
     "This is the first line",
     "And the second",
     "A third",
     "",
     "Fifth (fourth was blank)",
     "Sixth",
     "and finally the seventh")))

assertThat(readLines("lines.txt", n = 2), identicalTo(c(
     "This is the first line",
     "And the second")))

conn <- file("lines.txt")
assertThat(readLines(conn, n = 1), identicalTo("This is the first line"))
assertThat(readLines(conn, n = 1), identicalTo("And the second"))
