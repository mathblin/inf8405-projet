#ifndef COMMON_H
#define COMMON_H
#include <Arduino.h>

String getValue(String data, char separator, int index);

enum Flags {
  DEBUG_POSITION = 0,
  MODE_POSITION = 1,
  LETTER_POSITION = 2,
  ANGLE_POSITION = 2,
  STRENGTH_POSITION = 3,
};

enum CONTROL_MODE {
  MODE_JOYSTICK = 0,
  MODE_GYROSCOPE = 1
};

void stop(void);                  //Stop
void advance(char a, char b);     //Move forward
void back_off(char a, char b);    //Move backward
void turn_L_360(char a, char b);  //Turn Left
void turn_R_360(char a, char b);  //Turn Right

// Servo
enum servo_state {STOP_SERVO = 0, MOVE_SERVO = 1};
void manage_stop_servo(String* msg, bool* debug);
bool get_stop_servo();

#endif  // COMMON_H
