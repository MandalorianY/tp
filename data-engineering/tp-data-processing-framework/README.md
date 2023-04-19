# Practices - Data engineering

## TP - Data processing with Apache Spark
To process a large amount of data partitioned on a data lake, you can use data processing frameworks such as Apache Spark :
1. Read : https://spark.apache.org/docs/latest/sql-programming-guide.html

Some questions :
* What is Spark RDD API ?

RDD stands for Resilient Distributed Dataset.
Spark's RDD API is a distributed way of manipulating data, making use of fault-tolerant collections that can be processed in parallel across a cluster of computers. 
Spark uses RDD APIs to achieve faster and efficient method such as map, reduce, filter, groupByKey, join.
Fault tolerance means that if a node fails, the RDD can be recomputed from the original data by using another node.
RDDs are immutable, meaning that once they are created, they cannot be modified.
RDDs are also lazily evaluated, meaning that the computation is not performed until an action is called, such as collect or count.

Source : https://www.xenonstack.com/blog/rdd-in-spark/#:~:text=Resilient%20Distributed%20Dataset%20(RDD)%20is,that%20resides%20on%20multiple%20nodes.

* What is Spark Dataset API ?

A Dataset is a distributed collection of data. Datasets provide the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine. 
It is built on top of Spark SQL and provides a more expressive and efficient API than RDDs for working with structured and semi-structured data.
Dataset is represented as a distributed collection of typed objects (rows) that are partitioned across multiple nodes in a Spark cluster.
They have the same methods as RDDs, but they also have methods that are optimized for structured data.

source : https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html

* With which languages can you use Spark ? 

Spark can be used with Java, Scala, Python, R, SQL.

* Which data sources or data sinks can Spark work with ? 

Spark can work lots and lots of different files type and source with HDFS, Cassandra, HBase, Hive, JDBC, JSON, Parquet, ORC, Avro, Kafka, etc.

### Analyse data with Apache Spark and Scala 
One engineering team of your company created for you a TV News data stored as JSON inside the folder `data-news-json/`.

Your goal is to analyze it with your savoir-faire, enrich it with metadata, and store it as [a column-oriented format](https://parquet.apache.org/).

1. Look at `src/main/scala/com/github/polomarcus/main/Main.scala` and update the code 

**Important note:** As you work for a top-notch software company following world-class practices, and you care about your project quality, you'll write a test for every function you write.

You can see tests inside `src/test/scala/` and run them with `sbt test`

### How can you deploy your app to a cluster of machines ?
* https://spark.apache.org/docs/latest/cluster-overview.html

### Business Intelligence (BI)
How could use we Spark to display data on a BI tool such as [Metabase](https://www.metabase.com/) ?

Tips: https://github.com/polomarcus/television-news-analyser#spin-up-1-postgres-metabase-nginxand-load-data-to-pg

### Continuous build and test
**Pro Tips** : https://www.scala-sbt.org/1.x/docs/Running.html#Continuous+build+and+test

Make a command run when one or more source files change by prefixing the command with ~. For example, in sbt shell try:
```bash
sbt
> ~ testQuick
```