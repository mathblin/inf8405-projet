#include "gyroscope.h"
#include "common.h"

State currentState = STOP;

int vitesseMin = 80;
int vitesseMax = 255;
int vitesse = vitesseMin;
int vitesseIncrement = 17;
int vitesseIncrementRotation = 15;
int vitesseRotationMin = 80;
int vitesseLow = vitesseRotationMin;

void mode_gyroscope(String* msg, bool* debug) {
  char command = getValue(*msg, ',', LETTER_POSITION)[0];

  if (*debug && Serial.availableForWrite() > 30) Serial.print(" command: " + String(command) + "!!!");
  else if (Serial.availableForWrite() > 4) { // Tells the server that Arduino is ready to receive a command
    Serial.print("ok!!!");
  }  

  vitesse = vitesseMax;

  if (command == -1) {
    stop();
    return;
  }

  switch (command) {
    case 'W':  //Move Forward
      vitesse = (currentState == FORWARD) ? 
                (vitesse > vitesseMax) ? vitesseMax : vitesse + vitesseIncrement
                : vitesseMin;
      break;
    case 'w':  // reduce front speed
      vitesse = (currentState == FORWARD && vitesse >= vitesseMin) ? 
                vitesse - vitesseIncrement
                : vitesseMin;
      break;

    case 'S':  //Move Backward
      vitesse = (currentState == BACKWARD) ? 
                (vitesse > vitesseMax) ? vitesseMax : vitesse + vitesseIncrement
                : vitesseMin;
      break;
    case 's':  //Move Backward
      vitesse = (currentState == BACKWARD && vitesse >= vitesseMin) ? 
                vitesse - vitesseIncrement
                : vitesseMin;
      break;

    case 'a':  //Turn Left 360
      vitesse = (currentState == ROT_LEFT_360) ? 
                (vitesse > vitesseMax) ? vitesseMax : vitesse + vitesseIncrement
                : vitesseRotationMin;
      break;
    case 'd':  //Turn Right
      vitesse = (currentState == ROT_RIGHT_360) ? 
                (vitesse > vitesseMax) ? vitesseMax : vitesse + vitesseIncrement
                : vitesseRotationMin;
      break;

    case 'q':  //Turn left
      if (currentState == ROT_LEFT_FRONT) {
        vitesseLow = vitesseLow + vitesseIncrementRotation;

        if (vitesseLow > vitesse) {
          vitesseLow = vitesse;
        }
      } else {
        vitesse = vitesseRotationMin - vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;
    case 'Q':  //Turn left
      if (currentState == ROT_LEFT_FRONT) {
        vitesse = vitesse + vitesseIncrementRotation;
        vitesseLow = vitesseLow - vitesseIncrementRotation;
        if (vitesse > vitesseMax) {
          vitesse = vitesseMax;
        }
        if (vitesseLow < vitesseMin) {
          vitesseLow = vitesseRotationMin;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;

    case 'e':  //Turn right
      if (currentState == ROT_RIGHT_FRONT) {
        vitesseLow = vitesseLow + vitesseIncrementRotation;

        if (vitesseLow > vitesse) {
          vitesseLow = vitesse;
        }
      } else {
        vitesse = vitesseRotationMin - vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;
    case 'E':  //Turn right
      if (currentState == ROT_RIGHT_FRONT) {
        vitesse = vitesse + vitesseIncrementRotation;
        vitesseLow = vitesseLow - vitesseIncrementRotation;
        if (vitesse > vitesseMax) {
          vitesse = vitesseMax;
        }
        if (vitesseLow < vitesseMin) {
          vitesseLow = vitesseRotationMin;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;

    case 'z':  //Turn left
      if (currentState == ROT_LEFT_BACK) {
        vitesseLow = vitesseLow + vitesseIncrementRotation;

        if (vitesseLow > vitesse) {
          vitesseLow = vitesse;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;
    case 'Z':  //Turn left
      if (currentState == ROT_LEFT_BACK) {
        vitesse = vitesse + vitesseIncrementRotation;
        vitesseLow = vitesseLow - vitesseIncrementRotation;
        if (vitesse > vitesseMax) {
          vitesse = vitesseMax;
        }
        if (vitesseLow < vitesseMin) {
          vitesseLow = vitesseRotationMin;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;

    case 'c':  //Turn right
      if (currentState == ROT_RIGHT_BACK) {
        vitesseLow = vitesseLow + vitesseIncrementRotation;

        if (vitesseLow > vitesse) {
          vitesseLow = vitesse;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;
    case 'C':  //Turn right
      if (currentState == ROT_RIGHT_BACK) {
        vitesse = vitesse + vitesseIncrementRotation;
        vitesseLow = vitesseLow - vitesseIncrementRotation;
        if (vitesse > vitesseMax) {
          vitesse = vitesseMax;
        }
        if (vitesseLow < vitesseMin) {
          vitesseLow = vitesseRotationMin;
        }
      } else {
        vitesse = vitesseRotationMin + vitesseIncrementRotation;
        vitesseLow = vitesseRotationMin;
      }
      break;
  }

  // Motion and states
  switch (command) {
    case 'a':
      turn_L_360(vitesse, vitesse);
      currentState = ROT_LEFT_360;
      break;
    case 'd':
      turn_R_360(vitesse, vitesse);
      currentState = ROT_RIGHT_360;
      break;
    case 'w':
    case 'W':
      advance(vitesse, vitesse);  //move forward in max speed
      currentState = FORWARD;
      break;
    case 's':
    case 'S':
      back_off(vitesse, vitesse);  //move back in max speed
      currentState = BACKWARD;
      break;
    case 'q':
    case 'Q':
      advance(vitesse, vitesseLow);
      currentState = ROT_LEFT_FRONT;
      break;
    case 'e':
    case 'E':
      advance(vitesseLow, vitesse);
      currentState = ROT_RIGHT_FRONT;
      break;
    case 'z':
    case 'Z':
      back_off(vitesse, vitesseLow);
      currentState = ROT_LEFT_BACK;
      break;
    case 'c':
    case 'C':
      back_off(vitesseLow, vitesse);
      currentState = ROT_RIGHT_BACK;
      break;
    case 'x':
    default:
      stop();
      currentState = STOP;
      break;
  }
}