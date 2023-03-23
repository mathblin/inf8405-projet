//Standard PWM DC control
int E1 = 5;     //M1 Speed Control
int E2 = 6;     //M2 Speed Control
int M1 = 4;    //M1 Direction Control
int M2 = 7;    //M1 Direction Control

enum State {
  STOP,
  FORWARD,
  BACKWARD,
  ROT_LEFT_FRONT,
  ROT_LEFT_BACK,
  ROT_LEFT_360,
  ROT_RIGHT_FRONT,
  ROT_RIGHT_BACK,
  ROT_RIGHT_360,
  STATE_COUNT
};

enum Flags {
  DEBUG_POSITION = 0,
  MODE_POSITION = 1,
  LETTER_POSITION = 2,
  ANGLE_POSITION = 2,
  STRENGTH_POSITION = 3,
};

State currentState = STOP ;

int vitesseMin = 80;
int vitesseMax = 255;
int vitesse = vitesseMin;
int vitesseIncrement = 17;
int vitesseIncrementRotation = 15;
int vitesseRotationMin = 80;
int vitesseLow = vitesseRotationMin;
// int MODE_JOYSTICK = 0, MODE_GYROSCOPE = 1;
enum CONTROL_MODE {
  MODE_JOYSTICK = 0, MODE_GYROSCOPE = 1
};

// Ajout projet Smol
int vitesseDifference = vitesseMax - vitesseMin;
int left_strength;
int right_strength;
float strength_scaler = 100;

// getValue function taken from this link:
// https://stackoverflow.com/questions/9072320/split-string-into-string-array
 String getValue(String data, char separator, int index)
{
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length()-1;

  for(int i=0; i<=maxIndex && found<=index; i++){
    if(data.charAt(i)==separator || i==maxIndex){
        found++;
        strIndex[0] = strIndex[1]+1;
        strIndex[1] = (i == maxIndex) ? i+1 : i;
    }
  }

  return found>index ? data.substring(strIndex[0], strIndex[1]) : "";
}

void stop(void)                    //Stop
{
  digitalWrite(E1,LOW);   
  digitalWrite(E2,LOW);      
}   
void advance(char a,char b)          //Move forward
{
  analogWrite (E1,a);      //PWM Speed Control
  digitalWrite(M1,HIGH);    
  analogWrite (E2,b);    
  digitalWrite(M2,HIGH);
}  
void back_off (char a,char b)          //Move backward
{
  analogWrite (E1,a);
  digitalWrite(M1,LOW);   
  analogWrite (E2,b);    
  digitalWrite(M2,LOW);
}
void turn_L_360 (char a,char b)             //Turn Left
{
  analogWrite (E1,a);
  digitalWrite(M1,LOW);    
  analogWrite (E2,b);    
  digitalWrite(M2,HIGH);
}  
void turn_R_360 (char a,char b)             //Turn Right
{
  analogWrite (E1,a);
  digitalWrite(M1,HIGH);    
  analogWrite (E2,b);    
  digitalWrite(M2,LOW);
} 

/////////////////// JOYSTICK ///////////////////
enum
{
  FIRST, SECOND, THIRD, FOURTH, FRONT, RIGHT, LEFT, BACK
};

int getCadrans(int angle)
{
  // TODO: ajuster pour le tourner 360 degrees
  if (angle < 90)                 return FIRST;   // 0
  if (angle > 90 && angle <= 180) return SECOND;  // 1
  if (angle > 180 && angle < 270) return THIRD;   // 2
  if (angle > 270 && angle < 360) return FOURTH;  // 3
  if (angle == 90)                return FRONT;   // 4
  if (angle == 0 || angle == 360) return RIGHT;   // 5
  if (angle == 180)               return LEFT;    // 6
  if (angle == 270)               return BACK;    // 7

  return 100;                                     // Erreur
}

