Hilbert <- function(n)
{   ## generate the Hilbert matrix of dimension n
    n <- as.integer(n)
    i <- seq_len(n)
    new("dpoMatrix", x = c(1/outer(i - 1L, i, "+")), Dim = c(n,n))
}
