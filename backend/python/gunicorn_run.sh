#!/bin/bash

function main {
  host='192.168.1.56:8000'
  echo "host: ${host}"
  gunicorn -w 4 -k uvicorn.workers.UvicornWorker -b $host main:app
}

main
