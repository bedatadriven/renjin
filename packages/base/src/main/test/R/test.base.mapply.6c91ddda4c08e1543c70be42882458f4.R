library(hamcrest)

 
 
expected <- structure(list("equal.count(time(sunspot.year))" = 1:6), .Names = "equal.count(time(sunspot.year))") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list("equal.count(time(sunspot.year))" = 1:6), .Names = "equal.count(time(sunspot.year))"),list(1:6))
,  equalTo( expected ) ) 
 

