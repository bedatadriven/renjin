#  File src/library/tools/R/utils.R
#  Part of the R package, https://www.R-project.org
#
#  Copyright (C) 1995-2016 The R Core Team
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  https://www.R-project.org/Licenses/


.get_internal_S3_generics <- function (primitive = TRUE)
{
    out <- c("[", "[[", "$", "[<-", "[[<-", "$<-", "as.vector",
    "unlist", .get_S3_primitive_generics())
    if (!primitive)
    out <- out[!vapply(out, .is_primitive_in_base, NA)]
    out
}

.is_primitive_in_base <- function (fname)
{
    is.primitive(get(fname, envir = baseenv(), inherits = FALSE))
}


.get_S3_primitive_generics <- function (include_group_generics = TRUE)
{
if (include_group_generics)
c(base::.S3PrimitiveGenerics, "abs", "sign", "sqrt",
"floor", "ceiling", "trunc", "round", "signif", "exp",
"log", "expm1", "log1p", "cos", "sin", "tan", "acos",
"asin", "atan", "cosh", "sinh", "tanh", "acosh",
"asinh", "atanh", "lgamma", "gamma", "digamma", "trigamma",
"cumsum", "cumprod", "cummax", "cummin", "+", "-",
"*", "/", "^", "%%", "%/%", "&", "|", "!", "==",
"!=", "<", "<=", ">=", ">", "all", "any", "sum",
"prod", "max", "min", "range", "Arg", "Conj", "Im",
"Mod", "Re")
else base::.S3PrimitiveGenerics
}


.get_standard_package_names <-
local({
    lines <- readLines(file.path(R.home("share"), "make", "vars.mk"))
    lines <- grep("^R_PKGS_[[:upper:]]+ *=", lines, value = TRUE)
    out <- strsplit(sub("^R_PKGS_[[:upper:]]+ *= *", "", lines), " +")
    names(out) <-
    tolower(sub("^R_PKGS_([[:upper:]]+) *=.*", "\\1", lines))
    eval(substitute(function() {out}, list(out=out)), envir=NULL)
})