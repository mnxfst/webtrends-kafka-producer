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

package com.mnxfst.streams.webtrends.kafka.service;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinRouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mnxfst.streams.webtrends.kafka.producer.WebtrendsKafkaProducer;
import com.mnxfst.streams.webtrends.kafka.resource.KafkaProducerStatisticsResource;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsKafkaProducerConfiguration;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsKafkaProducerServiceConfiguration;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsStreamListenerConfiguration;
import com.mnxfst.streams.webtrends.kafka.socket.WebtrendsStreamListenerActor;

/**
 * Main class being in charge for initializing and ramping up the service
 * @author mnxfst
 * @since 18.03.2014
 *
 */
public class WebtrendsKafkaProducerService extends Application<WebtrendsKafkaProducerServiceConfiguration> {

	private ActorSystem actorSystem = null;
	private ActorRef webtrendsKafkaProducerRef = null;
	private ActorRef webtrendsStreamListenerRef = null;
	


	/**
	 * @see com.yammer.dropwizard.Service#initialize(com.yammer.dropwizard.config.Bootstrap)
	 */
	public void initialize(Bootstrap<WebtrendsKafkaProducerServiceConfiguration> bootstrap) {
	}

	/**
	 * @see com.yammer.dropwizard.Service#run(com.yammer.dropwizard.config.Configuration, com.yammer.dropwizard.config.Environment)
	 */
	public void run(WebtrendsKafkaProducerServiceConfiguration configuration, Environment environment) throws Exception {
		
		this.actorSystem = ActorSystem.create("webtrends-kafka-producer");
		
		if(configuration.getKafkaProducer().getNumOfProducers() > 1) {
			this.webtrendsKafkaProducerRef = actorSystem.actorOf(
				Props.create(
						WebtrendsKafkaProducer.class, configuration.getKafkaProducer()).withRouter(new RoundRobinRouter(configuration.getKafkaProducer().getNumOfProducers())));
		} else {
			this.webtrendsKafkaProducerRef = actorSystem.actorOf(
					Props.create(
							WebtrendsKafkaProducer.class, configuration.getKafkaProducer()));
		}

		this.webtrendsStreamListenerRef = actorSystem.actorOf(Props.create(WebtrendsStreamListenerActor.class, configuration.getWebtrendsStreamListener(), webtrendsKafkaProducerRef));
		
		environment.jersey().register(new KafkaProducerStatisticsResource());
	}
	
	public static void main(String[] args) throws Exception {
		new WebtrendsKafkaProducerService().run(args);
	}

}
