library(hamcrest)

 expected <- structure(
     list(
         breaks = c(0x1.9p+6, 0x1.2cp+8, 0x1.f4p+8, 0x1.5ep+9),
         counts = c(12L, 9L, 3L),
         density = c(0x1.47ae147ae147bp-9, 0x1.eb851eb851eb8p-10, 0x1.47ae147ae147bp-11),
         mids = c(0x1.9p+7, 0x1.9p+8, 0x1.2cp+9),
         xname = "structure(c(112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118, 463, 407, 362, 405, 417, 391, 419, 461, 472, 535, 622, 606), .Tsp = c(1949, 1950.91666666667, 12), class = \"ts\")",
         equidist = TRUE
         ),
     .Names = c("breaks", "counts", "density", "mids", "xname", "equidist"),
     class = "histogram"
     )


assertThat(graphics:::hist.default(breaks=c(100, 300, 500, 700),plot=FALSE,x=structure(c(
112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
463, 407, 362, 405, 417, 391, 419, 461, 472, 535, 622, 606
), .Tsp = c(1949, 1950.91666666667, 12), class = "ts"))[-5]
,  identicalTo( expected[-5] ) )