import serial.tools.list_ports
import sys

serialInst = None
DEBUGGER_MODE = None

def init_serial(DEBUG_MODE = None):
    global DEBUGGER_MODE
    DEBUGGER_MODE = DEBUG_MODE
    # serialInst = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')
    global serialInst
    if sys.platform == 'win32':
        portsList = []
        ports = serial.tools.list_ports.comports()

        for onePort in ports:
            portsList.append(str(onePort))
            print(str(onePort))


        portVar = "No COM found"

        INVALID_COMM_PORT = True
        while INVALID_COMM_PORT:
            val = input("q to quit or Select Port: COM")
            if val == 'q':
                sys.exit()
            for x in range(0, len(portsList)):
                if portsList[x].startswith("COM" + str(val)):
                    portVar = "COM" + str(val)
                    print(portVar)
                    INVALID_COMM_PORT = False
        serialInst = serial.Serial(portVar)
        # serialInst = serial.Serial()
    else:
        serialInst = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')

def write_command_to_arduino(command):
    serialInst.write(command.encode("UTF-8"))

def test_serial_comm():
    while True:
        command = input("Your Arduino command: ")

        if command == "q":
            exit()

        write_command_to_arduino(command)