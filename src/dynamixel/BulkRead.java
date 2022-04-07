/*
 * BulkRead.java
 *
 *  Created on: 2016. 6. 23.
 *      Author: Ryu Woon Jung (Leon)

https://docs.ros.org/en/kinetic/api/dynamixel_sdk/html/packet__handler_8h_source.html
 65 // Communication Result
   66 #define COMM_SUCCESS        0       // tx or rx packet communication success
   67 #define COMM_PORT_BUSY      -1000   // Port is busy (in use)
   68 #define COMM_TX_FAIL        -1001   // Failed transmit instruction packet
   69 #define COMM_RX_FAIL        -1002   // Failed get status packet
   70 #define COMM_TX_ERROR       -2000   // Incorrect instruction packet
   71 #define COMM_RX_WAITING     -3000   // Now recieving status packet
   72 #define COMM_RX_TIMEOUT     -3001   // There is no status packet
   73 #define COMM_RX_CORRUPT     -3002   // Incorrect status packet
   74 #define COMM_NOT_AVAILABLE  -9000   //


 */

//
// *********     Bulk Read Example      *********
//
//
// Available Dynamixel model on this example : MX or X series set to Protocol 1.0
// This example is designed for using two Dynamixel MX-28, and an USB2DYNAMIXEL.
// To use another Dynamixel model, such as X series, see their details in E-Manual(support.robotis.com) and edit below variables yourself.
// Be sure that Dynamixel MX properties are already set as %% ID : 1 / Baudnum : 1 (Baudrate : 1000000)
//
package dynamixel;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import static java.lang.Thread.sleep;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkRead
{
  public static void main(String[] args)
  {
    // Control table address
    short ADDR_MX_TORQUE_ENABLE         = 24;                  // Control table address is different in Dynamixel model
    short ADDR_MX_GOAL_POSITION         = 30;
    short ADDR_MX_PRESENT_POSITION      = 36;
    short ADDR_MX_MOVING                = 46;

    // Data Byte Length
    short LEN_MX_GOAL_POSITION          = 2;
    short LEN_MX_PRESENT_POSITION       = 2;
    short LEN_MX_MOVING                 = 1;

    // Protocol version
    int PROTOCOL_VERSION                = 1;                   // See which protocol version is used in the Dynamixel

    // Default setting
    byte DXL1_ID                        = 6;                   // Dynamixel ID: 1
    byte DXL2_ID                        = 2;                   // Dynamixel ID: 2
    int BAUDRATE                        = 1000000;
    String DEVICENAME                   = "COM3";// "/dev/ttyUSB0";      // Check which port is being used on your controller
                                                               // ex) "COM1"   Linux: "/dev/ttyUSB0"

    byte TORQUE_ENABLE                  = 1;                   // Value for enabling the torque
    byte TORQUE_DISABLE                 = 0;                   // Value for disabling the torque
    short DXL_MINIMUM_POSITION_VALUE    = 150;                 // Dynamixel will rotate between this value
    short DXL_MAXIMUM_POSITION_VALUE    = 310;                // and this value (note that the Dynamixel would not move when the position value is out of movable range. Check e-manual about the range of the Dynamixel you use.)
    int DXL_MOVING_STATUS_THRESHOLD     = 10;                  // Dynamixel moving status threshold

    String KEY_FOR_ESCAPE               = "e";                 // Key for escape

    int COMM_SUCCESS                    = 0;                   // Communication Success result value
    int COMM_TX_FAIL                    = -1001;               // Communication Tx Failed

    // Instead of getch
    Scanner scanner = new Scanner(System.in);

    // Initialize Dynamixel class for java
    Dynamixel dynamixel = new Dynamixel();

    // Initialize PortHandler Structs
    // Set the port path
    // Get methods and members of PortHandlerLinux or PortHandlerWindows
    int port_num = dynamixel.portHandler(DEVICENAME);
    port_num = 0;
    System.out.println("port_num " + port_num);

    // Initialize PacketHandler Structs
    dynamixel.packetHandler();

    // Initialize Groupsyncwrite instance
    // seems to be missing an address and a length????? Allan Note
    int group_numBulkRead = dynamixel.groupBulkRead(port_num, PROTOCOL_VERSION);
    // Initialize Groupsyncread Structs for Present Position - 
    // https://github.com/ROBOTIS-GIT/DynamixelSDK/blob/master/java/protocol2.0/sync_read_write/SyncReadWrite.java
    // https://docs.ros.org/en/kinetic/api/dynamixel_sdk/html/group__bulk__read_8cpp_source.html#l00206
    // the code is pulling from a protocol2.0 git repository unfortunately the same functionality is not available in protocol1.0
    // it seems like syncWrite works; but syncRead does not work - even the gitbhub protocal 1 demo code seems odd in how it it uses bulkRead and not syncRead
    // http://support.robotis.com/en/product/actuator/dynamixel/communication/dxl_instruction.htm
    // per the link above this line; bulkread doesn't work on the ax series - it only works on the mx series.
    //int group_numSyncRead = dynamixel.groupSyncRead(port_num, 2, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
    // Initialize Groupsyncwrite instance
    int group_num_syncWrite = dynamixel.groupSyncWrite(port_num, PROTOCOL_VERSION, ADDR_MX_GOAL_POSITION, LEN_MX_GOAL_POSITION);

    System.out.println("group_numBulkRead: " + group_numBulkRead);

    int index = 0;
    int dxl_comm_result = COMM_TX_FAIL;                         // Communication result
    Boolean dxl_addparam_result = false;                        // AddParam result
    Boolean dxl_getdata_result = false;                         // GetParam result
    short[] dxl_goal_position = new short[]{DXL_MINIMUM_POSITION_VALUE, DXL_MAXIMUM_POSITION_VALUE};         // Goal position

    byte dxl_error = 0;                                         // Dynamixel error
    short position = 0;                            // Present position
    int dxl1_present_position = 0;                            // Present position
    int dxl1_present_positionSync = 0;                            // Present position
    byte dxl2_moving = 0;                                       // Dynamixel moving status

    Native.setProtected(true);
    // Open port
    if (dynamixel.openPort(port_num))
    {
      System.out.println("Succeeded to open the port!");
    }
    else
    {
      System.out.println("Failed to open the port!");
      System.out.println("Press any key to terminate...");
      scanner.nextLine();
      return;
    }

    // Set port baudrate
    if (dynamixel.setBaudRate(port_num, BAUDRATE))
    {
      System.out.println("Succeeded to change the baudrate!");
    }
    else
    {
      System.out.println("Failed to change the baudrate!");
      System.out.println("Press any key to terminate...");
      scanner.nextLine();
      return;
    }

    // Enable Dynamixel#1 Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL1_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_ENABLE);
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
      System.err.println(" <<motorNumber.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }
    else
    {
      System.out.printf("Dynamixel#%d has been successfully connected\n", DXL1_ID);
    }

    // Enable Dynamixel#2 Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL2_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_ENABLE);
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
      System.err.println(" <<motorNumber.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }
    else
    {
      System.out.printf("Dynamixel#%d has been successfully connected\n", DXL2_ID);
    }
    
    // torque is how hard it pushes; speed is how fast speed 35 is ~3 RPM
    dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL1_ID, (short)32, (short)50); // moving speed set to 20
    dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL2_ID, (short)32, (short)50); // moving speed set to 20        

//    // Add parameter storage for Dynamixel#1 present position value
//    dxl_addparam_result = dynamixel.groupSyncReadAddParam(group_numSyncRead, DXL1_ID);
//    if (dxl_addparam_result != true)
//    {
//      System.out.printf("[ID: %d] groupSyncRead addparam failed\n", DXL1_ID);
//      return;
//    }
//
//    // Add parameter storage for Dynamixel#2 present position value
//    dxl_addparam_result = dynamixel.groupSyncReadAddParam(group_numSyncRead, DXL2_ID);
//    if (dxl_addparam_result != true)
//    {
//      System.out.printf("[ID: %d] groupSyncRead addparam failed\n", DXL2_ID);
//      return;
//    }    
    
    // Add parameter storage for Dynamixel#1 present position value
    dxl_addparam_result = dynamixel.groupBulkReadAddParam(group_numBulkRead, DXL1_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
    if (dxl_addparam_result != true)
    {
      System.out.printf("[ID: %d] groupBulkRead addparam failed\n", DXL1_ID);
      return;
    }

    // Add parameter storage for Dynamixel#2 present moving value
    dxl_addparam_result = dynamixel.groupBulkReadAddParam(group_numBulkRead, DXL2_ID, ADDR_MX_MOVING, LEN_MX_MOVING);
    if (dxl_addparam_result != true)
    {
      System.out.printf("[ID: %d] groupBulkRead addparam failed\n", DXL2_ID);
      return;
    }
    
    while (true)
    {
      System.out.println("Press enter to continue! (or press e then enter to quit!)");
      if(scanner.nextLine().equals(KEY_FOR_ESCAPE))
        break;

      // Add Dynamixel#1 goal position value to the Syncwrite storage
      dxl_addparam_result = dynamixel.groupSyncWriteAddParam(group_num_syncWrite, DXL1_ID, dxl_goal_position[index], LEN_MX_GOAL_POSITION);
      if (dxl_addparam_result != true)
      {
        System.out.printf("[ID: %d] groupSyncWrite addparam failed\n", DXL1_ID);
        return;
      }

      // Add Dynamixel#2 goal position value to the Syncwrite parameter storage
      dxl_addparam_result = dynamixel.groupSyncWriteAddParam(group_num_syncWrite, DXL2_ID, dxl_goal_position[index], LEN_MX_GOAL_POSITION);
      if (dxl_addparam_result != true)
      {
        System.out.printf("[ID: %d] groupSyncWrite addparam failed\n", DXL2_ID);
        return;
      }

      // Syncwrite goal position
      dynamixel.groupSyncWriteTxPacket(group_num_syncWrite);
      if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
        System.out.println("syncwrite " + dynamixel.getTxRxResult(PROTOCOL_VERSION, dxl_comm_result));

      // Clear syncwrite parameter storage
      dynamixel.groupSyncWriteClearParam(group_num_syncWrite);
      
//      // Write Dynamixel#1 goal position
//      dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL1_ID, ADDR_MX_GOAL_POSITION, dxl_goal_position[index]);
//      if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
//      {
//        //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
//        System.err.println(" <<motorNumber173.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
//      }
//      else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
//      {
//        dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
//      }
//
//      // Write Dynamixel#2 goal position
//      dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL2_ID, ADDR_MX_GOAL_POSITION, dxl_goal_position[index]);
//      if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
//      {
//        //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
//        System.err.println(" <<motorNumber185.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
//      }
//      else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
//      {
//        dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
//      }

        try {
            sleep(50);
        } catch (InterruptedException ex) {
            Logger.getLogger(BulkRead.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
      try{
      do
      {
          try {
            sleep(50);
        } catch (InterruptedException ex) {
            Logger.getLogger(BulkRead.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Bulkread present position and moving status
        dynamixel.groupBulkReadRxPacket(group_numBulkRead);
//        // Syncread present position
//        dynamixel.groupSyncReadTxRxPacket(group_numSyncRead);
        if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
            {System.err.println("row 285 "+dynamixel.getTxRxResult(PROTOCOL_VERSION, dxl_comm_result));}
//
//        // Check if groupsyncread data of Dynamixel#1 is available
//        dxl_getdata_result = dynamixel.groupSyncReadIsAvailable(group_numSyncRead, DXL1_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
//        if (dxl_getdata_result != true)
//        {
//          System.out.printf("[ID: %d] groupSyncRead getdata failed\n", DXL1_ID);
//          return;
//        }
//        
//        // Check if groupsyncread data of Dynamixel#2 is available
//        dxl_getdata_result = dynamixel.groupSyncReadIsAvailable(group_numSyncRead, DXL2_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
//        if (dxl_getdata_result != true)
//        {
//          System.out.printf("[ID: %d] groupSyncRead getdata failed\n", DXL2_ID);
//          return;
//        }
        
        //position = dynamixel.read2ByteTxRx(port_num, PROTOCOL_VERSION, DXL1_ID, ADDR_MX_PRESENT_POSITION);

//        if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
//          //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
//        {
//          System.err.println(" <<motorNumber198.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
//        }

        dxl_getdata_result = dynamixel.groupBulkReadIsAvailable(group_numBulkRead, DXL1_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
        if (dxl_getdata_result != true)
        {
          System.out.printf("[ID: %d] groupBulkRead getdata failed\n", DXL1_ID);
          return;
        }

        dxl_getdata_result = dynamixel.groupBulkReadIsAvailable(group_numBulkRead, DXL2_ID, ADDR_MX_MOVING, LEN_MX_MOVING);
        if (dxl_getdata_result != true)
        {
          System.out.printf("[ID: %d] groupBulkRead getdata failed\n", DXL2_ID);
          return;
        }

        // Get Dynamixel#1 present position value
        dxl1_present_position = dynamixel.groupBulkReadGetData(group_numBulkRead, DXL1_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);
        //dxl1_present_positionSync = dynamixel.groupSyncReadGetData(group_numSyncRead, DXL1_ID, ADDR_MX_PRESENT_POSITION, LEN_MX_PRESENT_POSITION);

        //System.out.println("dxl1_present_positionSync " + dxl1_present_positionSync);
        // Get Dynamixel#2 moving status value
        dxl2_moving = (byte)dynamixel.groupBulkReadGetData(group_numBulkRead, DXL2_ID, ADDR_MX_MOVING, LEN_MX_MOVING);

        System.out.printf("[ID: %d] Present Position : %d [ID: %d] Is Moving : %d\n", DXL1_ID, dxl1_present_position, DXL2_ID, dxl2_moving);

      } while (Math.abs(dxl_goal_position[index] - dxl1_present_position) > DXL_MOVING_STATUS_THRESHOLD);
    
            }
        catch (LastErrorException err) {
            System.err.println("Allan_Errror in packetHandler "+err);
        }

      // Change goal position
      if (index == 0)
      {
        index = 1;
      }
      else
      {
        index = 0;
      }
    }

    // Disable Dynamixel#1 Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL1_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_DISABLE);
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
      System.err.println(" <<motorNumber240.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }

    // Disable Dynamixel#2 Torque
    dynamixel.write1ByteTxRx(port_num, PROTOCOL_VERSION, DXL2_ID, ADDR_MX_TORQUE_ENABLE, TORQUE_DISABLE);
    if ((dxl_comm_result = dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION)) != COMM_SUCCESS)
    {
      //dynamixel.printTxRxResult(PROTOCOL_VERSION, dxl_comm_result);
      System.err.println(" <<motorNumber.  Position Not read correctly "+dxl_comm_result+" "+dynamixel.getLastTxRxResult(port_num, PROTOCOL_VERSION));
    }
    else if ((dxl_error = dynamixel.getLastRxPacketError(port_num, PROTOCOL_VERSION)) != 0)
    {
      dynamixel.printRxPacketError(PROTOCOL_VERSION, dxl_error);
    }

    // Close port
    dynamixel.closePort(port_num);

    return;
  }
}