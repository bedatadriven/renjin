## Generate R function implementations for 
## statistical distributions

## Implementations of these functions are in the 
## renjin-nmath library.

dists <- list(
  beta = c("shape1", "shape2"),
  nbeta = c("shape1", "shape2", "ncp"),
  binom = c("size",  "prob"),
  nbinom = c("size", "prob"),
  nbinom_mu = c("size", "mu"),
  cauchy = c("location", "scale"),
  chisq = c("df"),
  nchisq = c("df", "ncp"),
  exp = c("scale"),
  f = c("df1", "df2"),
  nf = c("df1", "df2", "ncp"),
  gamma = c("shape", "scale"),
  geom = c("prob"),
  hyper = c("m", "n", "k"),
  lnorm =  c("meanlog", "sdlog"),
  logis = c("location", "scale"),
#  multinom = c("size", "prob"),
  norm = c("mean", "sd"),
  pois = c("lambda"),
  signrank = c("n"), 
  t = c("df"),
  tukey = c("nranges", "nmeans", "df"),
  nt = c("df", "ncp"),
  unif = c("min", "max"),
  weibull = c("shape",  "scale"),
  wilcox = c("m", "n"))

className <- function(prefix, dist) {
  if(dist %in% c("signrank", "wilcox")) {
    dist
  } else if(grepl(dist, pattern = "_mu$")) {
    paste0(prefix, substr(dist, 1, nchar(dist)-3))
  } else {
    paste0(prefix, dist)
  }
}

fname <- function(prefix, dist) {
  fn <- paste0(prefix, dist)
  if(fn == "dnorm") {
    "dnorm4"
  } else if(fn == "pnorm") {
    "pnorm5"
  } else if(fn == "qnorm") {
    "qnorm5"
  } else {
    fn
  }
}

declList <- function(arg, params, flags) {
  argDecl <- paste("double", arg)
  paramDecls <- paste("@Recycle double", params)
  flagDecls <- paste("boolean", flags)
  
  paste(c(argDecl, paramDecls, flagDecls), collapse = ", ")
}

argList <- function(arg, params, flags) {
  paste(c(arg, params, sprintf("toInt(%s)", flags)), collapse = ", ")
}

writeFn <- function(con, dist, prefix, arg, params, flags) {
  fn <- sprintf("%s%s", prefix, dist)
  cat(file = con, sprintf("  @DataParallel @Internal\n"))
  cat(file = con, sprintf("  public static double %s%s(%s) {\n", prefix, dist, declList(arg, params, flags)))
  cat(file = con, sprintf("    return org.renjin.nmath.%s.%s(%s);\n", 
                          className(prefix, dist), 
                          fname(prefix, dist), 
                          argList(arg, params, flags)))
  cat(file = con, sprintf("  }\n"))
}

writeRandomFn <- function(con, dist, params) {
  fn <- sprintf("%s%s", prefix, dist)
  
  paramDecls <- paste("AtomicVector", params, collapse = ", ")
  
  cat(file = con, sprintf("  @Internal\n"))
  cat(file = con, sprintf("  public static double[] r%s(int length, %s) {\n", prefix, dist, paramDecls))
  cat(file = con, sprintf("    double[] result = new double[length];\n"))
  cat(file = con, sprintf("    for(int i = 0; i < length; ++i) {"))
  cat(file = con, sprintf("      result[i] = org.renjin.nmath."))
  
  
          
  cat(file = con, sprintf("    return org.renjin.nmath.%s.%s(%s);\n", 
                          className(prefix, dist), 
                          fname(prefix, dist), 
                          argList(arg, params, flags)))
  cat(file = con, sprintf("  }\n"))
  
}

writeDistFns <- function() {
  src <- file("core/src/main/java/org/renjin/stats/internals/Distributions.java", open = "wt")
  cat(file = src, "package org.renjin.stats.internals;\n\n")
  cat(file = src, "import org.renjin.invoke.annotations.*;\n")

  cat(file = src, "public class Distributions {\n")
  cat(file = src, "  public static int toInt(boolean x) { return x ? 1 : 0; }\n\n")
  for(dist.name in names(dists)) {
    params <- dists[[dist.name]]
    
    writeFn(src, dist.name, "p", "q", params, c("lowerTail", "logP"))
    writeFn(src, dist.name, "q", "p", params, c("lowerTail", "logP"))

    if(! (dist.name %in% c("tukey"))) {
      writeFn(src, dist.name, "d", "x", params, c("log"))
    }
    
  }
  cat(file = src, "}")
  close(src)
}
