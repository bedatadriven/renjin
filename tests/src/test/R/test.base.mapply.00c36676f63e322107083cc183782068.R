library(hamcrest)

 
 
expected <- structure(list(EE = 1:9), .Names = "EE") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(EE = 1:9), .Names = "EE"),list(1:9))
,  equalTo( expected ) ) 
 

