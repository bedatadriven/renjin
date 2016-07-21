library(hamcrest)

 expected <- c(-1.3+0i, -1.21961524227066+0i, -1+0i, -0.7-0i, -0.4-0i, -0.180384757729337-0i, 
-0.1+0i, -0.180384757729337+0i, -0.4-0i, -0.7+0i, -1+0i, -1.21961524227066+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(-0.7+0i, -0.3+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, -0.3+0i))
,  identicalTo( expected, tol = 1e-6 ) )