void advance_joystick(String* msg, float* angle_radi, float* sin_res, float* cos_res, float* strength, int* cad)
{
  // Calcul d'angles: https://arduinogetstarted.com/reference/arduino-sin ... adapter pour cos
  *strength = getValue(*msg, ',', STRENGTH_POSITION).toInt();
  String angle = getValue(*msg, ',', ANGLE_POSITION);
  *angle_radi = angle.toInt() * M_PI / 180;
  *sin_res = sin(*angle_radi);
  *cos_res = cos(*angle_radi);
  *cad = getCadrans(angle.toInt());

  float speed_scaler = (*strength / strength_scaler);

  switch(*cad) {
    case FIRST:
    case FOURTH:
    // left_strength = vitesseMin + vitesseDifference * *cos_res; // TODO: Enlever si pas necessaire
    // right_strength = vitesseMin + vitesseDifference * *sin_res; // TODO: Enlever si pas necessaire
    left_strength = vitesseMin + vitesseDifference * speed_scaler;
    right_strength = vitesseMin + vitesseDifference * abs(*sin_res) * speed_scaler;
    // advance(right_strength, left_strength);
    break;
    case SECOND:
    case THIRD:
    right_strength = vitesseMin + vitesseDifference * speed_scaler;
    left_strength = vitesseMin + vitesseDifference * abs(*sin_res) * speed_scaler;
    // back_off(left_strength, right_strength);
    break;
  }

  switch(*cad) {
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
  }

}

String angle = "";
float strength = 0;
float angle_rad;
float sin_result;
float cos_result;
int cad;

void mode_joystick(String* msg)
{
  advance_joystick(msg, &angle_rad, &sin_result, &cos_result, &strength, &cad);

  if (Serial.availableForWrite() > 30){
    
    Serial.print("sin:" + String(sin_result) + " cos:" + String(cos_result) + " str:"+  String(strength) + " cad:" + String(cad) \
    + " <--: " + String(left_strength) + " -->: " + String(right_strength));
  }
  
  delay(2000);
  stop();
}
////////////////////////////////////////////////

