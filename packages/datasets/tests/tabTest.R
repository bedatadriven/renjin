
library(hamcrest)
library(datasets)

assertThat(class(airquality$Ozone), identicalTo("integer"))

