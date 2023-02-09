#!/usr/bin/python3
import RPi.GPIO as GPIO
import socket
import serial
import logging
import threading
import time
import sys
from enum import Enum


class State(Enum):

	SECURE = 0
	WARNING = 1
	CRITICAL = 2
	STOP = 3


class SonarThread(threading.Thread):
	
	def __init__(self, serialPort, conList, DistanceList, logger, isInit): 
		threading.Thread.__init__(self)
		self.conList = conList
		self.serial = serialPort
		self.DistanceList = DistanceList
		self.logger = logger
		self.isInit = isInit
		self.stop_event = threading.Event()
		self.WARN_DISTANCE = 40.0
		self.CRIT_DISTANCE = 30.0
		self.STOP_DISTANCE = 20.0
		self.DistanceList.append(self.WARN_DISTANCE)
		self.currentState = State(0).name
		self.REMOTE_SERVER = "www.google.com"
		self.setup()

	def run(self):
		while not self.stopRequest() and self.isInit and self.isInit[0]:
			self.switchOffRgbLed()			
			if not self.conList:
				time.sleep(1)
				continue
			distance = self.distance()
			formattedDistance = format(distance, '.1f')
			if not self.is_connected(self.REMOTE_SERVER):
				GPIO.output(self.GPIO_BLUE_LIGHT, True)
				self.serial.write('x'.encode())
				time.sleep(1)
			elif distance < self.STOP_DISTANCE:
				if self.currentState != State(3).name:
					self.serial.write('x'.encode())
				#self.logger.info("Stop Distance : " + formattedDistance)
				GPIO.output(self.GPIO_RED_LIGHT, True)
				self.currentState = State(3).name			
			elif distance < self.CRIT_DISTANCE:
				#self.logger.info("Critical Distance : " + formattedDistance)
				GPIO.output(self.GPIO_GREEN_LIGHT, True)
				GPIO.output(self.GPIO_BLUE_LIGHT, True)
				self.currentState = State(2).name
			elif distance < self.WARN_DISTANCE:
				#self.logger.info("Warning Distance : " + formattedDistance)				
				GPIO.output(self.GPIO_GREEN_LIGHT, True)
				
				self.currentState = State(1).name
			else:
				self.currentState = State(0).name
			self.DistanceList[0] = distance
			
			time.sleep(0.2)			
				
		self.destroy()
		
	def distance(self):
		# set Trigger to HIGH
		GPIO.output(self.GPIO_TRIGGER, True)

		# set Trigger after 0.01ms to LOW
		time.sleep(0.00001)
		GPIO.output(self.GPIO_TRIGGER, False)

		StartTime = time.time()
		StopTime = time.time()

		# save StartTime
		while GPIO.input(self.GPIO_ECHO) == 0:
			StartTime = time.time()

		# save time of arrival
		while GPIO.input(self.GPIO_ECHO) == 1:
			StopTime = time.time()

		# time difference between start and arrival
		TimeElapsed = StopTime - StartTime
		# multiply with the sonic speed (34300 cm/s)
		# and divide by 2, because there and back
		distance = (TimeElapsed * 34300) / 2

		return distance
	
	def switchOffRgbLed(self):
		GPIO.output(self.GPIO_BLUE_LIGHT, False)
		GPIO.output(self.GPIO_GREEN_LIGHT, False)
		GPIO.output(self.GPIO_RED_LIGHT, False)
		
	def setup(self):
		#GPIO Mode (BOARD / BCM)
		GPIO.setmode(GPIO.BCM)
		 
		#set GPIO Pins
		self.GPIO_TRIGGER = 23
		self.GPIO_ECHO = 24
		self.GPIO_RED_LIGHT = 16
		self.GPIO_GREEN_LIGHT = 20
		self.GPIO_BLUE_LIGHT = 21
		 
		#set GPIO direction (IN / OUT)
		GPIO.setup(self.GPIO_TRIGGER, GPIO.OUT)
		GPIO.setup(self.GPIO_ECHO, GPIO.IN)
		GPIO.setup(self.GPIO_RED_LIGHT, GPIO.OUT)
		GPIO.setup(self.GPIO_GREEN_LIGHT, GPIO.OUT)
		GPIO.setup(self.GPIO_BLUE_LIGHT, GPIO.OUT)
		
	def destroy(self):
		#GPIO.output(self.GPIO_TRIGGER, GPIO.HIGH)
		#GPIO.output(self.GPIO_ECHO, GPIO.HIGH)
		#GPIO.output(self.GPIO_RED_LIGHT, GPIO.HIGH)
		#GPIO.output(self.GPIO_GREEN_LIGHT, GPIO.HIGH)
		#GPIO.output(self.GPIO_BLUE_LIGHT, GPIO.HIGH)
		GPIO.cleanup()
		
	
	def is_connected(self, hostname):
		try:
			# see if we can resolve the host name -- tells us if there is
			# a DNS listening
			host = socket.gethostbyname(hostname)
			# connect to the host -- tells us if the host is actually
			# reachable
			s = socket.create_connection((host, 80), 2)
			return True
		except:
			pass
		return False
	
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()


