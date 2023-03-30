#include "servomotor.h"

Servomotor::Servomotor() {}

void Servomotor::init(int pin, long from_in, long to_in) {
  my_servo.attach(pin);
  move_start_time = millis();
  from = from_in;
  to = to_in;
}

void Servomotor::start(long moving_time) {
  unsigned long progress = millis() - move_start_time;

  if (progress <= moving_time) {
    long angle = map(progress, 0, moving_time, from, to);
    my_servo.write(angle);
  } 
  else {
    move_start_time = millis();
    long temp = from;
    from = to;
    to = temp;
  }
}

void Servomotor::stop(long angle) {
  my_servo.write(angle);
}

// https://arduinogetstarted.com/faq/how-to-control-speed-of-servo-motor#:~:text=By%20using%20map()%20and,90%C2%B0%20in%203%20seconds.
