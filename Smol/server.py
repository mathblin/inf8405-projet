from time import sleep
from serial_comm import *
from socket_comm import *

DEBUGGER_MODE = False

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
HOST_MATH = '10.0.0.158' # Math
HOST = HOST_MATH
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
