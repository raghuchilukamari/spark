#!/usr/bin/env bash

spark-submit \
  --class SampleSparkApp \
  --master local[2] \
#  --conf spark.eventLog.enabled=true \
#  --conf spark.eventLog.dir hdfs://namenode/shared/spark-logs \
  /Users/rc/Documents/workspace/spark-sample/target/spark-sample-1.0-SNAPSHOT.jar


#spark-submit \
#  --class SampleSparkApp \
#  --master yarn-cluster \
#  /Users/rc/Documents/workspace/spark-sample/target/spark-sample-1.0-SNAPSHOT.jar



