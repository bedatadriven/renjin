
library(datasets)
library(utils)

data(mtcars)

stopifnot(identical(colnames(mtcars), 
    c("mpg", "cyl", "disp", "hp", "drat", "wt", "qsec", "vs", "am", "gear", "carb")))