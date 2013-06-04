library(hamcrest)

test.matchFactorToCharacter <- function() {
    x <- factor(c("Yes", "No", "Yes", "Yes"))
    assertThat(match(x[2:3], levels(x)), equalTo(c(1,2)))
    assertThat(match(levels(x), x[2:3]), equalTo(c(1,2)))
}

