library(hamcrest)

 
 
expected <- list(c(1L, 1L, 1L)) 


assertThat(base:::mapply(FUN="[",SIMPLIFY=FALSE,list(1L),list(c(1, 1, 1)))
,  equalTo( expected ) ) 
 

