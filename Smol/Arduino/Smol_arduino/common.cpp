#include "common.h"

// getValue function taken from this link:
// https://stackoverflow.com/questions/9072320/split-string-into-string-array
String getValue(String data, char separator, int index) {
  int found = 0;
  int strIndex[] = { 0, -1 };
  int maxIndex = data.length() - 1;

  for (int i = 0; i <= maxIndex && found <= index; i++) {
    if (data.charAt(i) == separator || i == maxIndex) {
      found++;
      strIndex[0] = strIndex[1] + 1;
      strIndex[1] = (i == maxIndex) ? i + 1 : i;
    }
  }
  return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}

//Standard PWM DC control
int E1 = 5;  //M1 Speed Control
int E2 = 6;  //M2 Speed Control
int M1 = 4;  //M1 Direction Control
int M2 = 7;  //M1 Direction Control

void stop(void)  //Stop
{
  digitalWrite(E1, LOW);
  digitalWrite(E2, LOW);
}
void advance(char a, char b)  //Move forward
{
  analogWrite(E1, a);  //PWM Speed Control
  digitalWrite(M1, HIGH);
  analogWrite(E2, b);
  digitalWrite(M2, HIGH);
}
void back_off(char a, char b)  //Move backward
{
  analogWrite(E1, a);
  digitalWrite(M1, LOW);
  analogWrite(E2, b);
  digitalWrite(M2, LOW);
}
void turn_L_360(char a, char b)  //Turn Left
{
  analogWrite(E1, a);
  digitalWrite(M1, LOW);
  analogWrite(E2, b);
  digitalWrite(M2, HIGH);
}
void turn_R_360(char a, char b)  //Turn Right
{
  analogWrite(E1, a);
  digitalWrite(M1, HIGH);
  analogWrite(E2, b);
  digitalWrite(M2, LOW);
}

// Servo
bool stop_servo = STOP_SERVO;
void manage_stop_servo(String* msg, bool* debug) {
  int mode = getValue(*msg, ',', MODE_POSITION).toInt();

  int angle = -1;
  char command = ' ';
  switch (mode) {
    case MODE_JOYSTICK:
      angle = getValue(*msg, ',', ANGLE_POSITION).toInt();
      if (angle == 0) stop_servo = STOP_SERVO;
      else stop_servo = MOVE_SERVO;
      break;
    case MODE_GYROSCOPE:
      command = getValue(*msg, ',', LETTER_POSITION)[0];
      if (command == 'x') stop_servo = STOP_SERVO;
      else stop_servo = MOVE_SERVO;
      break;
  }
}

bool isStopped() {
  return stop_servo;
}