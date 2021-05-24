#!/bin/bash

function main {
  host='127.0.0.1:8000'
  echo "host: ${host}"
  nohup gunicorn -w 2 -k uvicorn.workers.UvicornWorker -b $host main:app --error-logfile /home/ritwik/logs/guni-err.log &
}

main
