library(hamcrest)

 
 
expected <- structure(list(time = 1:4), .Names = "time") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(time = 1:4), .Names = "time"),list(1:4))
,  equalTo( expected ) ) 
 

