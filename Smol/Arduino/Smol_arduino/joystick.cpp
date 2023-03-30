#include "joystick.h"
#include "common.h"

int vitesseMinJoystick = 80;
int vitesseMaxJoystick = 255;
int vitesseJoystick = vitesseMinJoystick;
int vitesseDifference = vitesseMaxJoystick - vitesseMinJoystick;
int left_strength;
int right_strength;
float strength_scaler = 100;

enum {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    FRONT,
    RIGHT,
    LEFT,
    BACK
};

int getCadrans(int angle) {
    // TODO: ajuster pour le tourner 360 degrees
    // TODO: ajuster les angles de 90 et 270
    if (angle < 90 && angle > 10) return FIRST;                                       // 0
    if (angle > 90 && angle < 170) return SECOND;                                     // 1
    if (angle > 190 && angle < 270) return THIRD;                                     // 2
    if (angle > 270 && angle < 350) return FOURTH;                                    // 3
    if (angle == 90) return FRONT;                                                    // 4
    if ((angle >= 0 && angle <= 10) || (angle <= 360 && angle >= 350)) return RIGHT;  // 5
    if (angle >= 170 && angle <= 190) return LEFT;                                    // 6
    if (angle == 270) return BACK;                                                    // 7

    return 100;  // Erreur
}

void advance_joystick(String* msg, float* angle_radi, float* sin_res, float* cos_res, float* strength, int* cad) {
    *strength = getValue(*msg, ',', STRENGTH_POSITION).toInt();
    // Calcul d'angles: https://arduinogetstarted.com/reference/arduino-sin ... adapter pour cos
    String angle = getValue(*msg, ',', ANGLE_POSITION);
    *angle_radi = angle.toInt() * M_PI / 180;
    *sin_res = sin(*angle_radi);
    *cos_res = cos(*angle_radi);
    *cad = getCadrans(angle.toInt());

    if (*strength == 0) {
        left_strength = right_strength = 0;
        stop();
        return;
    }

    float speed_scaler = (*strength / strength_scaler);

    switch (*cad) {
    case FIRST:
    case FOURTH:
        left_strength = vitesseMinJoystick + vitesseDifference * speed_scaler;
        right_strength = vitesseMinJoystick + vitesseDifference * abs(*sin_res) * speed_scaler;
        break;
    case SECOND:
    case THIRD:
        right_strength = vitesseMinJoystick + vitesseDifference * speed_scaler;
        left_strength = vitesseMinJoystick + vitesseDifference * abs(*sin_res) * speed_scaler;
        break;
    case BACK:
    case FRONT:
    case LEFT:
    case RIGHT:
        left_strength = right_strength = vitesseMinJoystick + vitesseDifference * speed_scaler;
        break;
    }

    switch (*cad) {
    case FIRST:
        advance(right_strength, left_strength);
        break;
    case FOURTH:
        back_off(right_strength, left_strength);
        break;
    case SECOND:
        advance(right_strength, left_strength);
        break;
    case THIRD:
        back_off(right_strength, left_strength);
        break;
    case BACK:
        back_off(right_strength, left_strength);
        break;
    case FRONT:
        advance(right_strength, left_strength);
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
            + " <--: " + String(left_strength) + " -->: " + String(right_strength));
    }
    else if (Serial.availableForWrite() > 30) { // Tells the server that Arduino is ready to receive a command
        Serial.print("ok");
    }
}