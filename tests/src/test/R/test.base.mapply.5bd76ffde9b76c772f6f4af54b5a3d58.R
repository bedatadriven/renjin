library(hamcrest)

 
 
expected <- structure(list(HP = 1:6, "factor(cyl)" = 1:3), .Names = c("HP", 
"factor(cyl)")) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(HP = 1:6, "factor(cyl)" = 1:3), .Names = c("HP", 
"factor(cyl)")),list(1:6, 1:3))
,  equalTo( expected ) ) 
 

