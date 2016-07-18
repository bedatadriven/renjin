library(hamcrest)

 
 
expected <- c(2.22044604925031e-16+0i, 1.5-3i, 1.5+3i) 


assertThat(stats:::fft(z=c(1, 1.23205080756888, -2.23205080756888))
,  equalTo( expected ) ) 
 

