#!/bin/sh

# Include the wrappers utility script
. /usr/lib/java-wrappers/java-wrappers.sh

# We need sun runtime.
find_java_runtime openjdk sun 

export JAVA_CLASSPATH=$(echo /usr/share/renjin/lib/*.jar | tr ' ' ':')

run_java org.renjin.cli.Main "$@"