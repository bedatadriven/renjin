library(hamcrest)

 
 
expected <- structure(list(vp.comb = 1:4), .Names = "vp.comb") 


assertThat(base:::mapply(FUN="[",MoreArgs=structure(list(drop = FALSE), .Names = "drop"),SIMPLIFY=FALSE,structure(list(vp.comb = 1:4), .Names = "vp.comb"),list(1:4))
,  equalTo( expected ) ) 
 

