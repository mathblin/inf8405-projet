# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2 
import numpy as np


# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 32
camera.rotation = 180
rawCapture = PiRGBArray(camera, size=(640, 480))

# allow the camera to warmup
time.sleep(0.1)

# capture frames from the camera
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    # grab the raw NumPy array representing the image, then initialize the timestamp
    # and occupied/unoccupied text
    image = frame.array
    image =cv2.medianBlur(image,1)
##    output=image.copy()
    cimg = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
    circles = cv2.HoughCircles(cimg,cv2.CV_HOUGH_GARDIENT, 1, 20,param1=10,param2=1,minRadius=0, maxRadius=0)
    if circles.all() != None:
        #print("NoneType")
        circles = np.uint16(np.around(circles.astype(np.double),3))
        
##        for (x,y,r) in circles
##            cv2.circle(output, (x,y),r,(0,255,0),4)
##            cv2.rectangle(output, (x-5, y-5), (x+5,y+5),(0,)

        for i in circles[0,:]:
            # draw the outer circle
            cv2.circle(cimg,(i[0],i[1]),i[2],(0,255,0),2)
            # draw the center of the circle
            cv2.circle(cimg,(i[0],i[1]),2,(0,0,255),3) 

    # show the frame
    cv2.imshow("Frame", cimg)
    key = cv2.waitKey(1) & 0xFF

    # clear the stream in preparation for the next frame
    rawCapture.truncate(0)

    # if the `q` key was pressed, break from the loop
    if key == ord("q"):
        break
