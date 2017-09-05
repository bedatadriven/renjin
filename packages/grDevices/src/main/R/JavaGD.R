

JavaGD <- function(name="JavaGD", width=400, height=300, ps=12) {
  invisible(.Call(C_newJavaGD, name, width, height, ps))
}
