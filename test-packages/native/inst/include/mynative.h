#ifndef H_MYNATIVE_H
#define H_MYNATIVE_H

#include <Rinternals.h>
#include <R_ext/Rdynload.h>

SEXP Cmysum(SEXP vector);
SEXP Cmydsum(SEXP vector);
SEXP Cinvokeupper();

void dpchimtest_();
void dpchim2_();

#endif