library(hamcrest)

test.find.package <- function() {
    
    root <- find.package("stats")
    print(root)
    descriptionFile <- file.path(root, "NAMESPACE")
    
    assertTrue(file.exists(descriptionFile))
}