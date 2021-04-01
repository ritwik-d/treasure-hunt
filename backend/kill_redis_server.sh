#!/usr/bin/bash

pid=`ps aux | grep redis | grep -v grep | awk '{print $2}'`
echo "killing pid: $pid"
kill pid
