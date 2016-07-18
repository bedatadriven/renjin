library(hamcrest)

 
 
expected <- list(c(1L, 1L, 1L, 1L, 1L, 1L, 1L)) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,list(1L),list(c(1L, 1L, 1L, 1L, 1L, 1L, 1L)))
,  equalTo( expected ) ) 
 

