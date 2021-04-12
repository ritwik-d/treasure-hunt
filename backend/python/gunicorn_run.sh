#!/bin/bash

function main {
  host='127.0.0.1:8000'
  echo "host: ${host}"
  nohup gunicorn -w 4 -k uvicorn.workers.UvicornWorker -b $host main:app &
}

main
