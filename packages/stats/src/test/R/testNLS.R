

test.NLS <- function() {

  df <- data.frame(x = 1:10, y = log(1:10) + 3 + rnorm(10))

  model <- nls(y ~ log(x) + b, df)

  print(model)

}