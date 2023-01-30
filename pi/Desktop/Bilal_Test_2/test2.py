import cv2
import time
import socket
import base64
import numpy as np
from threading import Thread
from picamera.array import PiRGBArray
from picamera import PiCamera

SERVER_IP = "0.0.0.0"
SERVER_PORT = 5000
MAX_NUM_CONNECTIONS = 20
DEVICE_NUMBER = 0

class ConnectionPool(Thread):

    def __init__(self, ip_, port_, conn_, device_):
        Thread.__init__(self)
        self.ip = ip_
        self.port = port_
        self.conn = conn_
        self.device = device_
        print("[+] New server socket thread started for " + self.ip + ":" +str(self.port))

    def run(self):
        try:
            while True:
                ret, frame = self.device.read()
                a = b'\r\n'
                data = frame.tostring()
                da = base64.b64encode(data)
                self.conn.sendall(da + a)

        except Exception as e:
            print("Connection lost with " + self.ip + ":" + str(self.port) +"\r\n[Error] " + str(e.message))
        self.conn.close()

if __name__ == '__main__':
    #cap = cv2.VideoCapture(DEVICE_NUMBER)
    camera = PiCamera()
    camera.resolution = (320, 288)
    camera.framerate = 25
    rawCapture = PiRGBArray(camera, size=(320, 288))
    for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
        # grab the raw NumPy array representing the image, then initialize the timestamp
        # and occupied/unoccupied text
        image = frame.array
        camera.rotation = 180
        # show the frame
        cv2.imshow("Frame", image)
        key = cv2.waitKey(1) & 0xFF
        
        # clear the stream in preparation for the next frame
        rawCapture.truncate(0)
        
        # if the `q` key was pressed, break from the loop
        if key == ord("q"):
            break
        print("Waiting connections...")
        connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        connection.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        connection.bind((SERVER_IP, SERVER_PORT))
        connection.listen(MAX_NUM_CONNECTIONS)
        while True:
            (conn, (ip, port)) = connection.accept()
            thread = ConnectionPool(ip, port, conn, frame)
            thread.start()
        connection.close()
