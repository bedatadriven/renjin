library(hamcrest)

 
 
expected <- structure(list("  " = character(0), "  " = character(0), "  " = character(0), 
    "  " = character(0), "  " = character(0), "  " = character(0)), .Names = c("  ", 
"  ", "  ", "  ", "  ", "  ")) 


assertThat(base:::mapply(FUN=.Primitive("rep"),c("  ", "  ", "  ", "  ", "  ", "  "),c(0, 0, 0, 0, 0, 0))
,  equalTo( expected ) ) 
 

