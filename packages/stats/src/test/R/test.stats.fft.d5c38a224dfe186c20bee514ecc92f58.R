library(hamcrest)

 expected <- c(0x1.8p+1 + 0x0p+0i, -0x1.2p-47 + 0x0p+0i, -0x1.7fffffffffffcp+3 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(-3+0i, 3-3.46410161513775i, 3+3.46410161513775i))
,  identicalTo( expected, tol = 1e-6 ) )
