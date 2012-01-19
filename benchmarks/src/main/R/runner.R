

cat("Starting benchmarks...\n")

warmupRuns <- 3
runs <- 1
profile <- FALSE
pattern <- c()


if(exists("benchmarkArgs")) {
	for(arg in benchmarkArgs) {
		if(arg == "profile") {
			profile <- TRUE
		} else {
			pattern <- c(pattern, arg)
		}	  
	}
}

if(length(pattern) == 0) {
	pattern <- ".*"
	cat("Running all benchmarks.\n")
} else {
	cat(c("Limiting benchmarks to patterns ", paste(pattern, sep=","), "\n"))
}

suites <- list()

registerBenchmarkSuite <- function(...) {
	suites[[length(suites)+1]] <<- list(...)
}

benchmark <- function(fn, name) {
	list(fn=fn, name=name)
}

newBenchmark <- function(name, run, init={}, enclosure=parent.frame() ) {
	list(name=name, init=substitute(init), run=substitute(run), enclosure=enclosure)
}

loadBenchmarks <- function(script) {
  cat(c("Loading benchmarks from ", script, "\n"))
  
  # load script into this environment
  source(script, local=TRUE)

}

benchmarkScripts <- list.files("src/main/R/benchmarks", full.names=TRUE)

for(benchmarkScript in benchmarkScripts) {
  loadBenchmarks(benchmarkScript)
}

cat(c(length(suites), " benchmark suites have been registered\n"));

timeBenchmark <- function(benchmark) {
	cumulate <- 0; b <- 0
	for (i in 1:(warmupRuns+runs)) {
	  invisible(gc())
	  env <- new.env(parent=benchmark$enclosure)
	  eval(benchmark$init, env)
	  timing <- system.time({
	    b <- eval(benchmark$run, env)
	  })[3]
	  if(i > warmupRuns) {
	  	cumulate <- cumulate + timing
	  }
	}
	remove("b")
	cumulate/runs
}

repeat {
	
	for(suite in suites) {
	
	  cat(c("\nSuite: ", suite$name, "\n"));
	  
	  for(benchmark in suite$benchmarks) {
	  	if(any(grepl(pattern, benchmark$name, ignore.case=TRUE))) {	
		  	timing <- timeBenchmark(benchmark)
		  	cat(c(format(benchmark$name, width=60), ": ", format(timing,digits=2), "\n"))
		}
	  }
	}
	
	if(!profile) {
		break
	}
}
