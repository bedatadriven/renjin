
# From Advanced R

test.construction <- function() {
  Person <- setRefClass("Person")
  x <- Person$new()
  
  assertThat(class(x), equalTo("Person"))  
}