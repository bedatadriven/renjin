library(hamcrest)

 
 
expected <- structure(list(year = 1:2, site = 1:6), .Names = c("year", "site"
)) 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(year = 1:2, site = 1:6), .Names = c("year", "site"
)),list(1:2, 1:6))
,  equalTo( expected ) ) 
 

