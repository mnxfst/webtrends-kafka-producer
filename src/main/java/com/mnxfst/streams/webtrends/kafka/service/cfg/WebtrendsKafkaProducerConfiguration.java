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

package com.mnxfst.streams.webtrends.kafka.service.cfg;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.mnxfst.streams.webtrends.kafka.producer.WebtrendsKafkaProducer;

/**
 * Provides all required settings for initializing and ramping up an instance of type {@link WebtrendsKafkaProducer}
 * @author mnxfst
 * @since 18.03.2014
 *
 */
@JsonRootName ( value = "kafkaProducer" )
public class WebtrendsKafkaProducerConfiguration implements Serializable {

	private static final long serialVersionUID = 1285137864117247674L;
	
	/** class used for serializing message emitted into kafka topic */
	@JsonProperty ( value = "serializerClass", required = true )
	@NotNull
	private String serializerClass = null;
	
	/** class holding the implementation deciding upon which topic partition to write messages to */
	@JsonProperty ( value = "partitionerClass", required = false )
	private String partitionerClass = null;
	
	/** zookeeper connection string */
	@JsonProperty ( value = "zookeeperConnect", required = true )
	@NotNull
	private String zookeeperConnect = null;

	/** kafka broker connection string */
	@JsonProperty ( value = "kafkaBrokers", required = true )
	@NotNull
	private String kafkaBrokers = null;

	/** kafka topic receiving all inbound traffic */
	@JsonProperty ( value = "topic", required = true )
	@NotNull
	private String topic = null;
	
	/** number of producer instances - should correlate with the number of partitions per topic */
	@JsonProperty ( value = "numOfProducers", required = true )
	@NotNull
	private int numOfProducers = 1;
	
	/**
	 * Default constructor
	 */
	public WebtrendsKafkaProducerConfiguration() {		
	}

	public String getSerializerClass() {
		return serializerClass;
	}

	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}

	public String getPartitionerClass() {
		return partitionerClass;
	}

	public void setPartitionerClass(String partitionerClass) {
		this.partitionerClass = partitionerClass;
	}

	public String getZookeeperConnect() {
		return zookeeperConnect;
	}

	public void setZookeeperConnect(String zookeeperConnect) {
		this.zookeeperConnect = zookeeperConnect;
	}

	public String getKafkaBrokers() {
		return kafkaBrokers;
	}

	public void setKafkaBrokers(String kafkaBrokers) {
		this.kafkaBrokers = kafkaBrokers;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getNumOfProducers() {
		return numOfProducers;
	}

	public void setNumOfProducers(int numOfProducers) {
		this.numOfProducers = numOfProducers;
	}

	
}
