## TP - Getting started with a data broker - [Apache Kafka](https://kafka.apache.org/)
### Communication problems
![](https://content.linkedin.com/content/dam/engineering/en-us/blog/migrated/datapipeline_complex.png)

### Why a Broker such as Kafka ?

![](https://content.linkedin.com/content/dam/engineering/en-us/blog/migrated/datapipeline_simple.png)

### Use Kafka with docker
Start a kakfa server (called broker) using the docker compose recipe `docker-compose.yml` : 

```bash
docker compose -f docker-compose.yml up --detach
```

Check on the docker hub the image used : 
* https://hub.docker.com/r/confluentinc/cp-kafka

**Note** Using Mac M1 ? You have to change your image name `confluentinc/cp-kafka-connect:7.2.1.arm64` instead of `confluentinc/cp-kafka-connect:7.2.1`: https://github.com/provectus/kafka-ui/blob/master/documentation/compose/kafka-ui-arm64.yaml#L71

### Verify
```
docker ps
CONTAINER ID   IMAGE                             COMMAND                  CREATED          STATUS         PORTS                                                                                  NAMES
b015e1d06372   confluentinc/cp-kafka:7.1.3       "/etc/confluent/dock…"   10 seconds ago   Up 9 seconds   0.0.0.0:9092->9092/tcp, :::9092->9092/tcp, 0.0.0.0:9999->9999/tcp, :::9999->9999/tcp   kafka1
(...)
```

### Kafka User Interface
As Kafka does not have an interface, we are going to use the web app ["Kafka UI"](https://docs.kafka-ui.provectus.io/) thanks to docker compose.

Using Kafka UI on http://localhost:8080/, connect to **your existing docker kafka cluster** with `localhost:9092`.

0. Using Kafka UI, create a topic "mytopic" with 5 partitions
1. Find the `mytopic` topic on Kafka UI and its differents configs (InSync Replica, Replication Factor...)
2. Produce 10 messages (without a key) into it and read them
3. Look on which topic's partitions they are located.
The message are split between the 5 partitions.
4. Send another 10 messages but with a key called "my key"
5. Look again on which topic's partitions they are located.
The same partition is used for the same key.

Questions:
* [ ] When should we use a key when producing a message into Kafka ? What are the risks ? [Help](https://stackoverflow.com/a/61912094/3535853)
Key are used to know wich partition will be used to store the message. If you don't use a key, the partition is chosen randomly.
The risk is that if you use a key, all the messages with the same key will be stored in the same partition. If you have a lot of messages with the same key, the partition will be overloaded.

* [ ] How does the default partitioner (sticky partition) work with kafka ? [Help1](https://www.confluent.io/fr-fr/blog/apache-kafka-producer-improvements-sticky-partitioner/) and [Help2](https://www.conduktor.io/kafka/producer-default-partitioner-and-sticky-partitioner#Sticky-Partitioner-(Kafka-%E2%89%A5-2.4)-3)
The sticky partitioner is used to send all the messages with the same key to the same partition. It's the default partitioner since kafka 2.4.
And instead of changing partition at every message, the sticky partitioner will change partition at every batch of messages. So it's spread but more efficient.
It wait until a partition is full before changing partition.

#### Command CLI
1. Connect to your kafka cluster with 2 command-line-interface (CLI)

Using [Docker exec](https://docs.docker.com/engine/reference/commandline/exec/#description)

```
docker exec -ti tp-docker-kafka-kafka1-1 bash
> pwd
```

```
> kafka-topics # to get help on this command
# To list all topic you can use :
> kafka-topics --describe --bootstrap-server localhost:19092
```

Pay attention to the `KAFKA_ADVERTISED_LISTENERS` config from the docker-compose file.
This line is meant to to represent kafka broker , and 19092 is the port number on which Kafka is listening for connections.
2. Create a "mailbox" - a topic with the default config : https://kafka.apache.org/documentation/#quickstart_createtopic
``
kafka-topics --create --topic mailbox --bootstrap-server localhost:19092
```
By default there is only one partition for a created topic.
3. Check on which Kafka broker the topic is located using `--describe`
```
kafka-topics --describe --topic mailbox --bootstrap-server localhost:19092
```
Topic: mailbox  TopicId: y88lcFcgRcm4dsujIhFkdQ PartitionCount: 1       ReplicationFactor: 1    Configs: 
        Topic: mailbox  Partition: 0    Leader: 1       Replicas: 1     Isr: 1

As we can see there is only one replicas and the leader is broker 1.

5. Send events to a topic on one terminal : https://kafka.apache.org/documentation/#quickstart_send
```
kafka-console-producer --topic mailbox --bootstrap-server localhost:19092
```
 By default, each line you enter will result in a separate event being written to the topic.
4. Keep reading events from a topic from one terminal : https://kafka.apache.org/documentation/#quickstart_consume
* try the default config
* what does the `--from-beginning` config do ? What happens when you do not use `--from-beginning` and instead the config `--group` such as --group?
A consumer group is a group of consumers that work together to consume a topic. 
The --group option allows to read the messages from the last offset of the topic.
The --from-beginning option allows to read all the messages from the beginning of the topic but it's not always a good idea because it can be a lot of messages.
```
kafka-console-consumer --topic mailbox --group Pizza --bootstrap-server localhost:19092 
```	
* Keep reading the message in your terminal and using Conduktor, can you notice something in the **Consumers tab** ? 
There is a consummer who is active that is reading the messages the lag stays at 1

* Now, in your terminal stop your consumer, notice the **lag** inside the **Consumer tab** on Conduktor, it should be **0**
* With a producer, send message to the same topic, and look at the value of **lag**, what's happening ?
As we stop the consumer the message pile up so the lag is increasing.
* Restart your consumer with the same consumer group, what's happening ?
* Trick question : What about using the `--group` option for your producer ?

#### Partition - consumer group / bookmark
1. Check consumer group with `kafka-console-consumer` : https://kafka.apache.org/documentation/#basic_ops_consumer_group
* notice if there is [lag](https://univalence.io/blog/articles/kafka-et-les-groupes-de-consommateurs/) for your group
```
kafka-consumer-groups --bootstrap-server localhost:19092 --describe --group Pizza
```
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
Pizza           mailbox         0          225             243             18              -               -    
As we can see right there is only one group with a lag of 11
2. read from a new group, what happened ?
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
Donuts          mailbox         0          243             243             0               -               -               -
If a new group is created it's start from the current offset so it's read the last messages.
3. read from an already existing group, what happened ?
It's consume the lag before going to the current offset.
4. Recheck consumer group using `kafka-console-consumer`
The lag is now 0
#### Replication - High Availability
0. Stop your broker using `docker compose down` then with the file `docker-compose-multiple-kafka.yml` start 3 brokers : `docker-compose -f docker-compose-multiple-kafka.yml up -d`
1. Create a new topic with a replication factor (RF) of 3, in case one of your broker goes down : https://kafka.apache.org/documentation/#topicconfigs
````
* `docker exec -ti tp-docker-kafka-kafka1-1 bash`
* `kafka-topics --create --replication-factor 3 --partitions 2 --topic testreplicated --bootstrap-server localhost:19092`
> Created topic testreplicated.
2. Describe your topic, notice where the different partitions are replicated and where are the leaders
```
kafka-topics --describe --topic Triceratops --bootstrap-server localhost:19092
```
 Topic: Triceratops      Partition: 0    Leader: 2       Replicas: 2,3,1 Isr: 2,3,1
 Topic: Triceratops      Partition: 1    Leader: 3       Replicas: 3,1,2 Isr: 3,1

As we can see we have 2 replicas for each partition and the leader is not always the same.
Replicas 2,3,1 means that the partition 0 is replicated on the broker 2,3 and 1.
ISR difference with Replicas is that ISR is the list of replicas that are in sync with the leader.
When a producer sends a message to a Kafka topic, it is initially written to the leader replica's log. Once the leader replica acknowledges the message and commits it to its log, it can then be considered "in sync." At this point, the leader replica will send the message to the replicas in the ISR for replication.

3. now, stop one of your brokers with docker : `docker stop your_container`

4. Describe your topic, check and notice the difference with the ISR (in-sync replica) config : https://kafka.apache.org/documentation/#design_ha
As we can see the ISR is now 1,3 for both partitions, the broker 2 is not in sync anymore, so that means it will have some lag
5. Restart your stopped broker:  `docker start your_container`
6. Check again your topic
As we can see it takes time to the broker 2 to be in sync again.
7. Bonus: you can do this operation while keeping producing message to this kafka topic with your command line
Set up an an automatic producer 
```
while true; do
  echo "Your string message" | kafka-console-producer --topic Triceratops --bootstrap-server localhost:19092
  sleep 1
done
```

Set up a consumer
```
kafka-console-consumer --topic Triceratops --group Pizza --bootstrap-server localhost:19092 
```

