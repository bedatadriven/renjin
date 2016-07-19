

test.qr <- function() {

    hilbert <- function(n) { i <- 1:n; 1 / outer(i - 1, i, "+") }
    h3 <- hilbert(3)
    print(qr(h3))

}