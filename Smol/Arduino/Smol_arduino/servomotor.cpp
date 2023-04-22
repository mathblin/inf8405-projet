#include "servomotor.h"
#include "common.h"

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

      // for now this is only valid for servo of hat
      cnt++;
      if (actived && cnt == 3) { 
        actived = false;
        long max = max(from, to);
        from = min(from, to);
        to = max;
        cnt = 0;
      }
    }
}

void Servomotor::stop() {
    my_servo.write(to);
}

void Servomotor::activate(String* msg, bool* debug) {
  int mode = getValue(*msg, ',', MODE_POSITION).toInt();

  if (mode == MODE_SECOND_SERVO) {
    actived = true;
  }

  if (*debug && Serial.availableForWrite() > 30) {
    Serial.print(" mode : " + String(mode) + " msg : " + *msg);
  } else if (Serial.availableForWrite() > 30) {  // Tells the server that Arduino is ready to receive a command
    Serial.print("ok");
  }
}

bool Servomotor::isActive() {
  return actived;
}

// https://arduinogetstarted.com/faq/how-to-control-speed-of-servo-motor#:~:text=By%20using%20map()%20and,90%C2%B0%20in%203%20seconds.
