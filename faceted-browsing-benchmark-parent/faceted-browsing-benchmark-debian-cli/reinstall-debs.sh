#!/bin/sh
cd "$(dirname "$0")"

p1=`find ./target | grep '\.deb$'`

sudo dpkg -i "$p1"

