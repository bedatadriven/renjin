library(hamcrest)

 
 
expected <- structure(list(f = 1:2), .Names = "f") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(f = 1:2), .Names = "f"),list(1:2))
,  equalTo( expected ) ) 
 

