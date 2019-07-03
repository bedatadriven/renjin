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


file_ext <-
function(x)
{
    ## Return the file extensions.
    ## (Only purely alphanumeric extensions are recognized.)
    pos <- regexpr("\\.([[:alnum:]]+)$", x)
    ifelse(pos > -1L, substring(x, pos + 1L), "")
}

### ** file_path_as_absolute

file_path_as_absolute <-
function(x)
{
    ## Turn a possibly relative file path absolute, performing tilde
    ## expansion if necessary.
    if(length(x) != 1L)
        stop("'x' must be a single character string")
    if(!file.exists(epath <- path.expand(x)))
        stop(gettextf("file '%s' does not exist", x),
             domain = NA)
    normalizePath(epath, "/", TRUE)
}

### ** file_path_sans_ext

file_path_sans_ext <-
function(x, compression = FALSE)
{
    ## Return the file paths without extensions.
    ## (Only purely alphanumeric extensions are recognized.)
    if(compression)
        x <- sub("[.](gz|bz2|xz)$", "", x)
    sub("([^.]+)\\.[[:alnum:]]+$", "\\1", x)
}

### ** file_test

## exported/documented copy is in utils.

file_test <-
function(op, x, y)
{
    ## Provide shell-style '-f', '-d', '-x', '-nt' and '-ot' tests.
    ## Note that file.exists() only tests existence ('test -e' on some
    ## systems), and that our '-f' tests for existence and not being a
    ## directory (the GNU variant tests for being a regular file).
    ## Note: vectorized in x and y.
    switch(op,
           "-f" = !is.na(isdir <- file.info(x, extra_cols = FALSE)$isdir) & !isdir,
           "-d" = dir.exists(x),
           "-nt" = (!is.na(mt.x <- file.mtime(x))
                    & !is.na(mt.y <- file.mtime(y))
                    & (mt.x > mt.y)),
           "-ot" = (!is.na(mt.x <- file.mtime(x))
                    & !is.na(mt.y <- file.mtime(y))
                    & (mt.x < mt.y)),
           "-x" = (file.access(x, 1L) == 0L),
           stop(gettextf("test '%s' is not available", op),
                domain = NA))
}

### ** list_files_with_exts

list_files_with_exts <-
function(dir, exts, all.files = FALSE, full.names = TRUE)
{
    ## Return the paths or names of the files in @code{dir} with
    ## extension in @code{exts}.
    ## Might be in a zipped dir on Windows.
    if(file.exists(file.path(dir, "filelist")) &&
       any(file.exists(file.path(dir, c("Rdata.zip", "Rex.zip", "Rhelp.zip")))))
    {
        files <- readLines(file.path(dir, "filelist"))
        if(!all.files)
            files <- grep("^[^.]", files, value = TRUE)
    } else {
        files <- list.files(dir, all.files = all.files)
    }
    ## does not cope with exts with '.' in.
    ## files <- files[sub(".*\\.", "", files) %in% exts]
    patt <- paste0("\\.(", paste(exts, collapse="|"), ")$")
    files <- grep(patt, files, value = TRUE)
    if(full.names)
        files <- if(length(files))
            file.path(dir, files)
        else
            character()
    files
}

### ** list_files_with_type

list_files_with_type <-
function(dir, type, all.files = FALSE, full.names = TRUE,
         OS_subdirs = .OStype())
{
    ## Return a character vector with the paths of the files in
    ## @code{dir} of type @code{type} (as in .make_file_exts()).
    ## When listing R code and documentation files, files in OS-specific
    ## subdirectories are included (if present) according to the value
    ## of @code{OS_subdirs}.

    exts <- .make_file_exts(type)
    files <-
        list_files_with_exts(dir, exts, all.files = all.files,
                             full.names = full.names)

    if(type %in% c("code", "docs")) {
        for(os in OS_subdirs) {
            os_dir <- file.path(dir, os)
            if(dir.exists(os_dir)) {
                os_files <- list_files_with_exts(os_dir, exts,
                                                 all.files = all.files,
                                                 full.names = FALSE)
                os_files <- file.path(if(full.names) os_dir else os,
                                      os_files)
                files <- c(files, os_files)
            }
        }
    }
    ## avoid ranges since they depend on the collation order in the locale.
    ## in particular, Estonian sorts Z after S.
    if(type %in% c("code", "docs")) { # only certain filenames are valid.
        files <- files[grep("^[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789]", basename(files))]
    }
    if(type %in% "demo") {           # only certain filenames are valid.
        files <- files[grep("^[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]", basename(files))]
    }
    files
}


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