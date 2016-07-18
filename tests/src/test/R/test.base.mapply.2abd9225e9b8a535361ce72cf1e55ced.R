library(hamcrest)

 
 
expected <- structure(list(fac = 1:2), .Names = "fac") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(fac = 1:2), .Names = "fac"),list(1:2))
,  equalTo( expected ) ) 
 

