
library(hamcrest)

df <- data.frame(
    a = c("R1", "R2", "R3"), 
    b = 1:3,  
    c = c(1.5, 2.5, 3.5), 
    d = c(TRUE, FALSE, TRUE))


test.dataFrame <- function() {

  f <- tempfile()
  print(f)
  write.table(df, file = f)
  
  assertThat(readLines(f), identicalTo(c(
    "\"a\" \"b\" \"c\" \"d\"", 
    "\"1\" \"R1\" 1 1.5 TRUE", 
    "\"2\" \"R2\" 2 2.5 FALSE",  
    "\"3\" \"R3\" 3 3.5 TRUE")))
}


test.dataFrameCommaCsv <- function() {

  f <- tempfile()
  print(f)
  write.table(df, file = f, dec = ",")
  
  assertThat(readLines(f), identicalTo(c(
    "\"a\" \"b\" \"c\" \"d\"", 
    "\"1\" \"R1\" 1 1,5 TRUE", 
    "\"2\" \"R2\" 2 2,5 FALSE",  
    "\"3\" \"R3\" 3 3,5 TRUE")))
}

test.dataFrameNoRownames <- function() {

  f <- tempfile()
  print(f)
  write.table(df, file = f, row.names = FALSE)
  
  assertThat(readLines(f), identicalTo(c(
    "\"a\" \"b\" \"c\" \"d\"", 
    "\"R1\" 1 1.5 TRUE", 
    "\"R2\" 2 2.5 FALSE",  
    "\"R3\" 3 3.5 TRUE")))
}

test.dataFrameNoQuoting <- function() {

  f <- tempfile()
  print(f)
  write.table(df, file = f, quote = FALSE)
  
  assertThat(readLines(f), identicalTo(c(
    "a b c d", 
    "1 R1 1 1.5 TRUE", 
    "2 R2 2 2.5 FALSE", 
    "3 R3 3 3.5 TRUE" )))
}

test.dataFrameSelectiveQuoting <- function() {

  df <- data.frame(a = c("X", "Y"), b = c("P", "Q"), c = c("T", "U"))

  f <- tempfile()
  print(f)
  write.table(df, file = f, quote = c(1,3))
  
  assertThat(readLines(f), identicalTo(c(
    "\"a\" \"b\" \"c\"", 
    "\"1\" \"X\" P \"T\"", 
    "\"2\" \"Y\" Q \"U\"" )))
}


test.dataFrameCsv <- function() {

  f <- tempfile()
  print(f)
  write.csv(df, file = f)
  
  assertThat(readLines(f), identicalTo(c(
    "\"\",\"a\",\"b\",\"c\",\"d\"",
    "\"1\",\"R1\",1,1.5,TRUE", 
    "\"2\",\"R2\",2,2.5,FALSE", 
    "\"3\",\"R3\",3,3.5,TRUE")))
}