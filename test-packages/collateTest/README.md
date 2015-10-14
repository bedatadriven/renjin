
Test package that ensures that the evaluation order of source files can be specified 
via the `sourceFiles` property of the namespace-compile goal. 

For the most part, package sources merely define functions, such as:

```R
f <- function(x) g(x)
g <- function(x) x*42
```

In those cases, evaluation order has no effect, because the symbols within a function body are only
evaluated at the moment of a function call.

For packages that define S4 classes, however, evaluation order is important. 

For example, `Reporter.R` might contain:

```
setClass("Reporter")
```

and `TestReporter.R` might contain:

```
setClass("TestReporter", contains="Reporter")
```

in this case, `Reporter.R` *must* be evaluated before `TestReporter.R` or else `TestReporter.R` will fail. 

GNU R `DESCRIPTION` files include a `Collate` field which allows package authors to specify the evaluation
order. The `sourceFiles` maven property is Renjin's equivalent. 



