
## This unexported function is defined in the GNU R tools package
## and is often referenced in NAMESPACE files to detect OS

.OStype <- function() {
  OS <- Sys.getenv("R_OSTYPE")
  if (nzchar(OS)) {
    OS
  } else {
    .Platform$OS.type
  }
}
