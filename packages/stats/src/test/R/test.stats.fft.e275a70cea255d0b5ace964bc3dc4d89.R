library(hamcrest)

 
 
expected <- c(18+0i, 10.5-12i, 10.5+12i) 


assertThat(stats:::fft(z=c(13, 9.42820323027551, -4.42820323027551))
,  equalTo( expected ) ) 
 

