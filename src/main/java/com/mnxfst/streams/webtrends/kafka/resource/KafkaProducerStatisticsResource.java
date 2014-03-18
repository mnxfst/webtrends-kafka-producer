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
package com.mnxfst.streams.webtrends.kafka.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.mnxfst.streams.webtrends.kafka.producer.WebtrendsKafkaProducer;

/**
 * Provides a set of collected statistics about the {@link WebtrendsKafkaProducer kafka producer}
 * @author mnxfst
 * @since 18.03.2014
 */
@Path ( "/stats/kafka")
public class KafkaProducerStatisticsResource {

	@GET
	@Produces( "application/json" )
	public String getStats() {
		return "STATS";
	}
	
}
