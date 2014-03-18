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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinRouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mnxfst.streams.webtrends.kafka.producer.WebtrendsKafkaProducer;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsKafkaProducerConfiguration;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsKafkaProducerServiceConfiguration;
import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsStreamListenerConfiguration;
import com.mnxfst.streams.webtrends.kafka.socket.WebtrendsStreamListenerActor;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * Main class being in charge for initializing and ramping up the service
 * @author mnxfst
 * @since 18.03.2014
 *
 */
public class WebtrendsKafkaProducerService extends Service<WebtrendsKafkaProducerServiceConfiguration> {

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
		
	}
	
	public static void main(String[] args) throws Exception {
		new WebtrendsKafkaProducerService().run(args);
		
		WebtrendsKafkaProducerServiceConfiguration cfg = new WebtrendsKafkaProducerServiceConfiguration();
		WebtrendsKafkaProducerConfiguration prodCfg = new WebtrendsKafkaProducerConfiguration();
		prodCfg.setKafkaBrokers("localhost:9092");
		prodCfg.setNumOfProducers(1);
		prodCfg.setSerializerClass("kafka.serializer.StringEncoder");
		prodCfg.setZookeeperConnect("localhost:2181");
		cfg.setKafkaProducer(prodCfg);
		WebtrendsStreamListenerConfiguration listenerCfg = new WebtrendsStreamListenerConfiguration();
		listenerCfg.setAuthAudience("auth.webtrends.com");
		listenerCfg.setAuthScope("sapi.webtrends.com");
		listenerCfg.setAuthUrl("https://sauth.webtrends.com/v1/token");
		listenerCfg.setClientId("xxxxxxxxx");
		listenerCfg.setClientSecret("xxxxxxxxxx");
		listenerCfg.setEventStreamUrl("ws://sapi.webtrends.com/streaming");
		listenerCfg.setSchemaVersion("2.1");
		listenerCfg.setStreamVersion("2.1");
		listenerCfg.setStreamQuery("select *");
		listenerCfg.setStreamType("return_all");
		cfg.setWebtrendsStreamListener(listenerCfg);
		cfg.setHttpConfiguration(null);
		cfg.setLoggingConfiguration(null);
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		System.out.println(mapper.writeValueAsString(cfg));
	}
}
