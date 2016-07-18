library(hamcrest)

 
 
expected <- c(9+0i, 6-7.49999999999999i, 6+7.49999999999999i) 


assertThat(stats:::fft(z=c(7, 5.33012701892219, -3.33012701892219))
,  equalTo( expected ) ) 
 

