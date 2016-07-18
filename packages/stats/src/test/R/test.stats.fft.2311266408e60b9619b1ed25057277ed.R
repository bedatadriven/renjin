library(hamcrest)

 
 
expected <- c(13+0i, 9.42820323027551+0i, -4.42820323027551+0i) 


assertThat(stats:::fft(inverse=TRUE,z=c(6+0i, 3.5-4i, 3.5+4i))
,  equalTo( expected ) ) 
 

