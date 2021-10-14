package dynamixel;

public class createDynamixel {
    private Dynamixel[] dynamixel = null;
    private short goalPosMotor1 = 1;
    
    public Dynamixel createDynamixel(int dynamixelNumber){
        dynamixel[dynamixelNumber] = new Dynamixel();
        return dynamixel[dynamixelNumber];
        }
    
    public void moveMotor1Slider(short newPosition) {
        goalPosMotor1 = (short)newPosition;
        //dynamixel.write2ByteTxRx(port_num, PROTOCOL_VERSION, DXL_ID, ADDR_MX_GOAL_POSITION, goalPosMotor1);
    }
        
}
