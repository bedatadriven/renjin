GCC=gcc-4.6
PLUGIN_SOURCE_FILES= src/main/c/plugin.c
PLUGIN_OBJECT_FILES= $(patsubst %.c,%.o,$(PLUGIN_SOURCE_FILES))
GCCPLUGINS_DIR:= $(shell $(GCC) -print-file-name=plugin)
CFLAGS+= -I$(GCCPLUGINS_DIR)/include -fPIC -O2
     
bridge.so: $(PLUGIN_OBJECT_FILES)
	$(GCC) -shared $^ -o $@
