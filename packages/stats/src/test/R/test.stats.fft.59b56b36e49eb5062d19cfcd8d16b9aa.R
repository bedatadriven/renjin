library(hamcrest)

 
 
expected <- c(-0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i, 
-0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i, -0.9+0i) 


assertThat(stats:::fft(inverse=TRUE,z=c(-0.9+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 0+0i, 
0+0i, 0+0i))
,  equalTo( expected ) ) 
 

