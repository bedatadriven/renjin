
library(hamcrest)
library(methods)
library("org.renjin.test:s4test")


# NSBS is exported by s4test,
# but extends a class NativeNSBS that is NOT exported
setClass("RleNSBS", contains="NSBS", representation(subscript="Rle"))