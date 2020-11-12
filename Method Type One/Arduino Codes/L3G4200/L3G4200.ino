#include <Wire.h>

#define CTRL_REG1 0x20
#define CTRL_REG2 0x21
#define CTRL_REG3 0x22
#define CTRL_REG4 0x23
#define CTRL_REG5 0x24

int L3G4200D_Address = 105; //I2C address of the L3G4200D

int cal_count;
int flag = 1;
double z;
double z_cal;
double z_ang;
double last_time, current_time;

void setup(){ 
     Wire.begin();
     Serial.begin(115200);
     Serial.println("Starting up L3G4200D");
     setupL3G4200D(2000); // Configure L3G4200 - 250, 500 or 2000 dps
     delay(250);
     calibration();
}

void loop(){
     // get last_time and current_time
     if(flag == 1){
        last_time = millis();
        flag += 1;
     }
     current_time = millis();
     
     // get raw values
     getGyroValues();

     // times raw value to sensitivity
     z = z*0.07;

     // integrate velocity to get angle
     double dt = current_time-last_time;
     z_ang += z*dt/1000; // get angle
     last_time = current_time;

     // scale angle to 0~360
     if(z_ang < 0){
        z_ang = z_ang+360; 
     }
     if(z_ang > 360){
        z_ang = z_ang-360; 
     }

     // print out values
     Serial.print(" Z_angle:");
     Serial.println(z_ang,0);
}

void getGyroValues(){
     // get z value
     byte zMSB = readRegister(L3G4200D_Address, 0x2D);
     byte zLSB = readRegister(L3G4200D_Address, 0x2C);
     z = ((zMSB << 8) | zLSB);
     if(cal_count == 2000)z -= z_cal;
}

int setupL3G4200D(int scale){
    // enable x, y, z and turn off power down in CTRL_REG1
    writeRegister(L3G4200D_Address, CTRL_REG1, 0b11001111);

    // adjust/use the HPF in CTRL_REG2 
    writeRegister(L3G4200D_Address, CTRL_REG2, 0b00000000);

    // configure CTRL_REG3 to generate data ready interrupt on INT2, no interrupts used on INT1
    writeRegister(L3G4200D_Address, CTRL_REG3, 0b00001000);

    // control the full-scale range in CTRL_REG4
    if(scale == 250){
      writeRegister(L3G4200D_Address, CTRL_REG4, 0b00000000);
    }else if(scale == 500){
      writeRegister(L3G4200D_Address, CTRL_REG4, 0b00010000);
    }else{
      writeRegister(L3G4200D_Address, CTRL_REG4, 0b00110000);
    }

    // control high-pass filtering of outputs in CTRL_REG5
    writeRegister(L3G4200D_Address, CTRL_REG5, 0b00000000);
}

void writeRegister(int deviceAddress, byte address, byte val) {
     Wire.beginTransmission(deviceAddress);     // start transmission to gyro 
     Wire.write(address);                       // address of register to be written
     Wire.write(val);                           // value to be written to the address
     Wire.endTransmission();                    // end transmission
}

int readRegister(int deviceAddress, byte address){
    int v;                                      // variable for storing value
    Wire.beginTransmission(deviceAddress);      // start transmission to gyro
    Wire.write(address);                        // register to be read
    Wire.endTransmission();                     // end transmission
    Wire.requestFrom(deviceAddress, 1);         // request a byte
    while(!Wire.available());                   // wait for byte
    v = Wire.read();                            // read a byte
    return v;                                   // return value
}

void calibration(){
     Serial.print("Starting calibration...");                 // print message
     for (cal_count = 0; cal_count < 2000 ; cal_count ++){    // take 2000 readings for calibration
          getGyroValues();                                    // read the gyro output
          z_cal += z;                                         // add z value to z_cal
          if(cal_count%100 == 0)Serial.print(".");            // print a dot every 100 readings
     }
     //divide by 2000 to get the average gyro offset
     Serial.println(" done!");                                // print 2000 measures are done!
     z_cal /= 2000;                                           // divide the z total by 2000
}
