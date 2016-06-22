

setClass("NSBS",
    representation(
        "VIRTUAL",
        upper_bound="integer",            # single integer >= 0
        upper_bound_is_strict="logical",  # TRUE or FALSE
        ## 'subscript' is an object that holds integer values >= 1 and
        ## <= upper_bound. The precise type of the object depends on the NSBS
        ## subclass and is specified in the subclass definition.
        subscript="ANY"
    ),
    prototype(
        upper_bound=0L,
        upper_bound_is_strict=TRUE
    )
)


setClass("NativeNSBS",  # not exported
    contains="NSBS",
    representation(
        subscript="integer"
    ),
    prototype(
        subscript=integer(0)
    )
)