#!/bin/bash

function main {
  kill `ps aux | grep gunicorn | grep -v grep | awk '{print $2}'`
}

main
