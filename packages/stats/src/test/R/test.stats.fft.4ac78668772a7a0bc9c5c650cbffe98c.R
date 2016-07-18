library(hamcrest)

 
 
expected <- c(27+0i, 15-16.5i, 15+16.5i) 


assertThat(stats:::fft(z=c(19, 13.5262794416288, -5.52627944162883))
,  equalTo( expected ) ) 
 

