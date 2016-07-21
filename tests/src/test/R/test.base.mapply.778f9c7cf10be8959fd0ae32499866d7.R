library(hamcrest)

 
 
expected <- structure(list(Age = 1:2, Survived = 1L), .Names = c("Age", "Survived"
)) 


assertThat(base:::mapply(FUN="[",SIMPLIFY=FALSE,structure(list(Age = 1:2, Survived = 1:2), .Names = c("Age", 
"Survived")),list(TRUE, 1))
,  equalTo( expected ) ) 
 

