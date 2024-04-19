#! /bin/sh
set -e

gcloud functions deploy tic-tac-toe-move \
    --gen2 \
    --region=europe-west1 \
    --source=target/deployment \
    --runtime=java21 \
    --trigger-http \
    --entry-point=org.atorma.tictactoe.MoveComputationFunction \
    --timeout=30 \
    --max-instances=2 \
    --memory=4Gi \
    --cpu=8 \
    --no-allow-unauthenticated
