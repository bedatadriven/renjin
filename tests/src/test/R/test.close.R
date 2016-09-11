

test.close <- function() {
    
    filename <- tempfile()
    cat("hello\n", file = filename)
    
    for(i in 1:1000)  {
        con <- file(filename, open = "r")
        cat(readLines(con), "\n")
        close(con)
    }
}