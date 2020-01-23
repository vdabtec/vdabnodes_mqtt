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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisSource;
import com.lcrc.af.constants.IconCategory;
import com.lcrc.af.constants.LogLevel;

public class MQTTSource extends AnalysisSource implements MqttCallback {
	static {
		MQTTQualityOfService.getEnum();
		MQTTPersistenceType.getEnum();
	}
	private String c_BrokerServer;
	private Integer c_BrokerPort;
	private String c_User; 
	private String c_Password; ;
	private String c_DeviceId;
	private String c_DevicePath;
	private String c_TopicPath;
	private Integer c_PersistenceType = Integer.valueOf(MQTTPersistenceType.FILE);
	private MqttClientPersistence c_ClientPersistence;
	private Integer c_Qos = Integer.valueOf(MQTTQualityOfService.ATMOSTONCE);
	
	MqttConnectOptions c_ConnectOptions; 
	MqttClient c_ClientConnection;
	MqttTopic c_Topic;

	public MQTTSource(){
		super();
	}

	// ATTRIBUTE Methods
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
	// ANALYSIS NODE Methods -------------------------------
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
	// SUPPORTING Methods --------------------------------------
	private void connectToMQTT(){
		try {
			c_ConnectOptions = new MqttConnectOptions(); 
			c_ConnectOptions.setCleanSession(true); 
			c_ConnectOptions.setKeepAliveInterval(30); 
			if (c_User != null){
				c_ConnectOptions.setUserName(c_User); 
				c_ConnectOptions.setPassword(c_Password.toCharArray()); 
			}
			c_ClientPersistence = MQTTUtility.createAndReturnPersistence(c_PersistenceType);
			c_ClientConnection = new MqttClient(MQTTUtility.buildBrokerURL(c_BrokerServer,c_BrokerPort),getObjectPublicKey(), c_ClientPersistence ); 
			c_ClientConnection.setCallback(this); 
			c_ClientConnection.connect(c_ConnectOptions); 
			if (c_TopicPath == null)
				c_TopicPath = MQTTUtility.buildTopicSubscribePath(c_BrokerServer, c_BrokerPort, c_DevicePath, c_DeviceId);	
			c_ClientConnection.subscribe(c_TopicPath , c_Qos.intValue());
			if (isLogLevel(LogLevel.TRACE))
				logTrace("MQTT SUBSCRIBER connecting to TOPIC="+c_TopicPath+" QOS="+c_Qos.intValue());
		} 
		catch (Exception e) { 
			setError("MQTT SUBSCRIBER Failed connection to Server e>"+e);
			_disable();
			return; 
		}
	}
	private void disconnect(){
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
	public synchronized void messageArrived(String topicName, MqttMessage msg) throws Exception {
		try {
			String msgStr = new String(msg.getPayload());
			/* HACKALERT - This fails with a null pointer exception
			if (MQTTSource.this.isLogLevel(LogLevel.TRACE))
			MQTTSource.this.logTrace("MQTT SUBSCRIBER received MSG="+msgStr+" TOPIC="+topicName);	
			 */
			AnalysisData ad = MQTTUtility.buildAnalysisData(topicName, msgStr);
			if (ad != null){
				publish(new AnalysisEvent(MQTTSource.this, ad));

			}
			else {
				setWarning("MQTT could not parse the topic TOPIC="+topicName);
			}
		}
		catch (Exception e){
			setError("MQTT SUBSCRIBER Received message error e>"+e);
			throw e;
		}
	}
}
