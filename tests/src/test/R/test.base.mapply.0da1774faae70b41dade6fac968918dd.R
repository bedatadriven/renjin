library(hamcrest)

 
 
expected <- structure(list(Species = 1:3), .Names = "Species") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Species = 1:3), .Names = "Species"),list(1:3))
,  equalTo( expected ) ) 
 

