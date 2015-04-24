#!/usr/bin/env bash
JAR=`ls /ais-track-rest*SNAPSHOT.jar`
echo "Running: "
echo "java -jar $JAR -cp /data:."
java -jar $JAR -cp /data:.
