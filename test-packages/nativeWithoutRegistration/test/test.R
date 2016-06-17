
library(hamcrest)

assertThat(secret(1:10), identicalTo(10 * 42))