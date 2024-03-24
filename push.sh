#! /bin/sh
set -e

cd "$(dirname "$0")"

. utils.sh
. .env-build

docker push "$GCLOUD_REPOSITORY"/tic-tac-toe-server:"$(get_commit_hash)"