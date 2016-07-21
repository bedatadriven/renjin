library(hamcrest)

 expected <- c(1+0i, 1.23205080756888+0i, -2.23205080756888+0i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0+0i, 0.5-1i, 0.5+1i))
,  identicalTo( expected, tol = 1e-6 ) )
