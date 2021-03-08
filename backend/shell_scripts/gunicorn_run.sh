#!/bin/bash

function main {
  host=''
  echo "host: ${host}"
  gunicorn -w 4 -k uvicorn.workers.UvicornWorker -b $host main:app
}

main
