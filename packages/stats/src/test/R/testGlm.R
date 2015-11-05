
library(hamcrest)

test.binomial <- function() {
  Conversions <- matrix(c(23, 100, 21, 100, 17, 100, 15, 100), nrow=4, ncol=2)
  X <- c(1, 1, 0, 0)
  Y <- c(1, 0, 1, 0)
  
  fit <- glm(formula = Conversions ~ X + Y, family="binomial")
  
  singleCoefficients <- fit$coefficients
#  singleConfidenceIntervals <- confint(fit, level=0.95)
  
  # Check estimates
  assertThat(singleCoefficients, closeTo(c(0.002665463, -0.005330927,  0.318594578), 0.000001))
  
#  print(singleConfidenceIntervals)
  
  # Check confidence interval of first coefficient
#  assertThat(singleConfidenceIntervals[2,], closeTo(c(-0.3657575, 0.3550100), 0.00001))
 # assertThat(singleConfidenceIntervals[3,], closeTo(c( -0.1741743, 0.8206563), 0.00001))
}