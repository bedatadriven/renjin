library(hamcrest)

 expected <- c(0x1.999999999999ap-2 + 0x0p+0i, 0x1.8ba4a9f6d65abp-2 + 0x0p+0i, 
0x1.62b9586ad0a23p-2 + 0x0p+0i, 0x1.21a1851ff630bp-2 + 0x0p+0i, 
0x1.999999999999ap-3 + -0x1p-56i, 0x1.a80c935b80a81p-4 + 0x0p+0i, 
0x1.5db3d742c2656p-55 + 0x1.76cf5d0b09952p-57i, -0x1.a80c935b80a7fp-4 + 0x1p-56i, 
-0x1.999999999999ap-3 + 0x1.4p-54i, -0x1.21a1851ff630ap-2 + 0x1.8p-54i, 
-0x1.62b9586ad0a23p-2 + 0x1p-54i, -0x1.8ba4a9f6d65abp-2 + 0x1p-53i, 
-0x1.999999999999ap-2 + 0x0p+0i, -0x1.8ba4a9f6d65abp-2 + 0x0p+0i, 
-0x1.62b9586ad0a23p-2 + 0x0p+0i, -0x1.21a1851ff630bp-2 + 0x0p+0i, 
-0x1.999999999999ap-3 + 0x1p-56i, -0x1.a80c935b80a81p-4 + 0x0p+0i, 
-0x1.5db3d742c2656p-55 + -0x1.76cf5d0b09952p-57i, 0x1.a80c935b80a7fp-4 + -0x1p-56i, 
0x1.999999999999ap-3 + -0x1.4p-54i, 0x1.21a1851ff630ap-2 + -0x1.8p-54i, 
0x1.62b9586ad0a23p-2 + -0x1p-54i, 0x1.8ba4a9f6d65abp-2 + -0x1p-53i
) 
 

assertThat(stats:::fft(inverse=TRUE,z=c(0+0i, 0.2+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i, 0.2+0i))
,  identicalTo( expected, tol = 1e-6 ) )
