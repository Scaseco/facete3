#!/bin/sh
cd "$(dirname "$0")"

p1=`find . -wholename '*/facete3-debian-app-terminal/target/*.deb'`

sudo dpkg -i "$p1"

