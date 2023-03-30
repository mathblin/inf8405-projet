#ifndef GYSROSCOPE_H
#define GYSROSCOPE_H
#include <Arduino.h>

enum State {
  STOP,
  FORWARD,
  BACKWARD,
  ROT_LEFT_FRONT,
  ROT_LEFT_BACK,
  ROT_LEFT_360,
  ROT_RIGHT_FRONT,
  ROT_RIGHT_BACK,
  ROT_RIGHT_360,
  STATE_COUNT
};

void mode_gyroscope(String* msg, bool* debug);

#endif  // GYSROSCOPE_H