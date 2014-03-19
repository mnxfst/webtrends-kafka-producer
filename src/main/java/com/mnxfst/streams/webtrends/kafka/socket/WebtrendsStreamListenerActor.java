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
package com.mnxfst.streams.webtrends.kafka.socket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.mnxfst.streams.webtrends.kafka.service.cfg.WebtrendsStreamListenerConfiguration;

/**
 * Ramps up a web-socket listener which receives all inbound traffic and forwards them toa configured
 * receiving unit
 * @author mnxfst
 * @since 18.03.2014
 *
 */
@WebSocket
public class WebtrendsStreamListenerActor extends UntypedActor {

	private final String authAudience;
	private final String authScope;
	private final String authUrl;
	private final String clientId;
	private final String clientSecret;
	private final String streamType;
	private final String streamQuery;
	private final String streamVersion;
	private final String schemaVersion;
	private final String eventStreamUrl;

	private String oAuthToken;

	final ActorRef messageForwarderRef;
		  
	private final CountDownLatch latch = new CountDownLatch(1);
	
	private WebSocketClient webtrendsStreamSocketClient = null;

	/**
	 * Initializes the socket using the provided input
	 * @param configuration
	 * @param messageForwarderRef reference towards the message forwarding actor
	 */
	public WebtrendsStreamListenerActor(final WebtrendsStreamListenerConfiguration configuration, final ActorRef messageForwarderRef) {

		this.authAudience = configuration.getAuthAudience();
		this.authScope = configuration.getAuthScope();
		this.authUrl = configuration.getAuthUrl();
		this.eventStreamUrl = configuration.getEventStreamUrl();
		this.clientId = configuration.getClientId();
		this.clientSecret = configuration.getClientSecret();
		this.streamType = configuration.getStreamType();
		this.streamQuery = configuration.getStreamQuery();
		this.streamVersion = configuration.getStreamVersion();
		this.schemaVersion = configuration.getSchemaVersion();
		
		this.messageForwarderRef = messageForwarderRef;
	}
	
	/**
	 * Establishes a connection with the webtrends stream api
	 * @see akka.actor.UntypedActor#preStart()
	 */
	public void preStart() throws Exception {
		
		// authenticate with the webtrends service
		WebtrendsTokenRequest tokenRequest = new WebtrendsTokenRequest(this.authUrl, this.authAudience, this.authScope, this.clientId, this.clientSecret);
		this.oAuthToken = tokenRequest.execute();		
		
		// initialize the webtrends stream socket client and connect the listener
		this.webtrendsStreamSocketClient = new WebSocketClient();
		try {
			this.webtrendsStreamSocketClient.start();
			ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
			this.webtrendsStreamSocketClient.connect(this, new URI(this.eventStreamUrl), upgradeRequest);
			await(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			throw new RuntimeException("Unable to connect to web socket: " + e.getMessage(), e);
		}		
	}
	
	/**
	 * @see akka.actor.UntypedActor#postStop()
	 */
	public void postStop() throws Exception {
		try {
			this.webtrendsStreamSocketClient.stop();
		} catch(Exception e) {
			context().system().log().error("Failed to shutdown web-socket listener. Error: " + e.getMessage());
		}
	}

	/**
	 * Executed after establishing web socket connection with streams api
	 * @param session
	 */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		
		// build SAPI query object
		final StringBuilder sb = new StringBuilder();
		sb.append("{\"access_token\":\"");
	    sb.append(oAuthToken);
	    sb.append("\",\"command\":\"stream\"");
	    sb.append(",\"stream_type\":\"");
	    sb.append(streamType);
	    sb.append("\",\"query\":\"");
	    sb.append(streamQuery);
	    sb.append("\",\"api_version\":\"");
	    sb.append(streamVersion);
	    sb.append("\",\"schema_version\":\"");
	    sb.append(schemaVersion);
	    sb.append("\"}");

	    try {
	    	session.getRemote().sendString(sb.toString());
	    	context().system().log().info("WebTrends Streams API reader connected");
	    } catch(IOException e) {
	    	throw new RuntimeException("Unable to open stream", e);
	    }
	    
    	
	}

	/**
	 * Executed by web socket implementation when receiving a message from the
	 * streams api. The message will be directly handed over to the configured 
	 * {@link ActorRef message forwarder} which emits it into a kafka topic 
	 * @param message
	 */
	@OnWebSocketMessage
	public void onMessage(String message) {
		messageForwarderRef.tell(message, getSelf());
	}

	/**
	 * Executed when closing the web socket connection
	 * @param statusCode
	 * @param reason
	 */
	@OnWebSocketClose		    
	public void onClose(int statusCode, String reason) {
		context().system().log().info("websocket closing[status="+statusCode+", reason="+reason+"]");
	}

	/**
	 * Timeout handler
	 * @param duration
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean await(int duration, TimeUnit unit) throws InterruptedException {
		return latch.await(duration, unit);
	}
	
	/**
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		unhandled(message); // TODO 
	}

}
