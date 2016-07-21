library(hamcrest)

 expected <- c(1.1+0i, 1.05980762113533+0i, 0.95-0i, 0.8+0i, 0.65+0i, 0.540192378864669+0i, 
0.5+0i, 0.540192378864668+0i, 0.65+0i, 0.8-0i, 0.95-0i, 1.05980762113533-0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0.8+0i, 0.15+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0.15+0i))
,  identicalTo( expected, tol = 1e-6 ) )
