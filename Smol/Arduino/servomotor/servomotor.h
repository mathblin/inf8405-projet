#ifndef servomotor_h
#define servomotor_h

#include <Servo.h>

class Servomotor
{
public:
  Servomotor();
  void init(int pin, long from, long to);
  void start(long moving_time); // time in millis to do one rotation
  void stop(long angle);        // angle where to stop
private:
  Servo my_servo; 
  unsigned long move_start_time;
  long from;  // angle from which to start
  long to;    // angle where to end the movement
};

#endif