library(hamcrest)

 
 
expected <- structure(list(Depth = 1:8), .Names = "Depth") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Depth = 1:8), .Names = "Depth"),list(1:8))
,  equalTo( expected ) ) 
 

