library(grid)
library(lattice)
data(iris)
iris2 <- iris[,1:4]

print(splom(iris2, groups = iris$Species,
      pscales = 10))
