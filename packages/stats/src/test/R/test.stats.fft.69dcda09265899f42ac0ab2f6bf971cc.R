library(hamcrest)

 expected <- c(4+0i, 16+0i, 28+0i, 24+0i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(18+0i, -6+2i, -2+0i, -6-2i))
,  identicalTo( expected, tol = 1e-6 ) )
