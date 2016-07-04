
library(hamcrest)


test.dotCall <- function() {
    assertThat(dotCall(1:10), identicalTo(10 * 42))
}

test.dotFortran <- function() {
    data <- fortranCall()
    assertThat(data[1], identicalTo(999));
}

test.dotC <- function() {
    data <- dotC()
    assertThat(data[1], identicalTo(3333L));
}


