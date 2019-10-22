#!/usr/bin/env bash

spark-submit \
  --class SampleSparkApp \
  --master local[2] \
  --jars /Users/rc/Documents/workspace/spark-sample/target/spark-sample-1.0-SNAPSHOT.jar

