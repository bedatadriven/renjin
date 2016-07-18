library(hamcrest)

 
 
expected <- structure(list(site = 1:6), .Names = "site") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(site = 1:6), .Names = "site"),list(1:6))
,  equalTo( expected ) ) 
 

