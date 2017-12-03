package vdab.extnodes.mqtt;

import com.lcrc.af.datatypes.AFEnum;

public class MQTTQualityOfService {
	public static final int ATMOSTONCE = 0;
	public static final int ATLEASTONCE = 1;
	public static final int EXACTLYONCE = 2;
	private static AFEnum s_EnumQualityOfService = new AFEnum("MQTTQualityOfService")
	.addEntry(ATMOSTONCE, "At Most Once")
	.addEntry(ATLEASTONCE, "At Least Once")
	.addEntry(EXACTLYONCE, "Exactly Once");
	public static AFEnum getEnum(){
		return s_EnumQualityOfService ;
	}
}
