#include "common.h"
#include "gyroscope.h"
#include "joystick.h"

void setup(void) 
{ 
  int i;
  for(i=4;i<=7;i++)
    pinMode(i, OUTPUT);  
  Serial.begin(9600);      //Set Baud Rate
  // Serial.println("Run keyboard control");
} 
void loop(void) 
{
  bool debug = false;
  String msg = "";
  int mode = 1000;

  while(!Serial.available());

  if(Serial.available()){
    msg = Serial.readString();

    mode = getValue(msg, ',', MODE_POSITION).toInt();
    debug = getValue(msg, ',', DEBUG_POSITION).toInt();

    switch(mode)
    {
    case MODE_JOYSTICK:
      mode_joystick(&msg, &debug);
    break;

    case MODE_GYROSCOPE:
      mode_gyroscope(&msg, &debug);
    break;
    } 

  }

}
