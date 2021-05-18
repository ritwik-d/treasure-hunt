#!/bin/bash

function run {
  gunicorn -w 4 -k uvicorn.workers.UvicornWorker -b $host main:app
}


function main {
  host='127.0.0.1:8000'
  echo "host: ${host}"

  if [[$1 = 'false']]
  then
    nohup run &
  elif [[$1 = 'true']]
  then
    run
}

get_args
main
