library(hamcrest)

 
 
expected <- structure(list(Age = 1:2, Survived = 1L), .Names = c("Age", "Survived"
)) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Age = 1:2, Survived = 1:2), .Names = c("Age", 
"Survived")),list(1:2, 1L))
,  equalTo( expected ) ) 
 

