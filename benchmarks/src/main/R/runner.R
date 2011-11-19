
cat("Starting benchmarks...\n")

runBenchmark <- function(script) {
  cat("Starting benchmark ")
  cat(script)
  cat("\n")
  
  startTime <- Sys.time()
  
  # load script into this environment
  source(script, local=TRUE)
  
  finishTime <- Sys.time()
  runningTime <- finishTime-startTime
  
  cat("finished in ")
  cat(runningTime)
  cat("\n")
  
  list(script=script, runningTime=(finishTime-startTime))
}

benchmarkScripts <- list.files("src/main/R/benchmarks", full.names=TRUE)
cat("Found ") 
cat(length(benchmarkScripts)) 
cat(" benchmarks...\n")

print(typeof(benchmarkScripts))

for(benchmarkScript in benchmarkScripts) {
  cat("benchmarkScript = ")
  cat(benchmarkScript)
  cat("\n")
  runBenchmark(benchmarkScript)
}
