#!/usr/bin/env python
import RPi.GPIO as GPIO
import track_2
LedPin   = 10
TouchPin = 11
ledBlue = 12

def setup():
        GPIO.setmode(GPIO.BOARD)       # Numbers GPIOs by physical location
        GPIO.setup(LedPin, GPIO.OUT)   # Set LedPin's mode is output
        GPIO.setup(ledBlue, GPIO.OUT) 
        GPIO.setup(TouchPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        GPIO.output(ledBlue, GPIO.HIGH)
def loop():
        while True:
                if GPIO.input(TouchPin) == GPIO.HIGH:
                        GPIO.output(ledBlue, GPIO.LOW)
                        GPIO.output(LedPin, GPIO.HIGH) # led off
                        track_2.main()


def destroy():
        GPIO.output(LedPin, GPIO.LOW)     # led off
        GPIO.cleanup()                     # Release resource

if __name__ == '__main__':     # Program start from here
        setup()
        try:
                loop()
        except KeyboardInterrupt:  # When 'Ctrl+C' is pressed, the child program destroy() will be  executed.
                destroy()
