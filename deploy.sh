#! /bin/sh
set -e

cd "$(dirname "$0")"

. utils.sh
. .env-build

cd k8s

kustomize edit set image tic-tac-toe-image="$GCLOUD_REPOSITORY/tic-tac-toe-server:$(get_commit_hash)"
kubectl apply -k .
git restore kustomization.yaml

cd ..
./move-function/deploy.sh