
# Renjin does not have a concept of loading / unloading dynamic libraries
# as we focus rather on translating native code to JVM byte code which is 
# then managed by the JVM class loader, so loading / unloading dlls is not necessary

# library.dynam and library.dynam.unload both are meant to return DllInfo structures,
# but a survey of actual uses by packages shows that in the vast majority of cases,
# the return values are not used.

library.dynam <- function(chname, package, lib.loc, verbose = FALSE, file.ext = "", ...) {
    .Internal(library.dynam(chname, package))
}

library.dynam.unload <- function(chname, libpath, verbose = FALSE, file.ext = "") {
    .Internal(library.dynam.unload(chname))
}
