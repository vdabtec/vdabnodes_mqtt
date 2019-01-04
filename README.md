# VDAB MQTT Nodes
### Overview
With VDAB's distributed architecture, visual processing paradigm and IoT processing 
capabilities it can serve as a powerful <b>IoT Processing Hub</b> 

| | |
|  --- |  :---: |
| Application Page    | [IoT Processing Hub](https://vdabtec.com/vdab/app-guides/iot-processing-hub) |
| Demo Web Link   | [pi-demo.vdabsoft.com:31154/vdab](http://pi-demo.vdabsoft.com:31154/vdab) |

### Features
<ul>
<li>The <i>MQTTSource</i> can listen to one or more topics from a MQTT Server.
<li>The <i>MQTTTarget</i> allows publishing VDAB data directly to any MQTT topic.
<li>The <i>FromJSON</i> node is used to parse MQTT data that is represented as JSON.
</ul>

### Licensing
Use of this software is subject to restrictions of the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

This software makes us of the following components which include their own licensing restrictions:

| | | 
|  --- |  :---: |
| MQTT Java Library - Eclipse | [Eclipse Distribution License v1](https://projects.eclipse.org/content/eclipse-distribution-license-1.0-bsd) |
| MQTT Java Library - Eclipse | [Eclipse Public License v1](https://projects.eclipse.org/content/eclipse-public-license-1.0) |

### Loading the the Package
The current or standard version can be loaded directly using the VDAB Android Client following the directions
for [Adding Packages](https://vdabtec.com/vdab/docs/VDABGUIDE_AddingPackages.pdf) 
and selecting the <i>MqttNodes</i> package.
 
A custom version can be built using Gradle following the direction below.

* Clone or Download this project from Github.
* Open a command windows from the <i>MqttNodes</i> directory.
* Build using Gradle: <pre>      gradle vdabPackage</pre>

This builds a package zip file which contains the components that need to be deployed. These can be deployed by 
manually unzipping these files as detailed in the [Server Updates](https://vdabtec.com/vdab/docs/VDABGUIDE_ServerUpdates.pdf) 
 documentation.

### Known Issues as of 24 Oct  2018

* The MQTT source and target nodes do not currently support authentication. 


