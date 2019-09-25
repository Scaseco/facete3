#!/bin/sh
cd "$(dirname "$0")"

p1=`find facete3-fsbg-debian-cli/target | grep '\.deb$'`

sudo dpkg -i "$p1"

