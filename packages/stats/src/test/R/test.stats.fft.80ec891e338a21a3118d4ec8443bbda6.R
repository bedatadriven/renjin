library(hamcrest)

 expected <- c(0x1p-52 + 0x0p+0i, 0x1.8p+0 + -0x1.800000000000ap+1i, 0x1.8p+0 + 0x1.800000000000ap+1i
) 
 

assertThat(stats:::fft(z=c(1, 1.23205080756888, -2.23205080756888))
,  identicalTo( expected, tol = 1e-6 ) )
