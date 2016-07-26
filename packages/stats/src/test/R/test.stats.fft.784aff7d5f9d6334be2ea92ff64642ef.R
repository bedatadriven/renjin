library(hamcrest)

 expected <- c(0x1p+0 + 0x0p+0i, 0x1.3b67ae8584caap+0 + 0x0p+0i, -0x1.1db3d742c2655p+1 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0+0i, 0.5-1i, 0.5+1i))
,  identicalTo( expected, tol = 1e-6 ) )
