#define THRUST_OUTPUT_PIN 3
#define YAW_OUTPUT_PIN 5
#define ROLL_OUTPUT_PIN 6
#define PITCH_OUTPUT_PIN 9

#define MAX_OUTPUT_PWM 170
#define HALF_OUTPUT_PWM 85

/* 0V ~ analog 0
 * 1.65V ~ analog 84
 * 2.5V ~ analog 128
 * 3.3V ~ analog 168
 */

void setup() {
  initValues();
  startupProtocol();
  delay(1000);
}

void initValues() {
  pinMode(THRUST_OUTPUT_PIN, OUTPUT);
  pinMode(YAW_OUTPUT_PIN, OUTPUT);
  pinMode(ROLL_OUTPUT_PIN, OUTPUT);
  pinMode(PITCH_OUTPUT_PIN, OUTPUT);
}

void startupProtocol() {
  delay(100);
  analogWrite(THRUST_OUTPUT_PIN, MAX_OUTPUT_PWM);
  delay(200);
  analogWrite(THRUST_OUTPUT_PIN, 0);
  delay(100);
}

void loop() {
  testThrust();
  //analogWrite(THRUST_OUTPUT_PIN, HALF_OUTPUT_PWM);
  delay(500);
  exit(0);
}

void testYaw() {
  // ~0V
  analogWrite(THRUST_OUTPUT_PIN, 0);
  delay(50);
  analogWrite(THRUST_OUTPUT_PIN, 50);
  delay(100);
  
  // ~1.65V
  analogWrite(YAW_OUTPUT_PIN, HALF_OUTPUT_PWM);
  delay(100);
  
  // ~3.3V
  analogWrite(YAW_OUTPUT_PIN, MAX_OUTPUT_PWM);
  delay(2500);


  // ~1.65V
  analogWrite(YAW_OUTPUT_PIN, HALF_OUTPUT_PWM);
  delay(1000);

  // ~0V
  analogWrite(YAW_OUTPUT_PIN, 0);
  delay(2500);

  // ~1.65V
  analogWrite(YAW_OUTPUT_PIN, HALF_OUTPUT_PWM);
}

// Be careful with this. The thrust goes from 0% to 100%.
void testThrust() {
  int thrust = 0;
  delay(1000);
  
  // ~0V
  analogWrite(THRUST_OUTPUT_PIN, 0);
  delay(50);
  
  for(; thrust < MAX_OUTPUT_PWM; thrust++) {
    analogWrite(THRUST_OUTPUT_PIN, thrust);
    delay(50);
  }

  // ~0V
  analogWrite(THRUST_OUTPUT_PIN, 0);
  delay(50);
  
  // ~3.3V
  analogWrite(THRUST_OUTPUT_PIN, MAX_OUTPUT_PWM);
  delay(500);

  // ~1.65V
  analogWrite(THRUST_OUTPUT_PIN, HALF_OUTPUT_PWM);
  delay(500);

  // ~0V
  analogWrite(THRUST_OUTPUT_PIN, 0);
}

