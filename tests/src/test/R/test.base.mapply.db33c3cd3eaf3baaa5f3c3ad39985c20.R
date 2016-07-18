library(hamcrest)

 
 
expected <- structure(list(state.region = 1:4), .Names = "state.region") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(state.region = 1:4), .Names = "state.region"),list(1:4))
,  equalTo( expected ) ) 
 

