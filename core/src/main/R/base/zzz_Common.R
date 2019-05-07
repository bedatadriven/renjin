
# Renjin added from src/library/profile/Common.R
# NOTE(ab) these seem pretty essential to the language so it's not clear to me why
# they should be in (nominally) configurable system profile
.GlobalEnv <- globalenv()
.AutoloadEnv <- as.environment(2)
assign(".Autoloaded", NULL, envir = .AutoloadEnv)
T <- TRUE
F <- FALSE
R.version <- structure(R.Version(), class = "simple.list")
version <- R.version            # for S compatibility

.onLoad <- function() {

	options(keep.source = interactive())
	options(warn = 0)
	options(timeout = 60)
	options(encoding = "native.enc")
	options(show.error.messages = TRUE)
	## keep in sync with PrintDefaults() in  ../../main/print.c :
	options(scipen = 0)
	options(max.print = 99999)# max. #{entries} in internal printMatrix()
	options(add.smooth = TRUE)# currently only used in 'plot.lm'
	options(stringsAsFactors = TRUE)
	if(!interactive() && is.null(getOption("showErrorCalls")))
		options(showErrorCalls = TRUE)


    makeActiveBinding(".Options", function() {
     as.pairlist(options())
    }, baseenv())
}
