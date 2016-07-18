library(hamcrest)

 
 
expected <- structure(list(d = 1:14), .Names = "d") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(d = 1:14), .Names = "d"),list(1:14))
,  equalTo( expected ) ) 
 

