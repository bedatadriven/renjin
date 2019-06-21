library(hamcrest)

 expected <- c(0x1.cp+2 + 0x0p+0i, 0x1.5520cd1372feap+2 + 0x0p+0i, -0x1.aa419a26e5fd4p+1 + 0x0p+0i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(3+0i, 2-2.5i, 2+2.5i))
,  identicalTo( expected, tol = 1e-6 ) )
