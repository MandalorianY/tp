package com.github.polomarcus.utils

import com.github.polomarcus.conf.ConfService
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.PartitionInfo
import scala.collection.JavaConverters._
import java.time.Duration
import java.util
import java.util.Properties

object KafkaConsumerService {
  val logger = Logger(KafkaProducerService.getClass)



  val props: Properties = new Properties()
  props.put("group.id", ConfService.GROUP_ID)
  props.put("bootstrap.servers", ConfService.BOOTSTRAP_SERVERS_CONFIG)
  props.put(
    "key.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer"
  )
  props.put(
    "value.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer"
  )

  props.put(
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
    "earliest"
  ) // on the first execution, read from the beginning

  // @see https://docs.confluent.io/platform/current/clients/consumer.html#offset-management
  props.put(
    "enable.auto.commit",
    "false"
  ) // what are the risks to use this config ?
  // The risk is that if the consumer crashes, it will not be able to read the messages that were not committed
  // and will read them again. This is called "at least once" delivery.
  // Commited means that the consumer has read the message and saved the offset in the __consumer_offsets topic
  // We can have data loss or duplicated

  props.put("auto.commit.interval.ms", "1000")

  val consumer = new KafkaConsumer[String, String](props)
  val topic = ConfService.TOPIC_OUT
  val topicToRead = List(topic).asJava

  // we need to connect our consumer to our topic by **subscribing** it, tips : https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html#idm45788273579960
  consumer.subscribe(topicToRead)

  def consume() = {
    try {
      for (i <- 0 to 20) { // to avoid a while(true) loop
        val messages = consumer.poll(Duration.ofMillis(1000))
        if (messages.count() > 0) {
          messages.forEach(record => {
            logger.info(s""" Reading :
                 |Offset : ${record.offset()} from partition ${record
                            .partition()}
                 |Value : ${record.value()}
                 |Key : ${record.key()}
                 |""".stripMargin)
          })
        } else {
          logger.info(s"No new messages, waiting 3 seconds - will shutdown in ${i}/${numberOfLoop}")
          Thread.sleep(3000)
        }
      }
    } catch {
      case e: Exception => logger.error(e.toString)
    } finally {
      logger.warn("Closing our consumer, saving the consumer group offsets")
      consumer.close()
    }
  }
}
