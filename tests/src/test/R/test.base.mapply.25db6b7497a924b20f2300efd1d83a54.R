library(hamcrest)

 
 
expected <- structure(list(Age = 1:2, Survived = 1:2), .Names = c("Age", 
"Survived")) 


assertThat(base:::mapply(FUN="[",SIMPLIFY=FALSE,structure(list(Age = 1:2, Survived = 1:2), .Names = c("Age", 
"Survived")),list(1:2, 1:2))
,  equalTo( expected ) ) 
 

