library(hamcrest)

 expected <- c(0x1.199999999999ap+0 + 0x0p+0i, 0x1.0f4f8d60d3eb4p+0 + 0x0p+0i, 
0x1.e666666666667p-1 + -0x1p-56i, 0x1.999999999999ap-1 + 0x1.76cf5d0b09952p-57i, 
0x1.4cccccccccccdp-1 + 0x1.8p-55i, 0x1.149418718b5cep-1 + 0x1.8p-54i, 
0x1p-1 + 0x0p+0i, 0x1.149418718b5cdp-1 + 0x0p+0i, 0x1.4cccccccccccdp-1 + 0x1p-56i, 
0x1.999999999999ap-1 + -0x1.76cf5d0b09952p-57i, 0x1.e666666666667p-1 + -0x1.8p-55i, 
0x1.0f4f8d60d3eb3p+0 + -0x1.8p-54i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0.8+0i, 0.15+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0.15+0i))
,  identicalTo( expected, tol = 1e-6 ) )
