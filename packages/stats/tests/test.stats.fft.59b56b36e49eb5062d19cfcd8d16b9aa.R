library(hamcrest)

 expected <- c(-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i, 
-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i, 
-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i, 
-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i, 
-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i, 
-0x1.ccccccccccccdp-1 + 0x0p+0i, -0x1.ccccccccccccdp-1 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(-0.9+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i))
,  identicalTo( expected, tol = 1e-6 ) )
