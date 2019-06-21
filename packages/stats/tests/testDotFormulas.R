
library(hamcrest)


df <- data.frame(x = 1:3, y = 1:3, z = 1:3)

hasFormula <- function(expected) {
    return(function(actual) {
        x <- expected
        y <- actual
        attributes(x) <- NULL
        attributes(y) <- NULL
        identical(x, y)
    })
}

test.dot.expansion <- function() {
    
    f <- x ~ . 
    t <- terms(f, data = df)

    assertThat(t, hasFormula(x ~ y + z))
    
    mdf <- model.frame(f, data = df)
    
    assertThat(names(mdf), equalTo(c("x", "y", "z")))
}


test.dot.expansion.without.response <- function() {
    
    f <-  ~ . 
    t <- terms(f, data = df)

    assertThat(t, hasFormula(~ x + y + z))
    
    mdf <- model.frame(f, data = df)
    
    assertThat(names(mdf), equalTo(c("x", "y", "z")))
}

test.dot.expansion.nested <- function() {

    f <-  x ~ y + .
    t <- terms(f, data = df)

    assertThat(t, hasFormula(x ~ y + (y + z)))
}

test.dot.expansion.interaction <- function() {

    f <-  x ~ y * .
    t <- terms(f, data = df)

    assertThat(t, hasFormula(x ~ y * (y + z)))
}

test.allow.dot.as.name <- function() {

    ddf <- data.frame(x = 1:3, "." = 1:3)
    t <- terms(x ~ ., data = ddf)
    
    assertThat(t, hasFormula(x ~ .))
    
    mf <- model.frame(t, data = ddf)
    
    assertThat(names(mf), identicalTo(c("x", ".")))

}