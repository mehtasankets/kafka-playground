package com.mehtasan.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.DoubleDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import java.util.stream.IntStream
import kotlin.streams.toList

/**
 * @author mehtasan
 */

fun main() {
    val objectMapper = createObjectMapper()
    runBlocking {
        val asyncs = IntStream.range(0, 3).mapToObj {
            GlobalScope.async {
                handleData("handler-$it", objectMapper)
            }
        }.toList()
        asyncs.awaitAll()
    }
}

private fun handleData(handlerName: String, objectMapper: ObjectMapper) {
    println("Handler: $handlerName")
    val maxNoMsgFound = 100
    var noMessageFound = 0
    val consumer = createConsumer()
    while (true) {
        val consumerRecords = consumer.poll(Duration.ofMillis(1000))
        if (consumerRecords.count() == 0) {
            noMessageFound += 1
            if (noMessageFound >= maxNoMsgFound) break
        }
        consumerRecords.forEach {
            println("${it.key()} = ${it.value()}")
        }
        consumer.commitAsync()
    }
    println("$handlerName: I am done")
    consumer.close()
}

private fun createConsumer(): KafkaConsumer<String, Double> {
    val props = Properties()
    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:10001,localhost:10002,localhost:10003"
    props[ConsumerConfig.GROUP_ID_CONFIG] = "ConsumerGroup1"
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = DoubleDeserializer::class.java.canonicalName
    props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1
    props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    val consumer = KafkaConsumer<String, Double>(props)
    consumer.subscribe(listOf("avg-price-per-sector"))
    return consumer
}


private fun createObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
}
