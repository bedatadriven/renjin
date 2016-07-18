library(hamcrest)

 
 
expected <- list(1:2) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,list(1:2),list(1:2))
,  equalTo( expected ) ) 
 

