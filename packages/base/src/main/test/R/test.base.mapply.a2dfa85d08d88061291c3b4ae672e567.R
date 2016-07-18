library(hamcrest)

 
 
expected <- list(1L) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,list(1L),list(1L))
,  equalTo( expected ) ) 
 

