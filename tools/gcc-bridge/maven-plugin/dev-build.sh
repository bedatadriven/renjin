#!/bin/bash -e

if [ -z "$1" ]; then
	echo "USAGE: ./dev-build.sh <version>"
	echo
	echo "where <version> relates to the version in './pom.xml' and the project.version property in '../../../build.gradle'"
	echo "e.g., ./dev-build.sh 3.5-betadev"
	exit 1
fi

[ -f "../runtime/build/libs/runtime-$1.jar" ] || { echo "Runtime library not built, run './gradlew publishToMavenLocal' in the renjin root folder, and ensure version is consistent" && exit 1; }

echo "Building..."
set -x
mvn package
mvn install:install-file -Dfile=../runtime/build/libs/runtime-$1.jar -DgroupId=org.renjin -DartifactId=gcc-runtime -Dversion=$1 -Dpackaging=jar
mvn install:install-file -Dfile=target/gcc-bridge-maven-plugin-$1.jar -DgroupId=org.renjin -DartifactId=gcc-bridge-maven-plugin -Dversion=$1 -Dpackaging=jar -DpomFile=pom.xml
