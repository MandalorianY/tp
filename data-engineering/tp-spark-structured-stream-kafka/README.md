# Practices - Data engineering

## First step - Getting Started

Start Kafka with Docker Compose and init it with news data thanks to :
```bash
cat init.sh

chmod 755 init.sh

./init.sh
```

**Important** : As we have a streaming application, we need to send data again and again, we can do it by running `init.sh` again and again.

## Second step - Apache Spark Structured Streaming with Apache Kafka
To process a large amount of data partitioned on a data lake, you can use data processing frameworks such as Apache Spark.

Spark allows us to perform batch or streaming query :
1. Discover Spark Structured Streaming : https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html


Some definition  :

*What is "sink" and "data source":

Data Source: 

A data source refers to the origin or input of data in a data processing system. It is the provider of data that is read and processed by Spark. Data sources can be various types, such as files (e.g., CSV, JSON), databases (e.g., JDBC), message queues (e.g., Kafka), or even custom sources. Spark Structured Streaming provides built-in support for reading data from multiple sources, making it easy to integrate with different data systems.
Data Sink: 

A data sink, on the other hand, refers to the destination or output of processed data in a data processing system. It is where the processed data is written or stored. Data sinks can be files (e.g., CSV, JSON), databases (e.g., JDBC), message queues (e.g., Kafka), or other storage systems. Spark Structured Streaming provides built-in support for writing data to various sinks, allowing you to store the processed data in different formats or systems.


-Data Sink: A data sink, on the other hand, refers to the destination or output of processed data in a data processing system. It is where the processed data is written or stored. Data sinks can be files (e.g., CSV, JSON), databases (e.g., JDBC), message queues (e.g., Kafka), or other storage systems. Spark Structured Streaming provides built-in support for writing data to various sinks, allowing you to store the processed data in different formats or systems

Some questions :


* What is the Spark Dataset API ?

A Dataset is a distributed collection of data. they provide the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine. It is built on top of Spark SQL and provides a more expressive and efficient API than RDDs for working with structured and semi-structured data. 
Dataset is represented as a distributed collection of typed objects (rows) that are partitioned across multiple nodes in a Spark cluster. They have the same methods as RDDs, but they also have methods that are optimized for structured data.

source : https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html

* What is the micro-batch processing model ?

Micro-batch processing is a practice in which data is collected and processed in small groups or batches. It is a variant of traditional batch processing where data processing occurs more frequently, resulting in smaller groups of new data being processe
It's different from stream processing. While micro-batch processing collects data in small groups and processes them periodically, stream processing collects and processes data immediately as they are generated. Stream processing is ideal for use cases that require real-time responsiveness and live interaction but Micro-batch still allow for faster processing times.

source : https://hazelcast.com/glossary/micro-batch-processing/

* What are the 3 different output modes of Spark Structured Streaming ?

The three different output modes of Spark Structured Streaming are:

- Complete Mode: In this mode, the entire updated result table is written to the sink after each trigger. It means that all the rows in the result table, including the ones that haven't changed since the last trigger, will be written to the sink. This mode is useful when you need to maintain a complete and up-to-date view of the result data. However, it may result in a large amount of data being written to the sink, especially if the result table is large.

- Append Mode: In this mode, only the new rows appended to the result table since the last trigger are written to the sink. It means that only the rows that have been added to the result table will be written to the sink. This mode is suitable when the result table is only growing and doesn't have any updates or deletions.

- Update Mode: In this mode, only the updated rows in the result table since the last trigger are written to the sink. It means that only the rows that have been updated will be written to the sink. This mode is useful when the result table can have updates or deletions. It allows you to track the changes in the result table over time. However, it requires maintaining some form of state to track the changes, which may have performance implications.

source : https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html

* Which input sources or data sinks can Spark work with ? 

Input:  
-Data Sinks: 
Rate: A testing source that generates data at a given rate.
Socket: Listens to a specified socket and ingests any data received.
File: Reads streaming data from a directory as files in various formats like CSV, JSON, ORC, and Parquet.
Kafka: Consumes data from Apache Kafka topics

-Data Sinks: 
Console: Writes the output to the console for debugging and testing purposes.
File: Writes the output to files in various formats such as CSV, JSON, ORC, and Parquet.
Kafka: Writes the output to Apache Kafka topics.
JDBC: Writes the output to relational databases using the JDBC sink.
Memory: Writes the output to an in-memory table or a temporary view.
Custom Sinks: Allows you to implement your own custom sinks by extending the Sink interface.


## How to read from / write to Kafka ?
Spark can read and write with Kafka : https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
 
Some questions :
* How to configure the Spark consumer and producer ? [Help](https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html#kafka-specific-configurations)

We need to create a Kafka Source and Subscribe to kakfka with the read method

There is multiple method to write back but we can use the writeStream method with the format("kafka") option.

source: https://medium.com/data-arena/enabling-streaming-data-with-spark-structured-streaming-and-kafka-93ce91e5b435
source : https://stackoverflow.com/questions/62737171/trying-to-consuming-the-kafka-streams-using-spark-structured-streaming
<br>
* Can we use the Schema Registry with Spark ? [Help 1](https://learn.microsoft.com/en-us/azure/databricks/_static/notebooks/schema-registry-integration/avro-data-and-schema-registry.html)

## Third Step - Code
The goal is to process JSON data coming from the Kafka topics "news".

To run the app `sbt run`

Once the Streaming app is running you can access to the monitoring console here : http://localhost:4040/

### Write as a parquet file locally
For data durability, you want to save your kafka's topic data to a data lake using the columnar format Parquet.

You'll look at this [chapter of the Spark documentation](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#output-sinks) to know how to perform this operation and code it inside `KafkaService`

We can select what we want to write to parquet with <code> val selectedDF = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)") <\code>
and then write back with  <code> 
val query = selectedDF
  .writeStream
  .format("parquet")
</code>
  
### Transformation
Inside `Main`, count the number of news by media, and display it using the ConsoleSink