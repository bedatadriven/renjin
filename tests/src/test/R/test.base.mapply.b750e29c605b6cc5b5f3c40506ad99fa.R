library(hamcrest)

 
 
expected <- structure(list("coerce.oldClass#S3" = structure(c("oldClass", 
"S3"), .Names = c("from", "to")), initialize.oldClass = structure("oldClass", .Names = ".Object"), 
    show.oldClass = structure("oldClass", .Names = "object"), 
    slotsFromS3.oldClass = structure("oldClass", .Names = "object")), .Names = c("coerce.oldClass#S3", 
"initialize.oldClass", "show.oldClass", "slotsFromS3.oldClass"
)) 


assertThat(base:::mapply(FUN=function (elt, geom) 
elt[geom],structure(list(coerce = structure(list("oldClass#S3" = structure(c("oldClass", 
"S3"), .Names = c("from", "to"))), .Names = "oldClass#S3"), initialize = structure(list(
    oldClass = structure("oldClass", .Names = ".Object")), .Names = "oldClass"), 
    show = structure(list(oldClass = structure("oldClass", .Names = "object")), .Names = "oldClass"), 
    slotsFromS3 = structure(list(oldClass = structure("oldClass", .Names = "object")), .Names = "oldClass")), .Names = c("coerce", 
"initialize", "show", "slotsFromS3")),structure(list(coerce = structure(TRUE, .Names = "oldClass#S3"), 
    initialize = TRUE, show = TRUE, slotsFromS3 = TRUE), .Names = c("coerce", 
"initialize", "show", "slotsFromS3")))
,  equalTo( expected ) ) 
 

