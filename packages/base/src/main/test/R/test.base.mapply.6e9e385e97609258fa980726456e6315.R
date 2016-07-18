library(hamcrest)

 
 
expected <- structure(list(title = "title = \"boot: Bootstrap R (S-PLUS) Functions\"", 
    author = c("author = c(person(given = \"Angelo\",", "                  family = \"Canty\",", 
    "                  role = \"aut\",", "                  comment = \"S original\"),", 
    "           person(given = c(\"Brian\", \"D.\"),", "                  family = \"Ripley\",", 
    "                  role = c(\"aut\", \"trl\", \"cre\"),", 
    "                  email = \"ripley@stats.ox.ac.uk\",", "                  comment = \"R port, author of parallel support\"))"
    ), year = "year = \"2012\"", note = "note = \"R package version 1.3-4\"", 
    url = "url = \"https://CRAN.R-project.org/package=boot\""), .Names = c("title", 
"author", "year", "note", "url")) 


assertThat(base:::mapply(FUN=function (u, v) 
{
    prefix <- sprintf("%s = ", u)
    n <- length(v)
    if (n > 1L) 
        prefix <- c(prefix, rep.int(strrep(" ", nchar(prefix)), 
            n - 1L))
    sprintf("%s%s", prefix, v)
},c("title", "author", "year", "note", "url"),structure(list(title = "\"boot: Bootstrap R (S-PLUS) Functions\"", 
    author = c("c(person(given = \"Angelo\",", "         family = \"Canty\",", 
    "         role = \"aut\",", "         comment = \"S original\"),", 
    "  person(given = c(\"Brian\", \"D.\"),", "         family = \"Ripley\",", 
    "         role = c(\"aut\", \"trl\", \"cre\"),", "         email = \"ripley@stats.ox.ac.uk\",", 
    "         comment = \"R port, author of parallel support\"))"
    ), year = "\"2012\"", note = "\"R package version 1.3-4\"", 
    url = "\"https://CRAN.R-project.org/package=boot\""), .Names = c("title", 
"author", "year", "note", "url")))
,  equalTo( expected ) ) 
 

