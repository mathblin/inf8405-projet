#!/usr/bin/python3
import serial
import sys
import os
import socket
import logging
import threading
import queue
import netifaces
import time
import datetime
import platform
from Import.sonar import SonarThread
from Import.transmitter import TransmitterThread
from Import.buzzer import BuzzerThread




def main(argv):
	TIMEOUT = 5
	PORT = 5050
	logSuffix = '.log'
	logPath = './Log'
	logger = getLogger(logPath, logSuffix)
	serialPort = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')
	ethernet = ''
	if platform.system().lower() == 'linux':
		try:
			ethernet = 'wlan0'
			netifaces.ifaddresses(ethernet)			
		except:
				ethernet = 'eth0'
				netifaces.ifaddresses(ethernet)
	elif platform.system().lower() == 'darwin':
		ethernet = 'en0'
		netifaces.ifaddresses(ethernet)		
	else:
		logger.warn('system platform: ' + platform.system() + ' does not support this program')
		sys.exit(1)
	host = ''
	host = netifaces.ifaddresses(ethernet)[netifaces.AF_INET][0]['addr']
	logger.info('Server IP address : ' + host)
	mySocket = socket.socket()
	mySocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	try:
		mySocket.bind((host,PORT))
	except OSError:
		logger.error("The port number" + str(PORT) + "is in use")

	#listen(): This method sets up and start TCP listener.
	mySocket.listen(1)
	logger.info('Server is listening ...')
	
	DistanceList = []
	conList = []
	#buzzer = BuzzerThread(DistanceList, logger)
	#buzzer.setDaemon(True)
	#buzzer.start()
	sonar = SonarThread(serialPort, conList, DistanceList, logger)
	sonar.setDaemon(True)
	sonar.start()
	transmitter = TransmitterThread(serialPort, conList, DistanceList, logger)
	transmitter.setDaemon(True)
	transmitter.start()
	while True:
		try:
			connection, cAddress = mySocket.accept()
			#connection.settimeout(5)
			conList.append(connection)
		except KeyboardInterrupt:
			break
		except socket.error as e:
			break
	mySocket.close()

	#buzzer.stop()
	#buzzer.join()
	sonar.stop()
	sonar.join()	
	transmitter.stop()
	transmitter.join()
	sys.exit(0)


def getLogger(logPath, logSuffix):
	formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')
	consoleHdlr = logging.StreamHandler(sys.stdout)	
	consoleHdlr.setFormatter(formatter)	
	logFileName = str(datetime.datetime.now()) + logSuffix
	if not os.path.exists(logPath):
		os.makedirs(logPath)	
	fileHdlr = logging.FileHandler(os.path.join(logPath, logFileName))
	fileHdlr.setFormatter(formatter)
	rootLogger = logging.getLogger()
	rootLogger.addHandler(consoleHdlr)
	rootLogger.addHandler(fileHdlr)
	logger = logging.getLogger("myLogger")
	logger.setLevel(logging.DEBUG)
	return logger


if __name__ == "__main__":
   main(sys.argv[1:])
