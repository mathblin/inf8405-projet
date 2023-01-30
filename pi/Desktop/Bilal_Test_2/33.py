# -*- coding: utf-8 -*-

#from picamera.array import PiRGBArray
#from picamera import PiCamera
#import time
import _thread
import io
import picamera
import logging
import socketserver
from threading import Condition
from http import server
import cv2
import numpy as np



# initialize the camera and grab a reference to the raw camera capture
class StreamingOutput(object):
    def __init__(self):
        self.frame = None
        self.buffer = io.BytesIO()
        self.condition = Condition()
        self.cvStream = None
    def write(self, buf):
        if buf.startswith(b'\xff\xd8'):
            # New frame, copy the existing buffer's content and notify all
            # clients it's available
            self.buffer.truncate()
            with self.condition:
                #buff = self.buffer
                #self.array = np.frombuffer(buff)
                #self.frame = self.buffer.getvalue()
                self.frame = self.buffer.getvalue()
                self.cvStream = buf
                self.condition.notify_all()
            self.buffer.seek(0)
        return self.buffer.write(buf)
    
    
output = StreamingOutput()

def captureCv():
    while True:
        with output.condition:
            output.condition.wait()
            print(output.cvStream)
            #fr = np.frombuffer(output.cvStream, dtype="int32")
            #arr = np.asarray(frame)
        # grab the raw NumPy array representing the image, then initialize the timestamp
        # and occupied/unoccupied text
            #image = frame.array
        #cameraCv.rotation = 180
        # show the frame
            #cv2.imshow("Frame", fr)
        #key = cv2.waitKey(1) & 0xFF
    
        # clear the stream in preparation for the next frame
        #rawCapture.truncate(0)

# Web streaming example
# Source code from the official PiCamera package
# http://picamera.readthedocs.io/en/latest/recipes2.html#web-streaming

PAGE="""\
<html>
<body bgcolor="#000000">
<img src="stream.mjpg" width="640" height="480">
</body>
</html>
"""



class StreamingHandler(server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/':
            self.send_response(301)
            self.send_header('Location', '/index.html')
            self.end_headers()
        elif self.path == '/index.html':
            content = PAGE.encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'text/html')
            self.send_header('Content-Length', len(content))
            self.end_headers()
            self.wfile.write(content)
        elif self.path == '/stream.mjpg':
            self.send_response(200)
            self.send_header('Age', 0)
            self.send_header('Cache-Control', 'no-cache, private')
            self.send_header('Pragma', 'no-cache')
            self.send_header('Content-Type', 'multipart/x-mixed-replace; boundary=FRAME')
            self.end_headers()
            try:
                while True:
                    with output.condition:
                        output.condition.wait()
                        frame = output.frame
                    self.wfile.write(b'--FRAME\r\n')
                    self.send_header('Content-Type', 'image/jpeg')
                    self.send_header('Content-Length', len(frame))
                    self.end_headers()
                    self.wfile.write(frame)
                    self.wfile.write(b'\r\n')
            except Exception as e:
                logging.warning(
                    'Removed streaming client %s: %s',
                    self.client_address, str(e))
        else:
            self.send_error(404)
            self.end_headers()

class StreamingServer(socketserver.ThreadingMixIn, server.HTTPServer):
    allow_reuse_address = True
    daemon_threads = True

with picamera.PiCamera(resolution='640x480', framerate=24) as camera:
    
    #Uncomment the next line to change your Pi's Camera rotation (in degrees)
    camera.rotation = 180
    camera.start_recording(output, format='mjpeg')
    _thread.start_new_thread(captureCv, ())
    try:
        address = ('', 8000)
        server = StreamingServer(address, StreamingHandler)
        server.serve_forever()
    finally:
        camera.stop_recording()
