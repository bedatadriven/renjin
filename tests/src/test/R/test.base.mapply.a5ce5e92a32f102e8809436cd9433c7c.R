library(hamcrest)

 
 
expected <- structure(list(Temperature = 1:4, Wind = 1:4), .Names = c("Temperature", 
"Wind")) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Temperature = 1:4, Wind = 1:4), .Names = c("Temperature", 
"Wind")),list(1:4, 1:4))
,  equalTo( expected ) ) 
 

