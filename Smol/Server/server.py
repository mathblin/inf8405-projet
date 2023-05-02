from time import sleep
from serial_comm import *
from socket_comm import *
from server_flags import *

# Arduino communication init
init_serial(DEBUGGER_MODE)

# Arduino's serial communication test if True
if DEBUG_SERIAL_COMM: test_serial_comm()

HOST = args.ip[0] if args.ip else DEFAULT_HOST # defaults parameters in server_flags.py
PORT = args.port[0] if args.port else DEFAULT_PORT

init_socket(HOST, PORT, DEBUGGER_MODE)

SAFE_EXIT_COMMAND = '1,x'

while 1:
    command = get_comman_from_socket()
    got_exception, new_command = check_exceptions(command)

    if got_exception:
        manage_socket_exception(new_command)
    else:
        write_command_to_arduino_with_response(command)
        print('command:', command)
        if command == SERVO_SECOND_ARM_COMMAND:
            print('Bonjour!')
