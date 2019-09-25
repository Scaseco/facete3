#!/bin/sh
cd "$(dirname "$0")"

p1=`find facete3-debian-cli/target | grep '\.deb$'`

sudo dpkg -i "$p1"

