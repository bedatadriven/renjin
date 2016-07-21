library(hamcrest)

 expected <- c(7+0i, 5.33012701892219+0i, -3.33012701892219+0i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(3+0i, 2-2.5i, 2+2.5i))
,  identicalTo( expected, tol = 1e-6 ) )
