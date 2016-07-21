library(hamcrest)

 
 
expected <- structure(list(which = 1:4), .Names = "which") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(which = 1:4), .Names = "which"),list(1:4))
,  equalTo( expected ) ) 
 

