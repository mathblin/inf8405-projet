#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
import threading


class BuzzerThread(threading.Thread):
	
			
	def __init__(self, DistanceList, logger): 
			threading.Thread.__init__(self)
			self.DistanceList = DistanceList
			self.logger = logger
			self.STOP_DISTANCE = 20.0
			self.stop_event = threading.Event()
			self.setup()


	def playTone(self, p, tone):
			# calculate duration based on speed and tone-length
		duration = (1./(tone[1]*0.25*self.SPEED))

		if tone[0] == "p": # p => pause
			time.sleep(duration)
		else: # let's rock
			frequency = self.TONES[tone[0]]
			p.ChangeFrequency(frequency)
			p.start(0.5)
			time.sleep(duration)
			p.stop()

	def run(self):
		p = GPIO.PWM(self.GPIO_BUZZER, 440)
		while not self.stopRequest():
			if self.DistanceList:
				distance = self.DistanceList[0]
				if distance > self.STOP_DISTANCE :
					p.stop()
					time.sleep(0.2)
					continue			
				p.start(0.5)
				maxValue = 1
				if maxValue >= len(self.SONG):
					maxValue = len(self.SONG) - 1
				elif maxValue < 5:
					maxValue = 5
				for t in range(0, maxValue):	
					self.playTone(p, self.SONG[t])
				self.logger.debug("Play buzzer value : " + str(maxValue))
		self.destroy()
	
	
	def setup(self):
		# List of tone-names with frequency
		self.TONES = {"c6":1047,
			"b5":988,
			"a5":880,
			"g5":784,
			"f5":698,
			"e5":659,
			"eb5":622,
			"d5":587,
			"c5":523,
			"b4":494,
			"a4":440,
			"ab4":415,
			"g4":392,
			"f4":349,
			"e4":330,
			"d4":294,
			"c4":262}

		# Song is a list of tones with name and 1/duration. 16 means 1/16
		self.SONG =	[
			["e5",16],["eb5",16],
			["e5",16],["eb5",16],["e5",16],["b4",16],["d5",16],["c5",16],
			["a4",8],["p",16],["c4",16],["e4",16],["a4",16],
			["b4",8],["p",16],["e4",16],["ab4",16],["b4",16],
			["c5",8],["p",16],["e4",16],["e5",16],["eb5",16],
			["e5",16],["eb5",16],["e5",16],["b4",16],["d5",16],["c5",16],
			["a4",8],["p",16],["c4",16],["e4",16],["a4",16],
			["b4",8],["p",16],["e4",16],["c5",16],["b4",16],["a4",4]
			]
		#GPIO Mode (BOARD / BCM)
		GPIO.setmode(GPIO.BCM)
		 
		#set GPIO Pins
		self.GPIO_BUZZER = 15
		
		self.SPEED = 1
		 
		#set GPIO direction (IN / OUT)
		GPIO.setup(self.GPIO_BUZZER, GPIO.OUT)
		
	def destroy(self):
		#GPIO.output(self.GPIO_BUZZER, GPIO.HIGH)
		GPIO.cleanup()
	
		
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()


