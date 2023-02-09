import socket
import sys
 
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('192.168.50.33', 8888)) #IP is the server IP
 
while True:
	command = raw_input("Enter command: ")
	print "Command[w,s,a,d,x,z]: ", command
	s.send(command)
