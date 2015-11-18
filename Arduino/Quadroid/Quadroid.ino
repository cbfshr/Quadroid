#include <Usb.h> //library to use the USB port
#include <AndroidAccessory.h>

#define THRUST_OUTPUT_PIN 3
#define YAW_OUTPUT_PIN 5
#define ROLL_OUTPUT_PIN 6
#define PITCH_OUTPUT_PIN 9

#define MAX_OUTPUT_PWM 200
#define HALF_OUTPUT_PWM_PITCH 92
#define HALF_OUTPUT_PWM_ROLL 92

/* 0V     ~ analog 0
 * 1.65V  ~ analog 84
 * 2.5V   ~ analog 128
 * 3.3V   ~ analog 168
 */

//Connection to Tablet
AndroidAccessory acc(
  "cpre388",
  "sketchpad",
  "Description",
  "1.0",
  "cpre388.iastate.edu",
  "0000000012345679"
);

void setup() {
  initValues();

  Serial.begin(9600);   // Start serial
  acc.powerOn();        // Start up android app

  delay(1000);
}
void initValues() {
  pinMode(THRUST_OUTPUT_PIN, OUTPUT);
  pinMode(YAW_OUTPUT_PIN, OUTPUT);
  pinMode(ROLL_OUTPUT_PIN, OUTPUT);
  pinMode(PITCH_OUTPUT_PIN, OUTPUT);
}

// The startup protocol should be callled when a button is pressed.
// That way, the application is already connected to the Arduino.
void startupProtocol() {
  delay(100);
  analogWrite(THRUST_OUTPUT_PIN, MAX_OUTPUT_PWM);
  delay(200);
  analogWrite(THRUST_OUTPUT_PIN, 0);
  delay(100);
}

void loop() {
  byte msg[6];
  int thrust = 0;
  int yaw = 0;
  int roll = 0;
  int pitch = 0;

  //msg[0] - yaw
  //msg[1] - thrust
  //msg[2] - roll
  //msg[3] - pitcg
  
  if(acc.isConnected()) {
    int len = acc.read(msg, sizeof(msg), 1);
    
    if(len > 0) {
      // THRUST
      if((int)msg[1] < 10) {
        analogWrite(THRUST_OUTPUT_PIN, 0);
      } else {
        analogWrite(THRUST_OUTPUT_PIN, (((int)msg[1] * MAX_OUTPUT_PWM)) / 100);
      }

      // YAW
      Serial.print((int)msg[0]);
      Serial.print('\n');
      if((int)msg[0] < 35) {
        analogWrite(YAW_OUTPUT_PIN, 3);
      } else if((int)msg[0] > 65) {
        analogWrite(YAW_OUTPUT_PIN, 255);        
      } else {
        analogWrite(YAW_OUTPUT_PIN, 40);
      }

      // ROLL
      roll = normalizeRoll((int)msg[2]);
      analogWrite(ROLL_OUTPUT_PIN, roll);
      /*if((int)msg[2] > 40 && (int)msg[2] < 60) {
        analogWrite(ROLL_OUTPUT_PIN, HALF_OUTPUT_PWM_ROLL);
      } else {
        analogWrite(ROLL_OUTPUT_PIN, ((int)msg[2] * MAX_OUTPUT_PWM) / 100); 
      }*/
     

      // PITCH
      pitch = normalizePitch((int)msg[3]);
      
      analogWrite(PITCH_OUTPUT_PIN, pitch);
      /*if((int)msg[3] > 40 && (int)msg[3] < 60) {
        analogWrite(PITCH_OUTPUT_PIN, 200);
      } else {
        analogWrite(PITCH_OUTPUT_PIN, ((int)msg[3] * MAX_OUTPUT_PWM) / 100);
        //Serial.print(msg[3]);
        //Serial.print('\n');
        //Serial.print(   ((int)msg[3] * MAX_OUTPUT_PWM) / 100);
        //Serial.print('\n');
      }*/
    }
  }
}

int normalizeRoll(int rollValue) {
  if(rollValue > 40 && rollValue < 60) {
    return 100;
  }
  if(rollValue >= 0 && rollValue <= 10) {
    return 3;
  }
  if(rollValue > 10 && rollValue <= 20) {
    return 6;
  }
  if(rollValue > 20 && rollValue <= 30) {
    return 9;
  }
  if(rollValue > 30 && rollValue <= 40) {
    return 12;
  }
  if(rollValue > 60 && rollValue <= 70) {
    return 240;
  }
  if(rollValue > 70 && rollValue <= 80) {
    return 245;
  }
  if(rollValue > 80 && rollValue <= 90) {
    return 250;
  }
  if(rollValue > 90 && rollValue <= 100) {
    return 255;
  }
}

int normalizePitch(int rollValue) {
  if(rollValue > 40 && rollValue < 60) {
    return 100;
  }
  if(rollValue >= 0 && rollValue <= 10) {
    return 3;
  }
  if(rollValue > 10 && rollValue <= 20) {
    return 6;
  }
  if(rollValue > 20 && rollValue <= 30) {
    return 9;
  }
  if(rollValue > 30 && rollValue <= 40) {
    return 12;
  }
  if(rollValue > 60 && rollValue <= 70) {
    return 240;
  }
  if(rollValue > 70 && rollValue <= 80) {
    return 245;
  }
  if(rollValue > 80 && rollValue <= 90) {
    return 250;
  }
  if(rollValue > 90 && rollValue <= 100) {
    return 255;
  }
}

