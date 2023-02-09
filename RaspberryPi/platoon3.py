import numpy as np
import cv2 as cv
from picamera.array import PiRGBArray
from picamera import PiCamera
import time


camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 32
camera.rotation = 180
camera.brightness = 60
rawCapture = PiRGBArray(camera, size=(640, 480))
done = False

# allow the camera to warmup
time.sleep(0.1)

#frameInit = camera.capture_continuous(rawCapture, format="bgr", use_video_port=True)
#frameImg = frameInit.array
#r,h,c,w = 250,90,400,125  # simply hardcoded the values
#track_window = (c,r,w,h)
# set up the ROI for tracking
#roi = frameImg[r:r+h, c:c+w]
#hsv_roi =  cv.cvtColor(roi, cv.COLOR_BGR2HSV)
#mask = cv.inRange(hsv_roi, np.array((0., 60.,32.)), np.array((180.,255.,255.)))
#roi_hist = cv.calcHist([hsv_roi],[0],mask,[180],[0,180])
#cv.normalize(roi_hist,roi_hist,0,255,cv.NORM_MINMAX)
# capture frames from the camera
term_crit = None
r,h,c,w = None, None, None, None
track_window = None
roi = None
hsv_roi = None
mask = None
green1 = np.uint8([[[0, 0, 0]]])
green2 = np.uint8([[[50, 255, 50]]])
roi_hist = None
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    img = frame.array
    if done == False:
        term_crit = (cv.TERM_CRITERIA_EPS | cv.TERM_CRITERIA_COUNT, 10, 1)
        #r,h,c,w = 250,90,400,125  # simply hardcoded the values
        r,h,c,w = cv.selectROI(img)#(c,r,w,h)
        track_window = (int(c),int(r),int(w),int(h))
        # set up the ROI for tracking
        roi = img[int(r):int(r+h), int(c):int(c+w)]
        hsv_roi =  cv.cvtColor(roi, cv.COLOR_BGR2HSV)
        mask = cv.inRange(hsv_roi, cv.cvtColor(green1, cv.COLOR_BGR2HSV), cv.cvtColor(green2, cv.COLOR_BGR2HSV))   #cv.inRange(hsv_roi, np.array((0., 60.,32.)), np.array((180.,255.,255.)))
        roi_hist = cv.calcHist([hsv_roi],[0],mask,[180],[0,180])
        cv.normalize(roi_hist,roi_hist,0,255,cv.NORM_MINMAX)
        done = True
    
    hsv = cv.cvtColor(img, cv.COLOR_BGR2HSV)
    dst = cv.calcBackProject([hsv],[0],roi_hist,[0,180],1)
    # apply meanshift to get the new location
    ret, track_window = cv.CamShift(dst, track_window, term_crit)
    # Draw it on image
    cv.imshow('test',dst)
    pts = cv.boxPoints(ret)
    pts = np.int0(pts)
    img2 = cv.polylines(img,[pts],True, 255,2)
    cv.imshow('img2',img2)
    rawCapture.truncate(0)
    key = cv.waitKey(1) & 0xFF
    if key == ord("q"):
        break
