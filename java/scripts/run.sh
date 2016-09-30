#!/bin/sh

clear

CLASSPATH=""

ITEMS=`find -name *.jar`
for ITEM in $ITEMS; do
   CLASSPATH=$CLASSPATH:$ITEM
done

echo $CLASSPATH

./java/bin/java -classpath $CLASSPATH -Xmx256m frolov.robot.Main

