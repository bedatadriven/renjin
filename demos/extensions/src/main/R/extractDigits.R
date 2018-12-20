extractDigits <- function(x) {
    import(transformers.StringTransformer)
    sapply(x, function(txt) StringTransformer$toNumber(txt = txt), USE.NAMES = FALSE)
}
makeNumber <- function(x) as.numeric(extractDigits(x))