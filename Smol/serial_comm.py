import serial.tools.list_ports
import sys
from time import time, sleep

# serialInst = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')

serialInst = None
DEBUGGER_MODE = None
previous_time = 0
ARDUINO_SERIAL_COMM_MIN_TIME_DIFF = 1.5

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
    serialInst = serial.Serial(portVar)
    previous_time = time()
    sleep(ARDUINO_SERIAL_COMM_MIN_TIME_DIFF)
    # serialInst = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')

def can_send_command_to_arduino():
    """This fonction verify if enough time has passed in between two 
    serial communication on the arduino
    """
    global previous_time
    current_time = time()
    time_diff = current_time - previous_time
    print('time diff:', time_diff)
    can_send_command = time_diff > ARDUINO_SERIAL_COMM_MIN_TIME_DIFF
    if can_send_command:
        previous_time = current_time
    return can_send_command


def write_command_to_arduino(command):
    """This fonction sends the robot command over serial communication
    to the Arduino
    """
    if can_send_command_to_arduino():
        serialInst.write(command.encode("UTF-8"))

    

def test_serial_comm():
    """Fonction to test serial communication without socket"""
    while True:
        command = input("Your Arduino command: ")

        if command == "q":
            exit()

        write_command_to_arduino(command)