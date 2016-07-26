library(hamcrest)

 expected <- c(0x1.3p+4 + 0x0p+0i, 0x1.b0d747fbcb4b5p+3 + 0x0p+0i, -0x1.61ae8ff79696ap+2 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(9+0i, 5-5.5i, 5+5.5i))
,  identicalTo( expected, tol = 1e-6 ) )
