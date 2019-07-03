library(hamcrest)

 expected <- c(-0x1.4ccccccccccccp+0 + 0x0p+0i, -0x1.3838b45b417p+0 + 0x0p+0i, 
-0x1p+0 + 0x1p-55i, -0x1.6666666666667p-1 + -0x1.76cf5d0b09952p-56i, 
-0x1.9999999999999p-2 + -0x1.8p-54i, -0x1.716d905927b34p-3 + -0x1.8p-53i, 
-0x1.9999999999998p-4 + 0x0p+0i, -0x1.716d905927b3p-3 + 0x0p+0i, 
-0x1.9999999999999p-2 + -0x1p-55i, -0x1.6666666666665p-1 + 0x1.76cf5d0b09952p-56i, 
-0x1p+0 + 0x1.8p-54i, -0x1.3838b45b417p+0 + 0x1.8p-53i) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(-0.7+0i, -0.3+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, -0.3+0i))
,  identicalTo( expected, tol = 1e-6 ) )
