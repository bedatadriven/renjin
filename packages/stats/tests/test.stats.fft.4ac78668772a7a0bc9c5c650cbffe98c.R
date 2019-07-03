library(hamcrest)

 expected <- c(0x1.afffffffffff8p+4 + 0x0p+0i, 0x1.e000000000008p+3 + -0x1.07ffffffffffbp+4i, 
0x1.e000000000008p+3 + 0x1.07ffffffffffbp+4i) 
 

assertThat(stats:::fft(z=c(19, 13.5262794416288, -5.52627944162883))
,  identicalTo( expected, tol = 1e-6 ) )
