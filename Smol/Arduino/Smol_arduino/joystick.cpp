#include "joystick.h"
#include "common.h"

const int VITESSE_MIN_JOYSTICK = 80;
const int VITESSE_MAX_JOYSTICK = 255;
const int VITESSE_DIFF = VITESSE_MAX_JOYSTICK - VITESSE_MIN_JOYSTICK;
const float STRENGTH_SCALER = 100;

int left_strength;
int right_strength;

enum {
  FIRST,
  SECOND,
  THIRD,
  FOURTH,
  FRONT,
  RIGHT,
  LEFT,
  BACK,
  ERROR = 100
};

int getCadrans(int angle) {
  // TODO: ajuster pour le tourner 360 degrees
  if (angle < 0 || angle > 360) return ERROR;
  if (angle == 90) return FRONT;                  // 4
  if (angle == 270) return BACK;                  // 7
  if (angle <= 15 || angle >= 345) return RIGHT;  // 5
  if (angle < 90 && angle > 15) return FIRST;     // 0
  if (angle > 90 && angle < 165) return SECOND;   // 1
  if (angle >= 165 && angle <= 195) return LEFT;  // 6
  if (angle > 195 && angle < 270) return THIRD;   // 2
  if (angle > 270 && angle < 345) return FOURTH;  // 3
  return ERROR;
}

// TODO : retirer les params qui ne sont utilisés que pour le debug... ça va permettre également d'éviter les variables globales!
void advance_joystick(String* msg, float* angle_radi, float* sin_res, float* cos_res, float* strength, int* cad) {
  *strength = getValue(*msg, ',', STRENGTH_POSITION).toInt();
  
  if (*strength == 0) {
    stop();
    return;
  }

  // Calcul d'angles: https://arduinogetstarted.com/reference/arduino-sin ... adapter pour cos
  String angle = getValue(*msg, ',', ANGLE_POSITION);
  *angle_radi = angle.toInt() * M_PI / 180;
  *sin_res = sin(*angle_radi);
  //*cos_res = cos(*angle_radi); // À enlever?
  *cad = getCadrans(angle.toInt());

  float speed_scaler = (*strength / STRENGTH_SCALER);
  float sin_abs = abs(*sin_res);

  switch (*cad) {
    case FIRST:
    case FOURTH:
      left_strength = VITESSE_MIN_JOYSTICK + VITESSE_DIFF * speed_scaler;
      right_strength = VITESSE_MIN_JOYSTICK + VITESSE_DIFF * sin_abs * sin_abs * sin_abs * speed_scaler;
      break;
    case SECOND:
    case THIRD:
      right_strength = VITESSE_MIN_JOYSTICK + VITESSE_DIFF * speed_scaler;
      left_strength = VITESSE_MIN_JOYSTICK + VITESSE_DIFF * sin_abs * sin_abs * sin_abs * speed_scaler;
      break;
    case BACK:
    case FRONT:
    case LEFT:
    case RIGHT:
      left_strength = right_strength = VITESSE_MIN_JOYSTICK + VITESSE_DIFF * speed_scaler;
      break;
  }

  switch (*cad) {
    case FIRST:
    case SECOND:
    case FRONT:
      advance(right_strength, left_strength);
      break;
    case FOURTH:
    case THIRD:
    case BACK:
      back_off(right_strength, left_strength);
      break;
    case LEFT:
      turn_R_360(right_strength, left_strength);  // TODO: Verify why in Gyroscope it goes left
      break;
    case RIGHT:
      turn_L_360(right_strength, left_strength);  // TODO: Verify why in Gyroscope it goes right
      break;
  }
}

String angle = "";
float strength = 0;
float angle_rad;
float sin_result;
float cos_result;
int cad;

void mode_joystick(String* msg, bool* debug) {
  advance_joystick(msg, &angle_rad, &sin_result, &cos_result, &strength, &cad);

  if (*debug && Serial.availableForWrite() > 30) {
    Serial.print("sin:" + String(sin_result) + " cos:" + String(cos_result) + " str:" + String(strength) + " cad:" + String(cad)
                 + " <--: " + String(left_strength) + " -->: " + String(right_strength) + "!!!");
  }
  else if (Serial.availableForWrite() > 30) { // Tells the server that Arduino is ready to receive a command
    Serial.print("ok!!!");
  }
}