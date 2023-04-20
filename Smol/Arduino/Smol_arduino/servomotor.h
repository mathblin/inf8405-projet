#ifndef SERVOMOTOR_H
#define SERVOMOTOR_H
#include <Arduino.h>
#include <Servo.h>

class Servomotor
{
public:
  Servomotor();
  void init(int pin, long from, long to);
  void start(long moving_time); // time in millis to do one rotation
  void stop(long angle);        // angle where to stop
  void you_can_change_this_function_name_lau(String* msg, bool* debug);
private:
  Servo my_servo; 
  unsigned long move_start_time;
  long from;  // angle from which to start
  long to;    // angle where to end the movement
};

#endif	// SERVOMOTOR_H