library(hamcrest)

 
 
expected <- structure(list("coerce.oldClass#S3" = "", initialize.oldClass = "", 
    show.oldClass = "", slotsFromS3.oldClass = ""), .Names = c("coerce.oldClass#S3", 
"initialize.oldClass", "show.oldClass", "slotsFromS3.oldClass"
)) 


assertThat(base:::mapply(FUN=function (elt, geom) 
elt[geom],structure(list(coerce = structure(list("oldClass#S3" = ""), .Names = "oldClass#S3"), 
    initialize = structure(list(oldClass = ""), .Names = "oldClass"), 
    show = structure(list(oldClass = ""), .Names = "oldClass"), 
    slotsFromS3 = structure(list(oldClass = ""), .Names = "oldClass")), .Names = c("coerce", 
"initialize", "show", "slotsFromS3")),structure(list(coerce = structure(TRUE, .Names = "oldClass#S3"), 
    initialize = TRUE, show = TRUE, slotsFromS3 = TRUE), .Names = c("coerce", 
"initialize", "show", "slotsFromS3")))
,  equalTo( expected ) ) 
 

