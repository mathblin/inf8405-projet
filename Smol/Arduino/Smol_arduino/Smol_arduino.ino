#include "common.h"
#include "gyroscope.h"
#include "joystick.h"
#include "servomotor.h"
#include <protothreads.h>  // https://www.digikey.com/en/maker/blogs/2022/how-to-write-multi-threaded-arduino-programs

Servomotor cane;
pt ptServo;
pt ptMove;

int servo(struct pt* pt) {
  PT_BEGIN(pt);

  while (true) {
    if (get_stop_servo()) {
      cane.stop(45);
    } else {
      cane.start(1000);
    }
    PT_SLEEP(pt, 1);
  }

  PT_END(pt);
}

int move(struct pt* pt) {
  PT_BEGIN(pt);

  while (true) {
    bool debug = false;
    String msg = "";
    int mode = 1000;

    if (!Serial.available()) {
      return;
    }
    
    msg = Serial.readString();
    mode = getValue(msg, ',', MODE_POSITION).toInt();
    debug = getValue(msg, ',', DEBUG_POSITION).toInt();

    manage_stop_servo(&msg, &debug);

    switch (mode) {
      case MODE_JOYSTICK:
        mode_joystick(&msg, &debug);
        break;
      case MODE_GYROSCOPE:
        mode_gyroscope(&msg, &debug);
        break;
    }
  }

  PT_END(pt);
}

void setup(void) {
  PT_INIT(&ptServo);
  PT_INIT(&ptMove);

  cane.init(8, 45, 90);
  int i;
  for (i = 4; i <= 7; i++)
    pinMode(i, OUTPUT);
  Serial.begin(9600);  //Set Baud Rate
}

void loop(void) {
  PT_SCHEDULE(servo(&ptServo));
  PT_SCHEDULE(move(&ptMove));
}
