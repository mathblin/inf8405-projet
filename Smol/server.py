import sys
import socket
from time import sleep
from serial_comm import *
from socket_comm import *

DEBUGGER_MODE = True

# Arduino communication init
init_serial(DEBUGGER_MODE)

# Arduino communication test if True
DEBUG_SERIAL_COMM = False
if DEBUG_SERIAL_COMM:
    test_serial_comm()


#HOST = '132.207.186.54' #this is your localhost
#HOST = '127.0.0.1' #this is your localhost
# HOST = '10.200.0.163' # Math
# HOST = '169.254.137.33' # Math
HOST = '10.0.0.158' # Math
PORT = 5050

init_socket(HOST, PORT, DEBUGGER_MODE)
while 1:
    command = get_comman_from_socket()
    if command == 'exceded timout':
        manage_socket_exception(command)
    elif command == '':
        manage_socket_exception(command)
    write_command_to_arduino(command)
    print('command:', command)
    sleep(0.010) # To make the server a little more light weight. Power management

# s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# #socket.socket: must use to create a socket.
# #socket.AF_INET: Address Format, Internet = IP Addresses.
# #socket.SOCK_STREAM: two-way, connection-based byte streams.
# print ("socket created")

# #Bind socket to Host and Port
# try:
#     s.bind((HOST, PORT))
# except socket.error as err:
#   #  print ('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1]
#     sys.exit()

# print ('Socket Bind Success!')


# #listen(): This method sets up and start TCP listener.
# s.listen(10)
# print ('Socket is now listening')

# conn, addr = s.accept()
# print ('Connect with ' + addr[0] + ':' + str(addr[1]))

# # sys.exit(help(conn.timeout))
# conn.settimeout(5.0)

# receving = False
# try:
#     while 1:

#         recvCommand = conn.recv(64)

#         if recvCommand.decode('utf-8') == '':
#             print('no command')
#         else:
#             print (recvCommand)

        

#     s.close()

# except:
#     sys.exit('exceded timout')