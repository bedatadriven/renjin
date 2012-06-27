

z.score <- function(data, m=mean(data), stdev=sd(data)) {
              # these two lines force the promises, allowing us to fuse m and stdev
              # otherwise they are separated by the barrier (data-m), and don't fuse.
              # can we do better?
              m
              stdev
              (data-m) / stdev
      }

outliers <- function(data, ignore) {
        use <- !ignore(data)
        z <- z.score(data, mean(data[use]), sd(data[use]))
        sum(abs(z) > 1)
}


cleaning1 <- newBenchmark("Data cleaning pipeline",
 init = {
	data <- as.double(1:20000000)
 },
 run = {
        outliers(data, function(x) { is.na(x) | x==9999 })
       }
)

registerBenchmarkSuite(
   name="Data Cleaning Pipeline",
   source="riposte",
   description="Vector pipeline",
   benchmarks = list(cleaning1) )

