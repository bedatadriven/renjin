

.Library <- file.path(R.home(), "library")
.Library.site <- Sys.getenv("R_LIBS_SITE")
.Library.site <- if(!nchar(.Library.site)) file.path(R.home(), "site-library") else unlist(strsplit(.Library.site, ":"))
.Library.site <- .Library.site[file.exists(.Library.site)]

invisible(.libPaths(c(unlist(strsplit(Sys.getenv("R_LIBS"), ";")),	
						unlist(strsplit(Sys.getenv("R_LIBS_USER"), ";")
						))))

