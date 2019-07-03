
library(hamcrest)
library(stats)


test.modelFrame <- function() {
    f <- function(x) 2*x 
    x <- 1:10
    y <- 1:10
    formula <- f(y) ~ x;
    print(model.frame(formula));
}

test.simpleModelMatrix <- function() {
    x <- 1:10
    y <- 10:1
    z <- rnorm(10)
    
    print(mm <- model.matrix(~x+y+z))
    assertThat(attr(mm, 'assign'), equalTo(c(0, 1, 2, 3)))
}
  

test.modelMatrixFactors <- function() {
    x <- c('Good', 'Bad', 'Ugly', 'Good') ;
    
    print(mm <- model.matrix(~x));
    print(attributes(mm));
    assertThat(mm[,2], equalTo(c(1, 0, 0, 1)));
    assertThat(mm[,3], equalTo(c(0, 0, 1, 0)));
    assertThat(colnames(mm), equalTo(c("(Intercept)", "xGood", "xUgly")))
    assertThat(attr(mm, 'contrasts')$x, equalTo(c("contr.treatment")))
}
  
test.simpleInteraction <- function() {
    x <- 1:10
    y <- 10:1
    
    print(mm <- model.matrix(~x*y))
}
 
test.interactionWithFactors <- function() {
    x <- 1:10 ;
    y <- rep(c('Good', 'Bad', 'Ugly'), length=10) ;
    
    print(mm <- model.matrix(~x*y)); 
}
  
test.dotInFormula <- function() {
    df <- data.frame(x=1:3,y=(1:3)*2, z=(1:3)*6);
    
    model.matrix(x ~ ., data = df);

}
