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
  void stop();                  // stop at "to" angle
  void activate(String* msg, bool* debug);
  bool isActive();
private:
  Servo my_servo; 
  unsigned long move_start_time;
  long from;  // angle from which to start
  long to;    // angle where to end the movement
  bool actived;
  int cnt;    // counter
};

#endif	// SERVOMOTOR_H