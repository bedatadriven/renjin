

f <- function(z) {
    s <- 0
    for(i in z) {   
        s <- s + (sqrt(i) * (1/length(z)))
    }
    print(s)
}

print(system.time(f(1:1e6)))
print(system.time(f(1:1e8)))

