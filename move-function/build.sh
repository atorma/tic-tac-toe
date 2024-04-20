#! /bin/sh
set -e

cd "$(dirname "$0")"
cd ..

. utils.sh
. .env-build

java_version=$(cat .java-version)
java_home=$(/usr/libexec/java_home -v"$java_version")
echo "JAVA_HOME=$java_home"
JAVA_HOME="$java_home" mvn clean install --projects game --batch-mode -D skipTests
JAVA_HOME="$java_home" mvn clean package --projects move-function --also-make --batch-mode -D skipTests