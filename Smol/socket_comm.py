import sys
import socket
DEBUGGER_MODE = None
server, conn, addr = (None, None, None)
HOST, PORT = (None, None)
SOCKET_TIMEOUT = 15.0
from serial_comm import write_command_to_arduino_with_response

def init_socket(inHOST, inPORT, DEBUG_MODE=None):
    """Initializing the socket with Android App"""

    global HOST
    global PORT
    global DEBUGGER_MODE
    global server
    global conn
    global addr
    HOST, PORT = inHOST, inPORT
    DEBUGGER_MODE = DEBUG_MODE
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Next line is to avoit the TIME_WAIT state of connected sockets. 
    # Even if we close our socket, it can cause lingering consequences for some time (even minutes). 
    # Why: A socket flag can be set to disable the behavior. Flag: SO_REUSEADDR
    # Inspired by Jean-Paul Calderone: https://stackoverflow.com/questions/5040491/python-socket-doesnt-close-connection-properly
    # More reasons: http://www.unixguide.net/network/socketfaq/4.5.shtml
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # Other options:
    #socket.socket: must use to create a socket.
    #socket.AF_INET: Address Format, Internet = IP Addresses.
    #socket.SOCK_STREAM: two-way, connection-based byte streams.
    print ("socket created")

    # Bind socket to Host and Port
    try:
        server.bind((HOST, PORT))
    except socket.error as err:
    #  print ('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1]
        sys.exit()

    print ('Socket Bind Success!')

    #listen(): This method sets up and start TCP listener.
    server.listen(10)
    print ('Socket is now listening')

    conn, addr = server.accept()
    print ('Connect with ' + addr[0] + ':' + str(addr[1]))

    # sys.exit(help(conn.timeout))
    conn.settimeout(SOCKET_TIMEOUT)

def close_socket():
    """Closing the socket with Android App"""
    conn.close()
    server.close()

def manage_socket_exception(command):
    """Managing exception from sockets"""

    write_command_to_arduino_with_response(command)
    close_socket()
    if command == 'exceded timout\n':
        print(command)
    elif command == '':
        print('exceded timout, empty command\n')
    else:
        print('exceded timout\n')
    
    if DEBUGGER_MODE:
        sys.exit('Exited while being in debugger mode')
    else:
        init_socket(HOST, PORT) # QoS

def parse_command(recvCommand):
    newCommand = ''

    # if DEBUGGER_MODE: print('recvCommand: ', recvCommand)

    for rcvcom in reversed(recvCommand.split(';')):
        if rcvcom == '':
            pass
        else:
            newCommand = rcvcom
            if newCommand != '':
                break
    
    return newCommand

def get_comman_from_socket():
    """Fonction to get commands from the Android application"""

    try:
        recvCommand = conn.recv(1064)

        recvCommand = recvCommand.decode('utf-8').lower()
        
        return parse_command(recvCommand)

    except:
        return 'exceded timout'