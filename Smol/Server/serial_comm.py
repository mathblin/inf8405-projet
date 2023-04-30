import serial.tools.list_ports
import sys
from time import time, sleep
from server_flags import args
from enum import Enum
# serialInst = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0') # Deprecated: Old Raspberry Pi serial port.

serialInst = None
DEBUGGER_MODE = None
previous_time = 0
previous_read_time = 0
ARDUINO_SERIAL_READ_TIMEOUT = 0
class CONTROL_MODE(Enum):
  MODE_JOYSTICK = 0
  MODE_GYROSCOPE = 1
  MODE_SECOND_SERVO = 2

GYROSCOPE_COMMANDS = 'WwSsadqQeEzZcCx'
SERVO_SECOND_ARM_COMMAND = '2,1'

END_SERIAL_COMM_MARKER = '!!!'
DISCONNECTED_COMMAND = 'disconnect'

def init_serial(DEBUG_MODE = None):
    """Initialisation of the server socket
    It can differenciate if it is used by Windows or Linux
    """
    global current_time
    global previous_time
    global DEBUGGER_MODE
    DEBUGGER_MODE = DEBUG_MODE
    global serialInst
    
    if sys.platform == 'win32':
        com_path = "COM"

    else:
        com_path = "/dev/tty"
    portsList = []
    ports = serial.tools.list_ports.comports()

    for onePort in ports:
        portsList.append(str(onePort))
        print(str(onePort))


    portVar = "No usb port found"

    INVALID_COMM_PORT = True
    while INVALID_COMM_PORT:
        val = input("q to quit or Select Port: " + str(com_path))
        if val == 'q':
            sys.exit()
        for x in range(0, len(portsList)):
            if portsList[x].startswith(com_path + str(val)):
                portVar = com_path + str(val)
                print(portVar)
                INVALID_COMM_PORT = False
    serialInst = serial.Serial(portVar, timeout=ARDUINO_SERIAL_READ_TIMEOUT, baudrate=38400)
    previous_time = time()
    sleep(1)

def is_command_defined(command):
    if command == DISCONNECTED_COMMAND:
        return True, ''
    
    commands = command.split(',')
    if len(commands) < 2:
        message = "invalid command, not in format (mode,letter) or (mode,angle,power): " + str(commands)
        return False, message
    
    for section in commands:
        if section.replace(' ', '') == '':
            message = "invalid command, there is a blank space or empty parameter in the command: " + str(commands)
            return False, message
    
    gyro = str(CONTROL_MODE.MODE_GYROSCOPE.value)
    joy = str(CONTROL_MODE.MODE_JOYSTICK.value)
    servo = str(CONTROL_MODE.MODE_SECOND_SERVO.value)
    first_section_defined = commands[0] == gyro or commands[0] == joy or commands[0] == servo
    
    if not first_section_defined:
        message = "invalid command, mode is not Joystick, Gyroscope or servo: " + str(commands)
        return False, message
    
    if commands[0] == gyro:
        second_section_defined = commands[1] in GYROSCOPE_COMMANDS and len(commands) == 2
        if not second_section_defined:
            message = "invalid command, mode is Gyroscope, but not in format (mode,letter): " + str(commands)
            return False, message

    elif commands[0] == joy:
        # Order matters here. For instance, if the length is verify after commands[2] is access, server will crash
        second_section_defined = len(commands) == 3 \
            and commands[1].isnumeric() and commands[2].isnumeric() \
            and int(commands[1]) >= 0 \
            and int(commands[1]) <= 360 \
            and int(commands[2]) >= 0  \
            and int(commands[2]) <= 100
        if not second_section_defined:
            message = "invalid command, mode is Joystick, but not in format (mode,angle,power): " + str(commands)
            return False, message
    
    elif commands[0] == servo:
        second_section_defined = len(commands) == 2 \
            and command == SERVO_SECOND_ARM_COMMAND
        if not second_section_defined:
            message = "invalid command, mode is Servo (second arm), but not in format (mode,activation): " + str(commands)
            return False, message

    return True, ''

def can_send_command_to_arduino(command):
    """This fonction verify if enough time has passed in between two 
    serial communication on the arduino
    """
    command_defined, message = is_command_defined(command)
    if (not command_defined): #TODO: verify if is_command_defined() is reliable
        print(message)
        return False # TODO: verifier
    return True # TODO: Verify if the rest is fine

def can_read_message_from_arduino():
    """This fonction verify if enough time has passed in between two 
    serial communication on the arduino
    """
    global previous_read_time
    current_time = time()
    time_diff = current_time - previous_read_time
    can_read_message = time_diff > ARDUINO_SERIAL_READ_TIMEOUT
    if can_read_message:
        previous_read_time = current_time
    return can_read_message


def write_command_to_arduino(command):
    """This fonction sends the robot command over serial communication
    to the Arduino
    """
    if can_send_command_to_arduino(command):
        command = str(int(args.debug)) + ',' + command
        serialInst.write(command.encode("UTF-8"))
        serialInst.flush()
        return True
    else:
        return False

def blocking_read_from_arduino():
    """Fonction to test serial communication with verbose data from Arduino"""
    message = ''
    # while (serialInst.inWaiting() == 0):
    #         pass
    # packetIn = serialInst.readline()
    # packetIn = packetIn.decode("UTF-8").strip('\r\n')
    # message = packetIn

    while END_SERIAL_COMM_MARKER not in message:
        while (serialInst.inWaiting() == 0):
            pass
        packetIn = serialInst.readline()
        packetIn = packetIn.decode("UTF-8").strip('\r\n')
        message += packetIn
    

    if DEBUGGER_MODE: 
        message = message.strip(END_SERIAL_COMM_MARKER)
        print("Arduino's answer:", message)

def write_command_to_arduino_with_response(command):
    could_send_command = write_command_to_arduino(command)
    if could_send_command: blocking_read_from_arduino()

def test_serial_comm():
    """Fonction to test serial communication without socket"""
    while True:
        command = input("Your Arduino command (h for help, q to quit): ")

        if command == "h":
            message = """Command: (Mode,args...) *Include comas, but without spaces nor parentesis*
            Joystick: ({0},angle,power)
            Gyroscope: ({1},letter)
            Servo (for the second arm): ({2},activation)
            """.format(CONTROL_MODE.MODE_JOYSTICK.value, CONTROL_MODE.MODE_GYROSCOPE.value, CONTROL_MODE.MODE_SECOND_SERVO.value)
            print(message)

        elif command == "q":
            print(serialInst.timeout)
            serialInst.close()
            exit()
        else:
            could_send_command = write_command_to_arduino(command)
            if DEBUGGER_MODE and could_send_command: blocking_read_from_arduino()

