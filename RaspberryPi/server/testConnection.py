import socket
import sys
import time

REMOTE_SERVER = "www.google.com"
def is_connected(hostname):
  try:
    # see if we can resolve the host name -- tells us if there is
    # a DNS listening
    host = socket.gethostbyname(hostname)
    # connect to the host -- tells us if the host is actually
    # reachable
    s = socket.create_connection((host, 80), 2)
    return True
  except:
     pass
  return False
#%timeit is_connected(REMOTE_SERVER)

while(is_connected(REMOTE_SERVER)):
  time.sleep(1)
  print("Connected to internet")
print ("Connection lost") 
sys.exit(0)

