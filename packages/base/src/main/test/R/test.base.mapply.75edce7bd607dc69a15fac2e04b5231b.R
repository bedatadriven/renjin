library(hamcrest)

 
 
expected <- structure(list(radiation = 1:4), .Names = "radiation") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(radiation = 1:4), .Names = "radiation"),list(1:4))
,  equalTo( expected ) ) 
 

