#include <Wire.h>
#include <SoftwareSerial.h>
#include "Adafruit_TCS34725softi2c.h"
   
// use digital pin for emulating SDA / SCL
#define SDApin 8
#define SCLpin 9
#define SDApin1 7
#define SCLpin1 6

// initialise with specific int time and gain values
Adafruit_TCS34725softi2c tcs = Adafruit_TCS34725softi2c(TCS34725_INTEGRATIONTIME_24MS, TCS34725_GAIN_16X, SDApin, SCLpin);
Adafruit_TCS34725softi2c tcs1 = Adafruit_TCS34725softi2c(TCS34725_INTEGRATIONTIME_24MS, TCS34725_GAIN_16X, SDApin1, SCLpin1);

// create a software seiral
const int RX = 2;
const int TX = 3;
SoftwareSerial serial(RX, TX);

// some constant
//int r1Min = 0;
//int r1Max = 28;
//int b1Min = 0;
//int b1Max = 39;
//int r2Min = 0;
//int r2Max = 32;
//int b2Min = 0;
//int b2Max = 43;

// setup
void setup(void) {
  Serial.begin(9600);
  serial.begin(9600);
  tcs.begin();
  tcs1.begin();
}

// get readings
void loop(void) {
  // declare variables for the colors
  uint16_t r1, g1, b1, c1;
  uint16_t r2, g2, b2, c2;
//  int rc1, bc1, rc2, bc2;
  
  // read the sensor
  tcs.getRawData(&r1, &g1, &b1, &c1);                         
  tcs1.getRawData(&r2, &g2, &b2, &c2);

  // calibrate sensor
//  rc1 = map(r1, r1Min, r1Max, 0, 255);
//  bc1 = map(b1, b1Min, b1Max, 0, 255);
//  rc2 = map(r2, r2Min, r2Max, 0, 255);
//  bc2 = map(b2, b2Min, b2Max, 0, 255);

  // print readings
//  Serial.print(rc1); Serial.print(","); Serial.print(bc1); Serial.print(","); Serial.print(rc2); Serial.print(","); Serial.print(bc2); Serial.print(","); Serial.println();
//  serial.print(rc1); serial.print(","); serial.print(bc1); serial.print(","); serial.print(rc2); serial.print(","); serial.print(bc2); serial.print(","); serial.println();
  Serial.print(r1); Serial.print(","); Serial.print(b1); Serial.print(","); Serial.print(r2); Serial.print(","); Serial.print(b2); Serial.print(","); Serial.println();
  serial.print(r1); serial.print(","); serial.print(b1); serial.print(","); serial.print(r2); serial.print(","); serial.print(b2); serial.print(","); serial.println();
}
