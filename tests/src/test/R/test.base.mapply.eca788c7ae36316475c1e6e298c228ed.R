library(hamcrest)

 
 
expected <- structure(list("  " = character(0), "  " = "  ", "  " = c("  ", 
"  "), "  " = character(0), "  " = "  ", "  " = c("  ", "  ")), .Names = c("  ", 
"  ", "  ", "  ", "  ", "  ")) 


assertThat(base:::mapply(FUN=.Primitive("rep"),c("  ", "  ", "  ", "  ", "  ", "  "),c(0, 1, 2, 0, 1, 2))
,  equalTo( expected ) ) 
 

