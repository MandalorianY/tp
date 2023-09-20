package com.github.polomarcus.conf

object ConfService {
  val BOOTSTRAP_SERVERS_CONFIG = sys.env.getOrElse("BOOTSTRAP_SERVERS", "127.0.0.1:9092")
  val SCHEMA_REGISTRY = sys.env.getOrElse("SCHEMA_REGISTRY", "http://127.0.0.1:8081")
  val GROUP_ID = "my-spark-group"
  val TOPIC_IN = "news"
  val PATH_PARQUET ="./tp/data-engineering/tp-spark-structured-stream-kafka/logback/sink"
  val PATH_CHECKPOINT =  "./tp/data-engineering/tp-spark-structured-stream-kafka/logback/checkpoint"
}