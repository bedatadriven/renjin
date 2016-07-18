library(hamcrest)

 
 
expected <- structure(list("equal.count(stations)" = 1:6), .Names = "equal.count(stations)") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list("equal.count(stations)" = 1:6), .Names = "equal.count(stations)"),list(1:6))
,  equalTo( expected ) ) 
 

