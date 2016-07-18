library(hamcrest)

 
 
expected <- structure(list(voice.part = 1:8), .Names = "voice.part") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(voice.part = 1:8), .Names = "voice.part"),list(1:8))
,  equalTo( expected ) ) 
 

