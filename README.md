# Dynamixel

### Instructions - Sept 2021

### Started using github desktop app.

### the u2d2 wasn't being noticed by the com port - had to download this driver 
### and then it worked -> https://ftdichip.com/drivers/vcp-drivers/ (found this driver from -> https://www.robotis.us/u2d2/)
### another nice USB tool from Microsoft -> 'USB Device Viewer' (didn't really solve the problem but might help get visibility in future situations).
###
### In these instructions https://emanual.robotis.com/docs/en/software/dynamixel/dynamixel_sdk/library_setup/java_windows/#java-windows
###     under the 'Import Function Reference' it talks about 'configure build path'. This means to set the configuration -> sources within netbeans.
###         right click on the project -> set configuration -> customize -> add a 'Source package folder'
###         the folder to be added is a download from dynamixel -> C:\Users\allan\Documents\allan_github\Dynamixel-master\DynamixelSDK-3.7.31\c\build\win64\output
###         If these instructions are not followed you will get a 'win-64 output error' under resolve project errors. 
###            (the DynamixelSDK-3.7.31 needs to be in a folder in the project and this error will go away(.)

### jna-5.8.0.jar
### had to use Netbeans 12.4 with Java 16 because Netbeans 12.0 was giving an error with the JNA jar. -> https://issues.apache.org/jira/browse/NETBEANS-5641
### 
### The printTxRxResult had been deprecated so made an update from this commit to use System.out.println(dynamixel.getRxPacketError instead of dynamixel.printTxRxResult(
### https://github.com/mturktest123/DynamixelSDK/commit/0747488b71bfbd1da183b8417b5a6f9600700a3d#diff-5e3ce7ac88bb05c2f6f3f7dce7aafc7557497c0857d118f6570c831eb89d8cd8
### 

### A useful tool -> 'Dynamixel Wizard 2.0'
### 
### https://www.automationdirect.com/adc/shopping/catalog/power_transmission_(mechanical)/precision_gearboxes_for_small_nema_motors/nema_23_frame
###

### Phidgets requires some libraries to be installed (in addition to the jar). Follow this link:
### https://www.phidgets.com/docs/OS_-_Windows#Quick_Downloads
### Phidgets Control Panel will start in the background(??) and not close on it's own - this will cause java crashes that don't give a good error message)
### To fix the problem - go into task manager and 'end task' on the phidget control panel.
###
### Under Properties-Sources-Source/Binary Format select JDK 16; Profile Full JRE; Encoding UTF-8

### Extra Information for future - not needed for now.
### https://community.automationdirect.com/s/global-search/javahttps://community.automationdirect.com/s/global-search/java
### If you are using Autodirect PLC that supports modbus (all of them?) then get the free library for talking modbus over serial and ethernet for java. That way you will get more cross platform code in addition.
###
