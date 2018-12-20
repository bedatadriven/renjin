library("com.mycompany:extensionsdemo")
library("hamcrest")

test.extractDigits <- function() {
    assertThat(extractDigits(c("5", "6Y8", "He", "02")), identicalTo(c("5", "68", "", "02")))
    assertThat(extractDigits("56Y8He02"), identicalTo("56802"))
}

test.makeNumber <- function() {
    expected <- c(5, 68, NA, 2)
    assertThat(makeNumber(c("5", "6Y8", "He", "02")), identicalTo(expected))
    assertThat(makeNumber("234-123-789 12"), identicalTo(23412378912))
}



