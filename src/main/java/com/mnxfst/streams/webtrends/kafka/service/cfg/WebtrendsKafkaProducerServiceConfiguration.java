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

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Provides all necessary settings for initializing the webtrends kafka producer
 * @author mnxfst
 * @since 18.03.2014
 *
 */
@JsonRootName ( value = "configuration" )
public class WebtrendsKafkaProducerServiceConfiguration extends Configuration {
	
	@JsonProperty ( value = "webtrendsStreamListener", required = true )
	@NotNull
	private WebtrendsStreamListenerConfiguration webtrendsStreamListener = null;
	
	@JsonProperty ( value = "kafkaProducer", required = true )
	@NotNull
	private WebtrendsKafkaProducerConfiguration kafkaProducer = null;
	
	public WebtrendsKafkaProducerServiceConfiguration() {		
	}

	public WebtrendsStreamListenerConfiguration getWebtrendsStreamListener() {
		return webtrendsStreamListener;
	}

	public void setWebtrendsStreamListener(
			WebtrendsStreamListenerConfiguration webtrendsStreamListener) {
		this.webtrendsStreamListener = webtrendsStreamListener;
	}

	public WebtrendsKafkaProducerConfiguration getKafkaProducer() {
		return kafkaProducer;
	}

	public void setKafkaProducer(WebtrendsKafkaProducerConfiguration kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}
	
}
