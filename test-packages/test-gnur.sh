#!/bin/sh

R -e "devtools::check('s4test', manual=FALSE, document=FALSE)"
R CMD INSTALL s4test

R -e "devtools::check('s4testdep', manual=FALSE, document=FALSE)"
