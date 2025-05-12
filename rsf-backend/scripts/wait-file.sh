#!/usr/bin/env bash
set -euo pipefail

: "${TOKEN:?Need Bearer TOKEN}"        # exported beforehand
: "${FILE_ID:?Need FILE_ID variable}"  # UUID returned by /upload

STATUS=""
URL="http://localhost:8080/api/files/${FILE_ID}/status"

while true; do
  STATUS=$(curl -s -H "Authorization: Bearer $TOKEN" "$URL" | jq -r .status)
  printf 'status=%s\n' "$STATUS"

  case "$STATUS" in
    READY)   break ;;  # success
    ERROR)   echo "processing failed" ; exit 1 ;;
  esac

  sleep 1
done

echo "file is ready" 