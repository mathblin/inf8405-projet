import sys
import socket
DEBUGGER_MODE = None
s, conn, addr = (None, None, None)
HOST, PORT = (None, None)
def init_socket(inHOST, inPORT, DEBUG_MODE=None):
    global HOST
    global PORT
    global DEBUGGER_MODE
    global s
    global conn
    global addr
    HOST, PORT = inHOST, inPORT
    DEBUGGER_MODE = DEBUG_MODE
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #socket.socket: must use to create a socket.
    #socket.AF_INET: Address Format, Internet = IP Addresses.
    #socket.SOCK_STREAM: two-way, connection-based byte streams.
    print ("socket created")

    #Bind socket to Host and Port
    try:
        s.bind((HOST, PORT))
    except socket.error as err:
    #  print ('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1]
        sys.exit()

    print ('Socket Bind Success!')


    #listen(): This method sets up and start TCP listener.
    s.listen(10)
    print ('Socket is now listening')

    conn, addr = s.accept()
    print ('Connect with ' + addr[0] + ':' + str(addr[1]))

    # sys.exit(help(conn.timeout))
    conn.settimeout(5.0)

def close_socket():
    s.close()

def manage_socket_exception(command):
    close_socket()
    if not DEBUGGER_MODE:
        init_socket(HOST, PORT) # QoS
    else:
        if command == 'exceded timout':
            sys.exit(command)
        elif command == '':
            sys.exit('exceded timout, empty command')


def get_comman_from_socket():
    try:
        recvCommand = conn.recv(64)
        
        return recvCommand.decode('utf-8')

    except:
        return 'exceded timout'