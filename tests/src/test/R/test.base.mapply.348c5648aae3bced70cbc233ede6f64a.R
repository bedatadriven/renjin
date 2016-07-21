library(hamcrest)

 
 
expected <- list(1L) 


assertThat(base:::mapply(FUN="[",SIMPLIFY=FALSE,list(1L),list(1L))
,  equalTo( expected ) ) 
 

