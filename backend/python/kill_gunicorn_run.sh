#!/bin/bash

function main {
  kill `ps aux | grep gunicorn | grep -v grep | awk '{print $2}'`
  rm /home/ritwik/git_hub/TreasureHunt/backend/python/nohup.out
}

main
