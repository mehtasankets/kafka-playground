package com.mehtasan.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import java.util.concurrent.Future
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.random.Random

/**
 * @author mehtasan
 */

fun main() {

    val objectMapper = createObjectMapper()
    val producer = createProducer()
    val futures = publishData(objectMapper, producer)
    waitForCompletion(futures)

    println("Bye bye")
}

private fun createObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
}

private fun waitForCompletion(futures: Stream<Pair<String, Future<RecordMetadata>>>) {
    futures.forEach {
        val recordMetadata = it.second.get()
        println("${it.first}: ${recordMetadata.partition()}-${recordMetadata.topic()}")
    }
}

private fun publishData(
    objectMapper: ObjectMapper,
    producer: Producer<String, String>
): Stream<Pair<String, Future<RecordMetadata>>> = IntStream.range(1000, 2000).mapToObj {
    val sector = Sector.values()[it % Sector.values().size]
    val tickerDetails = TickerDetails(
        "Symbol-${it.toString().padStart(3, '0')}", sector, Random.nextDouble(100.0, 5000.0)
    )
    val serializedTickerDetails = objectMapper.writeValueAsString(tickerDetails)
    return@mapToObj serializedTickerDetails to producer.send(
        ProducerRecord("ticker-price", sector.name, serializedTickerDetails)
    )
}

fun createProducer(): Producer<String, String> {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:10001,localhost:10002,localhost:10003"
    props[ProducerConfig.PARTITIONER_CLASS_CONFIG] = SectorPartitioner::class.java.canonicalName
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.canonicalName
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.canonicalName
    return KafkaProducer<String, String>(props)
}
