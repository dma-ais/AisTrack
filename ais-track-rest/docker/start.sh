#!/usr/bin/env bash
JAR=`ls /ais-track-rest*.jar`
echo "Running: "
echo "java -jar $JAR -cp /data:."
java -jar $JAR -cp /data:.