/////////////////// GYROSCOPE //////////////////
void mode_gyroscope(String* msg)
{
  // if (Serial.availableForWrite() > 30) Serial.print("mode :" + String(mode) + " debug: " + String(debug));
      vitesse = vitesseMax;

      {
        char command = getValue(*msg, ',', LETTER_POSITION)[0];
        
        if(command != -1)
        {
          switch(command)
          {
          case 'W'://Move Forward
            if (currentState == FORWARD)
            {
              vitesse = vitesse + vitesseIncrement;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
            }
            else
            {
              vitesse = vitesseMin;
            }
            advance (vitesse,vitesse); //move forward in max speed
            break;

          case 'w':// reduce front speed
          if (currentState == FORWARD)
          {
            vitesse = vitesse - vitesseIncrement;
            if (vitesse < vitesseMin)
            {
              vitesse = vitesseMin;
            }
          }
          else
          {
            vitesse = vitesseMin;
          }
          advance (vitesse,vitesse); //move forward in max speed
          break;
            
          case 'S'://Move Backward
            if (currentState == BACKWARD)
            {
              vitesse = vitesse + vitesseIncrement;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
            }
            else
            {
              vitesse = vitesseMin;
            }
            back_off (vitesse,vitesse);   //move back in max speed
            break;

          case 's'://Move Backward
            if (currentState == BACKWARD)
            {
              vitesse = vitesse - vitesseIncrement;
              if (vitesse < vitesseMin)
              {
                vitesse = vitesseMin;
              }
            }
            else
            {
              vitesse = vitesseMin;
            }
            back_off (vitesse,vitesse);   //move back in max speed
            break;
            
          case 'a'://Turn Left 360
          if (currentState == ROT_LEFT_360)
            {
              vitesse = vitesse + vitesseIncrement;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
            }
            else
            {
              vitesse = vitesseRotationMin;
            }
            turn_L_360 (vitesse,vitesse);
            break;       
          case 'd'://Turn Right
          if (currentState == ROT_RIGHT_360)
            {
              vitesse = vitesse + vitesseIncrement;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
            }
            else
            {
              vitesse = vitesseRotationMin;
            }
            turn_R_360 (vitesse,vitesse);
            break;
          
          case 'q'://Turn left 
          if (currentState == ROT_LEFT_FRONT)
            {
              vitesseLow = vitesseLow + vitesseIncrementRotation;
              
              if (vitesseLow > vitesse)
              {
                vitesseLow = vitesse;
              }
            }
            else
            {
              vitesse = vitesseRotationMin - vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            advance(vitesse,vitesseLow);
            break;

          case 'Q'://Turn left 
          if (currentState == ROT_LEFT_FRONT)
            {
              vitesse = vitesse + vitesseIncrementRotation;
              vitesseLow = vitesseLow - vitesseIncrementRotation;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
              if (vitesseLow < vitesseMin)
              {
                vitesseLow = vitesseRotationMin;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            advance(vitesse,vitesseLow);
            break;

          case 'e'://Turn right 
          if (currentState == ROT_RIGHT_FRONT)
            {
              vitesseLow = vitesseLow + vitesseIncrementRotation;
              
              if (vitesseLow > vitesse)
              {
                vitesseLow = vitesse;
              }
            }
            else
            {
              vitesse = vitesseRotationMin - vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            advance(vitesseLow,vitesse);
            break;

          case 'E'://Turn right 
          if (currentState == ROT_RIGHT_FRONT)
            {
              vitesse = vitesse + vitesseIncrementRotation;
              vitesseLow = vitesseLow - vitesseIncrementRotation;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
              if (vitesseLow < vitesseMin)
              {
                vitesseLow = vitesseRotationMin;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            advance(vitesseLow,vitesse);
            break;
            
          case 'z'://Turn left 
          if (currentState == ROT_LEFT_BACK)
            {
              vitesseLow = vitesseLow + vitesseIncrementRotation;
              
              if (vitesseLow > vitesse)
              {
                vitesseLow = vitesse;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            back_off(vitesse,vitesseLow);
            break;

          case 'Z'://Turn left 
          if (currentState == ROT_LEFT_BACK)
            {
              vitesse = vitesse + vitesseIncrementRotation;
              vitesseLow = vitesseLow - vitesseIncrementRotation;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
              if (vitesseLow < vitesseMin)
              {
                vitesseLow = vitesseRotationMin;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            back_off(vitesse,vitesseLow);
            break;

          case 'c'://Turn right 
          if (currentState == ROT_RIGHT_BACK)
            {
              vitesseLow = vitesseLow + vitesseIncrementRotation;
              
              if (vitesseLow > vitesse)
              {
                vitesseLow = vitesse;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            back_off(vitesseLow,vitesse);
            break;

          case 'C'://Turn right 
          if (currentState == ROT_RIGHT_BACK)
            {
              vitesse = vitesse + vitesseIncrementRotation;
              vitesseLow = vitesseLow - vitesseIncrementRotation;
              if (vitesse > vitesseMax)
              {
                vitesse = vitesseMax;
              }
              if (vitesseLow < vitesseMin)
              {
                vitesseLow = vitesseRotationMin;
              }
            }
            else
            {
              vitesse = vitesseRotationMin + vitesseIncrementRotation;
              vitesseLow = vitesseRotationMin;
            }
            back_off(vitesseLow,vitesse);
            break;
            
          case 'x':
            stop();
            break;
          default :
            stop();
            break;
          }
          switch(command)
          {
            case 'a':
              currentState = ROT_LEFT_360;
              break;
            case 'd':
              currentState = ROT_RIGHT_360;
              break;
            case 'w':
              currentState = FORWARD;
              break;
            case 'W':
              currentState = FORWARD;
              break;
            case 's':
              currentState = BACKWARD;
              break;
            case 'S':
              currentState = BACKWARD;
              break;
            case 'x':
              currentState = STOP;
              break;
            case 'q':
              currentState = ROT_LEFT_FRONT;
              break;
            case 'Q':
              currentState = ROT_LEFT_FRONT;
              break;
            case 'e':
              currentState = ROT_RIGHT_FRONT;
              break;
            case 'E':
              currentState = ROT_RIGHT_FRONT;
              break;     
            case 'z':
              currentState = ROT_LEFT_BACK;
              break;
            case 'Z':
              currentState = ROT_LEFT_BACK;
              break;  
            case 'c':
              currentState = ROT_RIGHT_BACK;
              break;   
            case 'C':
              currentState = ROT_RIGHT_BACK;
              break;  
          }
        }
        else 
        {
          stop();
        }
      }
}


void setup(void) 
{ 
  int i;
  for(i=4;i<=7;i++)
    pinMode(i, OUTPUT);  
  Serial.begin(9600);      //Set Baud Rate
  // Serial.println("Run keyboard control");
} 
void loop(void) 
{
  bool debug = false;

  String msg = "";
  int mode = 1000;

  while(!Serial.available());

  if(Serial.available()){
    msg = Serial.readString();

    mode = getValue(msg, ',', MODE_POSITION).toInt();
    debug = getValue(msg, ',', DEBUG_POSITION).toInt();

    switch(mode)
    {
    case MODE_JOYSTICK:
      mode_joystick(&msg);
    break;
    case MODE_GYROSCOPE:
      if (Serial.availableForWrite() > 30) Serial.print("mode :" + String(mode) + " debug: " + String(debug));
      mode_gyroscope(&msg);
    break;
    } 
  }

}
