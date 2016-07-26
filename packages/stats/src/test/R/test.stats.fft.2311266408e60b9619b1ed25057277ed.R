library(hamcrest)

 expected <- c(0x1.ap+3 + 0x0p+0i, 0x1.2db3d742c2655p+3 + 0x0p+0i, -0x1.1b67ae8584caap+2 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(6+0i, 3.5-4i, 3.5+4i))
,  identicalTo( expected, tol = 1e-6 ) )
