mean.online <- function(x) {
    xbar <- x[1]

    for(n in 2:length(x)) {
        xbar <- ((n - 1) * xbar + x[n]) / n
    }

    xbar
}

### 
x <- log(seq(10e6))
