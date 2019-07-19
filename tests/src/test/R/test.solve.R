library(hamcrest)

mt <- function(s) {
    rows <- strsplit(s, "\n")[[1]]
    values <- as.numeric(strsplit(s, "\\s+")[[1]])

    matrix(values, byrow = TRUE, nrow = length(rows))
}


test.square <- function() {
    hilbert <- function(n) { i <- 1:n; 1 / outer(i - 1, i, "+") }
    h8 <- hilbert(8)
    sh8 <- solve(h8)


    expected <- mt(
             "64     -2016      20160     -92400      221760     -288288      192192     -51480
           -2016     84672    -952560    4656960   -11642400    15567552   -10594584    2882880
           20160   -952560   11430720  -58212000   149688000  -204324119   141261119  -38918880
          -92400   4656960  -58212000  304919999  -800414996  1109908794  -776936155  216215998
          221760 -11642400  149688000 -800414996  2134439987 -2996753738  2118916783 -594593995
         -288288  15567552 -204324119 1109908793 -2996753738  4249941661 -3030050996  856215352
          192192 -10594584  141261119 -776936154  2118916782 -3030050996  2175421226 -618377753
          -51480   2882880  -38918880  216215998  -594593995   856215351  -618377753  176679358")


    assertThat(solve(h8), identicalTo(expected, tol = 1))
}

test.simple <- function() {

    a <- mt("1  1  1
             0  2  5
             2  5 -1")

    b <- c(6, -4, 27)


    assertThat(solve(a, b), identicalTo(c(5, 3, -2)))
}

test.names <- function() {
   a <- mt("1  1  1
            0  2  5
            2  5 -1")

   dimnames(a) <- list(c("x", "y", "z"), c("a", "b", "c"))

   b <- c(6, -4, 27)

   assertThat(solve(a, b), identicalTo(c(a = 5, b = 3, c = -2)))

}

test.dimnames <- function() {
   a <- mt("1  1  1
            0  2  5
            2  5 -1")

   dimnames(a) <- list(c("x", "y", "z"), c("a", "b", "c"))

   b <- matrix(c(6, -4, 27), ncol = 1)

   assertThat(solve(a, b), identicalTo(structure(c(5, 3, -2), .Dim = c(3L, 1L), .Dimnames = list(c("a",  "b", "c"), NULL))))

}