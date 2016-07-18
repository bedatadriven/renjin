library(hamcrest)

 
 
expected <- c(19+0i, 13.5262794416288+0i, -5.52627944162882+0i) 


assertThat(stats:::fft(inverse=TRUE,z=c(9+0i, 5-5.5i, 5+5.5i))
,  equalTo( expected ) ) 
 

