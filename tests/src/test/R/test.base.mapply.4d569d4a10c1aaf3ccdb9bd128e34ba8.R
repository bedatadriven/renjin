library(hamcrest)

 
 
expected <- structure(list("  " = character(0), "  " = character(0), "  " = "  ", 
    "  " = c("  ", "  "), "  " = character(0), "  " = "  ", "  " = c("  ", 
    "  "), "  " = c("  ", "  ")), .Names = c("  ", "  ", "  ", 
"  ", "  ", "  ", "  ", "  ")) 


assertThat(base:::mapply(FUN=.Primitive("rep"),c("  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "),c(0, 0, 1, 2, 0, 1, 2, 2))
,  equalTo( expected ) ) 
 

