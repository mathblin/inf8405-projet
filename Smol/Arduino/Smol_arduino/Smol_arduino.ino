#include "common.h"
#include "gyroscope.h"
#include "joystick.h"
#include "servomotor.h"

Servomotor cane;

void setup(void) {
  cane.init(8, 45, 90);
  int i;
  for (i = 4; i <= 7; i++)
    pinMode(i, OUTPUT);
  Serial.begin(9600);  //Set Baud Rate
  // Serial.println("Run keyboard control");
}

void loop(void) {
  bool debug = false;
  String msg = "";
  int mode = 1000;
  bool started = false;

  //while (!Serial.available())
  //  ; // TODO: is this really necessary?

  if (started) {
    cane.start(1000);
  } else {
    cane.stop(45);
  }

  if (Serial.available()) {
    msg = Serial.readString();

    mode = getValue(msg, ',', MODE_POSITION).toInt();
    debug = getValue(msg, ',', DEBUG_POSITION).toInt();

    switch (mode) {
      case MODE_JOYSTICK:
        //int strength = getValue(msg, ',', STRENGTH_POSITION).toInt();
        //if (strength == 0) {
        if (msg == "1,x") {
            started = false;
        } else {
            started = true;
        }
        mode_joystick(&msg, &debug);
        break;

      case MODE_GYROSCOPE:
        //char command = getValue(msg, ',', LETTER_POSITION)[0];
        //if (command == -1 || command == 'x') {
        if (msg == "1,x") {
            started = false;
        } else {
            started = true;
        }
        cane.start(100);
        mode_gyroscope(&msg, &debug);
        break;
    }
  }
}
