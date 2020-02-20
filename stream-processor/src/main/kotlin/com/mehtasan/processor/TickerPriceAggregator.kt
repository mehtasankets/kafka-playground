package com.mehtasan.processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit


/**
 * @author mehtasan
 */

fun main() {
    val builder = StreamsBuilder()
    val topology = createTopology(builder)
    createKafkaStreams(topology).start()
}

private fun createKafkaStreams(topology: Topology): KafkaStreams {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "ticker-price-aggregator"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:10001,localhost:10002,localhost:10003"
    return KafkaStreams(topology, props)
}

private fun createTopology(builder: StreamsBuilder): Topology {
    val objectMapper = createObjectMapper()
    builder.stream("ticker-price", Consumed.with(Serdes.String(), Serdes.String()))
        .map { _, value ->
            val data = objectMapper.readValue<TickerDetails>(value)
            KeyValue(data.sector.name, data.price)
        }.peek {k, v ->
            println("$k = $v")
        }.groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
        .reduce { v1, v2 -> v1 + v2 / 2 }.toStream().peek { k, v ->
            println("\t\tPost reduce: $k = $v")
        }.to("avg-price-per-sector", Produced.with(Serdes.String(), Serdes.Double()))
    return builder.build()

}

private fun createObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
}