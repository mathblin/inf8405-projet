#include "servomotor.h"

Servomotor cane;
Servomotor hat;

void setup() {
  cane.init(8, 0, 45);
  hat.init(9, 0, 90);
}

void loop() {
  cane.start(3000);
  hat.start(4000);
}