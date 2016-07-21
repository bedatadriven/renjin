library(hamcrest)

 expected <- c(0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 
0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 
0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i, 0.5+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0.5+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0+0i))
,  identicalTo( expected, tol = 1e-6 ) )
