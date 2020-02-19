package com.mehtasan.producer

import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster

/**
 * @author mehtasan
 */
class SectorPartitioner: Partitioner {

    private val partitions = 6

    override fun configure(configs: MutableMap<String, *>?) {
        // Empty declaration
    }

    override fun close() {
        // Empty declaration
    }

    override fun partition(
        topic: String?, key: Any?, keyBytes: ByteArray?, value: Any?, valueBytes: ByteArray?, cluster: Cluster?
    ) = Sector.valueOf(key.toString()).id % partitions
}