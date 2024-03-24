#! /bin/sh
set -e

function get_commit_hash() {
  echo $(git rev-parse --short HEAD)
}