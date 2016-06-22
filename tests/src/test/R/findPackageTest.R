library(hamcrest)

test.find.package <- function() {
    
    root <- find.package("stats")
    print(root)
    namespaceFile <- file.path(root, "NAMESPACE")
    
    assertTrue(file.exists(namespaceFile))
    
    
}