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
