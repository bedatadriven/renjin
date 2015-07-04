
## Test that exceeds possible memory assigned to JVM...

print("about to allocate gobs of memory!!")

x <- list()
for(i in 1:100000) {
    print(i)
    x[[i]] <- .Internal(runif(2^28, 0, 1))
}

