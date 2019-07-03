#!/usr/bin/env bash

ROOT=../R-3.5.3
SRC=$ROOT/src

# Headers
# Require some merging as Renjin makes a few changes
cp -r $SRC/include/* ./tools/gnur-installation/src/main/resources/include/


# Packages
PACK
for


# grDevices
# ~~~
# This is a difficult merge as we are combining C and Java Code
# As well as some sources from src/main that we migrate to the grDevices module

GRDC=packages/grDevices/src/main/c
cp $SRC/main/g_*                        $GRDC
cp $SRC/main/devices.c                  $GRDC/main-devices.c
cp $SRC/main/graphics.c                 $GRDC/main-graphics.c
cp $SRC/main/plot.c                     $GRDC/main-plot.c
cp $SRC/main/plotmath.c                 $GRDC/main-plotmath.c
cp $SRC/main/xspline.c                  $GRDC/main-xspline.h
cp $SRC/main/grDevices/src/*.c          $GRDC/
cp $SRC/main/grDevices/src/grDevices.h  $GRDC/

