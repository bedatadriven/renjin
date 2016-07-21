library(hamcrest)

 
 
expected <- structure(list(Sex = 1:2, Age = 1:2), .Names = c("Sex", "Age"
)) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(Sex = 1:2, Age = 1:2), .Names = c("Sex", "Age"
)),list(1:2, 1:2))
,  equalTo( expected ) ) 
 

