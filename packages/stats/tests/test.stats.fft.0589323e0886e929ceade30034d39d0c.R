library(hamcrest)

 expected <- c(0x1.2p+3 + 0x0p+0i, 0x1.8p+2 + -0x1.dfffffffffff9p+2i, 0x1.8p+2 + 0x1.dfffffffffff9p+2i
) 
 

assertThat(stats:::fft(z=c(7, 5.33012701892219, -3.33012701892219))
,  identicalTo( expected, tol = 1e-6 ) )
