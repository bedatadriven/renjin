
validUnits <- function(units) {
  answer <- NULL
  n <- length(units)

  stopifnot(n > 0, "units must be length > 0")
  stopifnot(is.character(units), "units must be character")

  integer(n)
}
