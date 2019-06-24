

# Renjin exposes Recall() directly as a special function
# The following wrapper defined by GNU R in eval.R is not necesssary:
#
# Recall <- function(...) .Internal(Recall(...))

rm("Recall")
rm("cbind")
rm("rbind")

rm("attachNamespace")
