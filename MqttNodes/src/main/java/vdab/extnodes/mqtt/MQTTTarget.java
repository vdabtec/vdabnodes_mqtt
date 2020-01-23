/*LICENSE*
 * Copyright (C) 2013 - 2018 MJA Technology LLC 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package vdab.extnodes.mqtt;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisTarget;
import com.lcrc.af.constants.IconCategory;
import com.lcrc.af.constants.LogLevel;
import com.lcrc.af.util.AnalysisDataUtility;
import com.lcrc.af.util.StringUtility;

public class MQTTTarget extends AnalysisTarget implements MqttCallback{
	static {
		MQTTQualityOfService.getEnum();
		MQTTPersistenceType.getEnum();
	}
	private static final int MAXFAILURES = 4;
	private int c_NoFailures = 0;
	private String c_BrokerServer;
	private Integer c_BrokerPort;
	private String c_User; 
	private String c_Password; ;
	private String c_DevicePath;
	private String c_DeviceId;
	private String c_TopicPath;
	private Integer c_PersistenceType = Integer.valueOf(MQTTPersistenceType.FILE);
	private MqttClientPersistence c_ClientPersistence;
	private Integer c_Qos = Integer.valueOf(MQTTQualityOfService.ATMOSTONCE);
	private HashMap<String, MqttTopic> c_map_MqttTopics = new HashMap<String, MqttTopic>();

	MqttConnectOptions c_ConnectOptions; 
	MqttClient c_ClientConnection;
	MqttTopic c_Topic;

	// ATTRIBUTES ---------------------------------------------------
	public Integer get_IconCode(){
		return IconCategory.NODE_MQTT;
	}
	public String get_BrokerServer(){
		return c_BrokerServer;
	}
	public void set_BrokerServer(String server){
		c_BrokerServer= server;
	}
	public Integer get_BrokerPort(){
		return c_BrokerPort;
	}
	public void set_BrokerPort(Integer port){
		c_BrokerPort = port;
	}
	public String get_User(){
		return c_User;
	}
	public void set_User( String user){
		c_User = user;
	}
	public String get_Password(){
		return c_Password;
	}
	public void set_Password( String password){
		c_Password = password;
	}
	public void set_Topic(String topic){
		c_TopicPath = topic;
	}
	public String get_Topic(){
		return c_TopicPath;
	}
	public void set_DevicePath(String path){
		c_DevicePath = path;
	}
	public String get_DevicePath(){
		return c_DevicePath ;
	}
	public String get_DeviceId(){
		return c_DeviceId;
	}
	public void set_DeviceId( String id){
		c_DeviceId = id;
	}
	public Integer get_QOS(){
		return c_Qos;
	}
	public void set_QOS( Integer qos){
		c_Qos = qos;
	}
	public Integer get_PersistenceType(){
		return c_PersistenceType;
	}
	public void set_PersistenceType(Integer type){
		c_PersistenceType = type;
	}
	public String get_TopicInfo(){
		if (c_TopicPath != null)
			return c_TopicPath;
		return MQTTUtility.buildTopicPublishPath(c_BrokerServer, c_BrokerPort, c_DevicePath, c_DeviceId);
	}
	// ANALYSIS NODE Methods -----------------------------------------
	public void _init(){
		super._init();
	}
	public void _reset(){
		disconnect();
		super._reset();
		connectToMQTT();
	}
	public void _start(){
		disconnect();
		connectToMQTT();
		super._start();
	}
	public void _stop(){
		disconnect();
		super._stop();
	}
	public synchronized void processEvent(AnalysisEvent ev){
		if (ev.isTriggerEvent())
			return;
		AnalysisData ad =  getSelectedData(ev.getAnalysisData());
		if (ad == null) // Not the selected data.
			return;
		String txt = null;
		if (ad.isSimple())
			txt = ad.getDataAsString();
		else
			txt = ad.getDataAsJson();
		
		MqttMessage msg = new MqttMessage(txt.getBytes());
		msg.setQos(c_Qos.intValue());
		msg.setRetained(false);
		MqttDeliveryToken token = null;
		try {
			// publish message to broker
			try {
				MqttTopic topic = getCurrentTopic(ev);
				token = topic.publish(msg);
			}
			catch (Exception e) {

				if (c_NoFailures > MAXFAILURES)
					throw e;
				c_NoFailures++;
				setWarning("MQTT PUBLISHING failure, one event droppered, NOFAILURES="+c_NoFailures+" e>"+e);
				disconnect();
				connectToMQTT();
				return;
			}

			if (isLogLevel(LogLevel.TRACE))
				logTrace("MQTT PUBLISHING data TOPIC="+c_Topic+" QOS="+c_Qos.intValue()+" MSG="+msg);

			// Wait until the message has been delivered to the broker
			token.waitForCompletion();
		} 
		catch (Exception e) {
			setError("MQTT PUBLISHER: Failed sending message to the broker. Max retries reached. e>"+e);
			_disable();

		}
	}
	// SUPPORTING Methods -----------------------------------------------
	private MqttTopic getCurrentTopic(AnalysisEvent ev){
		
		String topicPath ;
		// LATEST - Support single TOPIC attribute which can include tags.
		if (c_TopicPath != null){
			if (StringUtility.hasTags(c_TopicPath)){
				HashMap<String,String> tvMap = AnalysisDataUtility.buildTagValueMap(ev.getAnalysisData());
				// Build body with tags if necessary
				topicPath  = getTemplateAttribute( "Topic", tvMap);
			}
			else {
				topicPath = c_TopicPath;
			}
		}
		// DEPRECATE - Old two part device defined path;
		else {
			topicPath = MQTTUtility.buildTopicPublishPath(c_BrokerServer, c_BrokerPort, c_DevicePath, c_DeviceId);
		}
				
		MqttTopic topic = c_map_MqttTopics.get(topicPath);
		if (topic != null)
			return topic;
		
		try {
			topic = c_ClientConnection.getTopic(topicPath);
			c_map_MqttTopics.put(topicPath, topic);
			if (isLogLevel(LogLevel.TRACE))
				logTrace("MQTT PUBLISHER connecting to TOPIC="+topicPath+"QOS="+c_Qos.intValue());
		}
		catch (Exception e){
			setError("MQTT PUBLISHER unable to connect to TOPIC="+topicPath+" e>"+e);
		}
		return topic;
	}
	private void connectToMQTT(){
		try {
			c_map_MqttTopics.clear();
			c_ConnectOptions = new MqttConnectOptions(); 
			c_ConnectOptions.setCleanSession(true); 
			c_ConnectOptions.setKeepAliveInterval(30); 
			if (c_User != null){
				c_ConnectOptions.setUserName(c_User); 
				c_ConnectOptions.setPassword(c_Password.toCharArray()); 
			}
			c_ClientConnection = new MqttClient(MQTTUtility.buildBrokerURL(c_BrokerServer,c_BrokerPort), getObjectPublicKey(), c_ClientPersistence); 
			c_ClientConnection.connect(c_ConnectOptions); 
		} 
		catch (Exception e) { 
			setError("MQTT PUBLISHER failed connection to server e>"+e);
			_disable();
			return; 
		}
	}
	private void disconnect(){
		c_map_MqttTopics.clear();
		try {
			if (c_ClientConnection != null){
				c_ClientConnection.disconnect();
				c_ClientConnection.close();

			}
		}
		catch (Exception e){}
		c_ClientConnection = null;
	}
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void messageArrived(String topicName, MqttMessage msg) throws Exception {
		//	String msgStr = new String(msg.getPayload());
		//	publish(new AnalysisEvent(MQTTSource.this, new AnalysisData(topicName, msgStr)));

	}
}
