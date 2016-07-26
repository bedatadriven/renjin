library(hamcrest)

 expected <- c(0x1.2p+4 + 0x0p+0i, 0x1.5p+3 + -0x1.8000000000001p+3i, 0x1.5p+3 + 0x1.8000000000001p+3i
) 
 

assertThat(stats:::fft(z=c(13, 9.42820323027551, -4.42820323027551))
,  identicalTo( expected, tol = 1e-6 ) )
