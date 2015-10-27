
# Math Routine Micro Benchmarks

This module includes a number of micro benchmark experiments useful for tuning the math routines used
by Renjin, and comparing performance across releases. These benchmarks rely on the 
[Java Microbenchmark Harness](http://openjdk.java.net/projects/code-tools/jmh/) library.

## Running

```
mvn clean install
java -jar target/benchmarks.jar DgemmBenchmark -i 10 -f 1

```

