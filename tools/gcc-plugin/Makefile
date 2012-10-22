GCC=gcc
PLUGIN_SOURCE_FILES= renjin-plugin.c
PLUGIN_OBJECT_FILES= $(patsubst %.c,%.o,$(PLUGIN_SOURCE_FILES))
GCCPLUGINS_DIR:= $(shell $(GCC) -print-file-name=plugin)
CFLAGS+= -I$(GCCPLUGINS_DIR)/include -fPIC -O2
     
renjin.so: $(PLUGIN_OBJECT_FILES)
	$(GCC) -shared $^ -o $@
