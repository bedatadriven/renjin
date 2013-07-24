library(hamcrest)

test.searchPath <- function() {
    assertThat(search(), equalTo(c(".GlobalEnv", "package:stats", "package:graphics",
            "package:grDevices",  "package:utils", "package:datasets",
            "package:methods", "Autoloads",  "package:base")))
}