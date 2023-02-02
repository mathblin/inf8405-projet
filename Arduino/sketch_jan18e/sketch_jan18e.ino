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

State currentState = STOP ;

int vitesseMin = 80;
int vitesseMax = 255;
int vitesse = vitesseMin;
int vitesseIncrement = 17;
int vitesseIncrementRotation = 15;
int vitesseRotationMin = 80;
int vitesseLow = vitesseRotationMin;

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

void setup(void) 
{ 
  int i;
  for(i=4;i<=7;i++)
    pinMode(i, OUTPUT);  
  Serial.begin(9600);      //Set Baud Rate
  Serial.println("Run keyboard control");
} 
void loop(void) 
{
   bool DEBUG = true;
   if (DEBUG)
    {
      vitesse = vitesseMax;
      // advance(vitesse, vitesse);
      turn_L_360(vitesse, vitesse);
    }
   else
   {

      while(!Serial.available());

      advance (vitesse,vitesse); //move forward in max speed

    char command = Serial.read();
    
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
