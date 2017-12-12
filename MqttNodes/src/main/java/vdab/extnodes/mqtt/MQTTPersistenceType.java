package vdab.extnodes.mqtt;



import com.lcrc.af.datatypes.AFEnum;

public class MQTTPersistenceType {
	public static final int MEMORY = 0;
	public static final int FILE = 1;
	private static AFEnum s_EnumPersistenceType = new AFEnum("MQTTPersistenceType")
	.addEntry(MEMORY, "Memory")
	.addEntry(FILE, "File");
	public static AFEnum getEnum(){
		return s_EnumPersistenceType ;
	}
}
