#!/bin/bash

cd "$(dirname "$0")/lib"
jars=($(ls *.jar))
JAR_PATH=
dir=$(pwd)
for i in "${jars[@]}"; do
	JAR_PATH="${JAR_PATH}:$dir/$i"
done
export CLASSPATH=$CLASSPATH:$JAR_PATH

echo $CLASSPATH
java -Xmx256m frolov.robot.Main &

killall Terminal