#ifndef JOYSTICK_H
#define JOYSTICK_H
#include <Arduino.h>

int getCadrans(int angle);
void mode_joystick(String* msg, bool* debug);

#endif // JOYSTICK_H