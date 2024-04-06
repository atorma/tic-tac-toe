#! /bin/sh
set -e

cd "$(dirname "$0")"

. utils.sh
. .env-build

java_version=$(cat .java-version)
java_home=$(/usr/libexec/java_home -v"$java_version")
echo "JAVA_HOME=$java_home"
JAVA_HOME="$java_home" mvn clean package -DskipTests

cd client
npm run build
cd ..

docker build \
  -t tic-tac-toe-server \
  -t "$GCLOUD_REPOSITORY"/tic-tac-toe-server:"$(get_commit_hash)" \
  .