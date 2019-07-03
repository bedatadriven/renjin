
# From Advanced R

failing.test.construction <- function() {
  Person <- setRefClass("Person")
  x <- Person$new()
  
  assertThat(class(x), equalTo("Person"))  
}