/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamixel;

import static java.lang.Thread.sleep;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadWrite
{
  public static void main(String[] args)
  {
    // https://github.com/ROBOTIS-GIT/emanual/blob/master/docs/en/software/dynamixel/dynamixel_sdk/sample_code/java/java_read_write_protocol_1_0.md
    // Control table address -> http://emanual.robotis.com/
    short ADDR_MX_TORQUE_ENABLE         = 24;                  // Control table address is different in Dynamixel model
    short ADDR_MX_GOAL_POSITION         = 30;//182;
    short ADDR_MX_PRESENT_POSITION      = 36;
    short ADDR_MX_TORQUE                = 14; // address to set Torque. 
    int Target_Torque                   = 100; // between 0 and 1023

    // Protocol version
    int PROTOCOL_VERSION                = 1;                   // See which protocol version is used in the Dynamixel

    // Default setting
    byte DXL_ID                         = 6;                   // Dynamixel ID: 1
    int BAUDRATE                        = 57142;//1000000;
    String DEVICENAME                   = "COM3";  //linux -> "/dev/ttyUSB0";              // Check which port is being used on your controller
                                                               // ex) "COM1"   Linux: "/dev/ttyUSB0"

    byte TORQUE_ENABLE                  = 1;                   // Value for enabling the torque
    byte TORQUE_DISABLE                 = 0;                   // Value for disabling the torque
    short DXL_MINIMUM_POSITION_VALUE    = 120;                 // Dynamixel will rotate between this value
    short DXL_MAXIMUM_POSITION_VALUE    = 180;                // and this value (note that the Dynamixel would not move when the position value is out of movable range. Check e-manual about the range of the Dynamixel you use.)
    int DXL_MOVING_STATUS_THRESHOLD     = 10;                  // Dynamixel moving status threshold

    String KEY_FOR_ESCAPE               = "e";                 // Key for escape

    int COMM_SUCCESS                    = 0;                   // Communication Success result value
    int COMM_TX_FAIL                    = -1001;               // Communication Tx Failed

    
    // Instead of getch
    Scanner scanner = new Scanner(System.in);
    
    // Initialize Dynamixel class for java
    Dynamixel dynamixel = new Dynamixel();


    System.out.println("https://github.com/ROBOTIS-GIT/DynamixelSDK/blob/master/java/dynamixel_functions_java/x64/Dynamixel.java");
    System.out.println("Instructions: ");
    System.out.println("1) run 'RoboPlus' - icon on desktop ");
    System.out.println("2) select 'Dynamixel Wizard' within RoboPlus ");
    System.out.println("3) click the icon to 'open port' within Dynamixel Wizard");
    System.out.println("4) this will bring up a window that allows you to 'start searching'");
    System.out.println("5) Close the Dynamixel Wizard window and run this app");

    System.out.println("port handler: "+dynamixel.portHandler(DEVICENAME));
    // Initialize PortHandler Structs
    // Set the port path
    // Get methods and members of PortHandlerLinux or PortHandlerWindows
    int port_num = dynamixel.portHandler(DEVICENAME);

    
    // Initialize PacketHandler Structs
    dynamixel.packetHandler();

    int dxl_model_number = dynamixel.pingGetModelNum(port_num, PROTOCOL_VERSION, DXL_ID);
    System.out.println("dxl_model_number: "+dxl_model_number);
    //dynamixel.closePort(port_num);
    int index = 0;
    int dxl_comm_result = COMM_TX_FAIL;                        // Communication result
    short[] dxl_goal_position = new short[]{DXL_MINIMUM_POSITION_VALUE, DXL_MAXIMUM_POSITION_VALUE};         // Goal position

    byte dxl_error = 0;                                        // Dynamixel error
    short dxl_present_position = 0;                            // Present position

//       dynamixel.clearPort(0);
//       dynamixel.closePort(0);
       dynamixel.setBaudRate(0, 571);
       System.err.println(dynamixel.getBaudRate(0));
       dynamixel.setBaudRate(0, 57142);
       System.err.println(dynamixel.getBaudRate(0));
      try {
          sleep(2);
      } catch (InterruptedException ex) {
          Logger.getLogger(ReadWrite.class.getName()).log(Level.SEVERE, null, ex);
      }
System.err.println("portName? "+dynamixel.libFunction.getPortName(port_num));
    System.err.println("port_num? "+port_num+" "+dynamixel.openPort(port_num));
    dynamixel.openPort(port_num);
    // Open port
//    if (dynamixel.openPort(port_num))
//    {
//      System.out.println("Succeeded to open the port!");
//    }
//    else
//    {
//      System.out.println("Failed to open the port! "+port_num);
//      System.out.println("Press any key to terminate...");
//      //scanner.nextLine();
//      return;
//    }

    // Set port baudrate
//    dynamixel.setBaudRate(port_num, BAUDRATE);
    if (dynamixel.setBaudRate(port_num, BAUDRATE))
    {
      System.out.println("Succeeded to change the baudrate!");
    }
    else
    {
      System.out.println("Failed to change the baudrate!");
      System.out.println("Press any key to terminate...");
      scanner.nextLine();
//      return;
    }

    // Enable Dynamixel Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_ENABLE);
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }
    else
    {
      System.out.println("Dynamixel has been successfully connected");
    }

    while (true)
    {
      System.out.println("Press enter to continue! (or press e then enter to quit!)");
      if(scanner.nextLine().equals(KEY_FOR_ESCAPE))
        break;

      // Write goal position
      dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_GOAL_POSITION, dxl_goal_position[index]);
      
      dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION);
      System.err.println("dxl_comm_result: "+dxl_comm_result +" <- zero equals success.");
      if ((dxl_comm_result) != COMM_SUCCESS)
      {
          System.err.println("failure?");
        dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
      }
      else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
      {
          System.err.println("dxl_error? "+dxl_error);
        dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
      }

      DXL_ID = 6;
      Target_Torque = 50;
       dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_TORQUE, (byte) Target_Torque);
      DXL_ID = 6;
      Target_Torque = 300;
       dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_TORQUE, (byte) Target_Torque);
      
      do
      {
        // Read present position
        dxl_present_position = dynamixel.read2ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_PRESENT_POSITION);
        if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
        {
          dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
        }
        else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
        {
          dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
        }

        System.out.printf("[ID: %d] GoalPos:%d  PresPos:%d  Threas:%d\n", DXL_ID, dxl_goal_position[index], dxl_present_position, DXL_MOVING_STATUS_THRESHOLD);

      } while ((Math.abs(dxl_goal_position[index] - dxl_present_position) > DXL_MOVING_STATUS_THRESHOLD));

      // Change goal position
      if (index == 0)
      {
        index = 1;
      }
      else
      {
        index = 0;
      }
      break;
    }
    int dummy=1;
    while(true){
    DXL_ID = 6;
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_DISABLE);
    dxl_present_position = dynamixel.read2ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_PRESENT_POSITION);
    
    short dxl_present_position2 = (short) ((short)dxl_present_position * (short)4);
    
    DXL_ID = 6;
    dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_GOAL_POSITION, dxl_present_position2);
      
      dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION);
      System.err.println(dummy+": "+dxl_present_position+" (this section reads one servo and prints to the other)");
      //System.err.println("dxl_comm_result: "+dxl_comm_result +" <- zero equals success.");
      
      //if(scanner.nextLine().equals(KEY_FOR_ESCAPE))
      
      if(dummy==2800){
        break;
      }
      dummy++;
    }

    // Disable Dynamixel Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_DISABLE);
    System.err.println("Torque disabled");
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }

    // Close port
    dynamixel.closePort(port_num);

    //return;

  }
}
