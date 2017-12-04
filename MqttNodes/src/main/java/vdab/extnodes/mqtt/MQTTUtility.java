package vdab.extnodes.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.lcrc.af.AnalysisCompoundData;
import com.lcrc.af.AnalysisData;
import com.lcrc.af.constants.ContainerDefaults;
import com.lcrc.af.file.AFFileUtility;

public class MQTTUtility {
	
	public static final String MQTT_URL_START = "tcp://";
	public static final String MQTT_PATH_DELIM = "/";
	public static final int MQTT_PATH_MIN_PARTS = 1;
	public static final String MQTT_PERSISTENCE_SUBDIR = "/mqtt";
	public static String buildBrokerURL(String server, Integer port){
		StringBuilder sb = new StringBuilder();
		sb.append(MQTT_URL_START);
		sb.append(server);
		sb.append(":");
		sb.append(port);
		return sb.toString();
	}	

	public static AnalysisCompoundData buildAnalysisData (String topic, String data){
		String[] parts = topic.split("/");
		int lastPart = parts.length -1;
		if (lastPart <= MQTT_PATH_MIN_PARTS)
			return null;
		AnalysisCompoundData acdNext = null;
		AnalysisCompoundData acdFirst = null;
		AnalysisCompoundData acdLast = null;
	

		for (int n = MQTT_PATH_MIN_PARTS; n <= lastPart; n++){
			if (n == lastPart){
				AnalysisData ad = new AnalysisData(parts[n], data);
				if (acdLast != null)
					acdLast.addAnalysisData(ad);
			}
			else {
				acdNext = new AnalysisCompoundData(parts[n]);
				if (acdLast != null)	
					acdLast.addAnalysisData(acdNext);	
			}
			acdLast = acdNext;
			if (acdFirst == null)
				acdFirst = acdNext;
		}
		return acdFirst;
	}
	public static String getDefaultPersistenceDirectory(){
		AFFileUtility.makeAFDirIfNeeded(ContainerDefaults.TMP_SUBDIR);
		return AFFileUtility.getAFDirPath(ContainerDefaults.TMP_SUBDIR)+ MQTT_PERSISTENCE_SUBDIR;
	}
	
	public static MqttClientPersistence createAndReturnPersistence(Integer type){
		switch (type.intValue()){
		case MQTTPersistenceType.MEMORY:
			return new MemoryPersistence();
					
		// These drop to use FILE			
		case MQTTPersistenceType.FILE:
		default:
			break;
		}
		return new MqttDefaultFilePersistence(getDefaultPersistenceDirectory());
	}
	public static String buildTopicPublishPath(String server, Integer port, String path, String devid){
		StringBuilder sb = new StringBuilder();

		sb.append(MQTT_PATH_DELIM);
		sb.append(path);
		sb.append(MQTT_PATH_DELIM);		
		sb.append(devid);
		return sb.toString();
	}		
	public static String buildTopicSubscribePath(String server, Integer port, String path, String devid){
		StringBuilder sb = new StringBuilder();
		sb.append(MQTT_PATH_DELIM);
		sb.append(path);
		if (!path.contains("#")&& devid != null){
			sb.append(MQTT_PATH_DELIM);
			sb.append(devid);
		}
		return sb.toString();
	}	
}
