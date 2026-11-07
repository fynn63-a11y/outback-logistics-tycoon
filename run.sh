#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
if [ ! -d out ]; then ./build.sh; fi
java -cp out com.outback.tycoon.Main
