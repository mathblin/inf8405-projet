#!/usr/bin/python3
import socket
import serial
import logging
import threading
import queue
import time


class TransmitterThread(threading.Thread):

	def __init__(self, serialPort, conList, DistanceList, logger): 
		threading.Thread.__init__(self)
		self.serial = serialPort
		self.conList = conList
		self.DistanceList = DistanceList
		self.logger = logger
		self.stop_event = threading.Event()
		self.STOP_DISTANCE = 20.0
		self.CRIT_DISTANCE = 30.0

	def run(self):
		while not self.stopRequest():			
			if self.conList:
				connection = self.conList[len(self.conList)-1]
				try:
					recvCommand = connection.recv(1).decode()
					com = recvCommand.lower()
					if not recvCommand:
						self.serial.write('x'.encode())
						continue
					while com != 'a' and com != 'd' and com != 'w' and com != 's' and com != 'q'\
					and com != 'e' and com != 'z' and com != 'c' and com != 'x':
						recvCommand = connection.recv(1024).decode()
						com = recvCommand.lower()					
					if self.DistanceList:
						distance = self.DistanceList[0]						
						if distance <= self.STOP_DISTANCE and com != 'z' and com != 's' and com != 'c':
							self.serial.write('x'.encode())
							self.logger.debug('x')
							continue
						elif distance <= self.CRIT_DISTANCE and com != 'z' and com != 's' and com != 'c':
							recvCommand = recvCommand.lower()
					self.serial.write(recvCommand.encode())					
					self.logger.debug(recvCommand)
				except socket.error as e:
					self.serial.write('x'.encode())
					self.logger.debug('x')
					self.logger.info(str(e) + ": connection is interrupted ")
					self.conList.remove(connection)
					conn.close()
					self.logger.info("Server is listening ...[CTRL] + [C] to quit")
			else:
				#self.serial.write('x'.encode())
				#self.logger.debug('x')
				time.sleep(1)
		
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()
		
