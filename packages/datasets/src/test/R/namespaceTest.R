
library(hamcrest)


# GNU R allows the use of the :: operator to access data

assertTrue(length(datasets::airmiles) == 24)

# This is because the dataset actually loads the datesets into the namespace environment

assertTrue(exists("airmiles", envir=getNamespace("datasets")))