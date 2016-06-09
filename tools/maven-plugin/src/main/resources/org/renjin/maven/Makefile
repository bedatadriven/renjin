
########################################################################
# shlib.mk
##########################################################################3

## ${R_HOME}/share/make/shlib.mk

all: $(SHLIB)

# Note that for Renjin, we don't actually need to link the shared library

$(SHLIB): $(OBJECTS)

.PHONY: all shlib-clean

shlib-clean:
	@rm -rf .libs _libs
	@rm -f $(OBJECTS) symbols.rds


## FIXME: why not Rscript?
symbols.rds: $(OBJECTS)
	@$(ECHO) "tools:::.shlib_objects_symbol_tables()" | \
	  $(R_HOME)/bin/R --vanilla --slave --args $(OBJECTS)