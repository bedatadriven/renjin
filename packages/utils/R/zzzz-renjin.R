



flush.console <- function() invisible()

process.events <- function() invisible()

installed.packages <- function (lib.loc = NULL, priority = NULL, noCache = FALSE, fields = NULL,
                          subarch = .Platform$r_arch, ...)

{
    pkgs <- c("base", "compiler", "datasets", "graphics", "grDevices",  "grid", "methods", "parallel", "splines", "stats", "stats4",  "tcltk", "tools", "utils")
    pkgmatrix <- cbind(
        Package = pkgs,
        LibPath = sprintf("classpath:///org/renjin/%s", pkgs),
        Version = "3.5.3",
        Priority = "base",
        Depends = NA,
        Imports = NA,
        LinkingTo = NA,
        Suggests = NA,
        Enhances = NA)

    rownames(pkgmatrix) <- pkgs
    pkgmatrix
}
