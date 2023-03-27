from time import sleep
from serial_comm import *
from socket_comm import *
from server_flags import *

# Arduino communication init
init_serial(DEBUGGER_MODE)

# Arduino communication test if True
if DEBUG_SERIAL_COMM: test_serial_comm()

HOST = args.ip[0] if args.ip else DEFAULT_HOST
PORT = args.port[0] if args.port else DEFAULT_PORT

init_socket(HOST, PORT, DEBUGGER_MODE)

while 1:
    command = get_comman_from_socket()
    if command == 'exceded timout':
        # write_command_to_arduino(command) # TODO: decommanter
        manage_socket_exception(command)
    elif command == '':
        # write_command_to_arduino(command) # TODO: decommanter
        manage_socket_exception(command)
    # write_command_to_arduino(command) # TODO: decommanter
    print('command:', command)
    sleep(0.010) # To make the server a little more light weight. Power management
