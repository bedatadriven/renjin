library(hamcrest)

 expected <- c(4+0i, 8+0i, 12+0i, 16+0i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(10+0i, -2+2i, -2+0i, -2-2i))
,  identicalTo( expected, tol = 1e-6 ) )
