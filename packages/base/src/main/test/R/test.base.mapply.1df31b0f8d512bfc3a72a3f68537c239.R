library(hamcrest)

 
 
expected <- structure(list(Subject = 1:27), .Names = "Subject") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Subject = 1:27), .Names = "Subject"),list(1:27))
,  equalTo( expected ) ) 
 

