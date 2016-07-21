library(hamcrest)

 expected <- c(3+0i, -7.99360577730113e-15+0i, -12+0i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(-3+0i, 3-3.46410161513775i, 3+3.46410161513775i))
,  identicalTo( expected, tol = 1e-6 ) )
