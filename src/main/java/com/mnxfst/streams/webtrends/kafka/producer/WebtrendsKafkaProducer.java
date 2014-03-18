/**
 *  Copyright 2014 Christian Kreutzfeldt
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mnxfst.streams.webtrends.kafka.producer;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.lang3.StringUtils;

import akka.actor.UntypedActor;

import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsKafkaProducerConfiguration;
import com.mnxfst.streams.webtrends.kafka.socket.WebtrendsStreamListenerActor;

/**
 * Establishes a connection with the kafka infrastructure and emits inbound webtrends messages to specified topics. 
 * The message is expected to be of type string as it was received by the {@link WebtrendsStreamListenerActor webtrends stream listener}.
 * No pre-processing happens here, the producer simply forwards the message. 
 * @author mnxfst
 * @since Mar 14, 2014
 *
 */
public class WebtrendsKafkaProducer extends UntypedActor {

	public static final String CFG_SERIALIZER_CLASS = "serializer.class";
	public static final String CFG_PARTITIONER_CLASS = "partitioner.class";
	public static final String CFG_ZOOKEEPER_CONNECT = "zookeeper.connect";
	public static final String CFG_BROKER_LIST = "metadata.broker.list";
	
	private final WebtrendsKafkaProducerConfiguration configuration;
	private String topic;
	private Producer<Integer, String> producer;

	/**
	 * Initializes the producer using the provided input
	 * @param configuration
	 */
	public WebtrendsKafkaProducer(final WebtrendsKafkaProducerConfiguration configuration) {
		this.configuration = configuration;
		this.topic = configuration.getTopic();
	}
	
	/**
	 * @see akka.actor.UntypedActor#preStart()
	 */
	public void preStart() throws Exception {

		Properties properties = new Properties();
		properties.put(CFG_BROKER_LIST, configuration.getKafkaBrokers());
		properties.put(CFG_SERIALIZER_CLASS, configuration.getSerializerClass());
		properties.put(CFG_ZOOKEEPER_CONNECT, configuration.getZookeeperConnect());
		if(StringUtils.isNotBlank(configuration.getPartitionerClass()))
			properties.put(CFG_PARTITIONER_CLASS, configuration.getPartitionerClass());

		producer = new Producer<>(new ProducerConfig(properties));
	}
	
	/**
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if(message instanceof String) {
			producer.send(new KeyedMessage<Integer, String>(this.topic, (String)message));
		} else {
			unhandled(message);
		}
	}
}
