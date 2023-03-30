#ifndef JOYSTICK_H
#define JOYSTICK_H
#include <Arduino.h>

int getCadrans(int angle);
void advance_joystick(String* msg, float* angle_radi, float* sin_res, float* cos_res, float* strength, int* cad);
void mode_joystick(String* msg, bool* debug);

#endif  // JOYSTICK_H