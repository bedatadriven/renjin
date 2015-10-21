

test.binomial <- function() {
  Conversions <- matrix(c(23, 100, 21, 100, 17, 100, 15, 100), nrow=4, ncol=2)
  X <- c(1, 1, 0, 0)
  Y <- c(1, 0, 1, 0)
  
  fit <- glm(formula = Conversions ~ X + Y, family="binomial")
  
  singleCoefficients <- fit$coefficients
  singleConfidenceIntervals <- confint(fit, level=0.95)
}