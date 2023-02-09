import numpy as np
import argparse
import cv2
import time
import serial
import threading
import math
from enum import Enum
from Import.sonar import SonarThread
# initialize the current frame of the video, along with the list of
# ROI points along with whether or not this is input mode
frame = None
roiPts = [(189,101), (192, 298), (411, 302), (408, 105)]
inputMode = False
isInit = []
isInit[0] = False
width = 640
height = 480
currentState = 2
serial = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q21Z-if00-port0')
counterW = 5


class turningState():
	TS0 = 0
	TS1 = 1
	TS2 = 2
	TS3 = 3
	TS4 = 4

turningState = turningState()

def init(frame):
	global roiHist, roiBox, roiPts, isInit[0]
	orig = frame.copy()

	# determine the top-left and bottom-right points
	roiPts = np.array(roiPts)
	s = roiPts.sum(axis = 1)
	tl = roiPts[np.argmin(s)]
	br = roiPts[np.argmax(s)]

	# grab the ROI for the bounding box and convert it
	# to the HSV color space
	roi = orig[tl[1]:br[1], tl[0]:br[0]]
	roi = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
	#roi = cv2.cvtColor(roi, cv2.COLOR_BGR2LAB)

	# compute a HSV histogram for the ROI and store the
	# bounding box
	roiHist = cv2.calcHist([roi], [0], None, [16], [0, 180])
	roiHist = cv2.normalize(roiHist, roiHist, 0, 255, cv2.NORM_MINMAX)
	roiBox = (tl[0], tl[1], br[0], br[1])
	isInit[0] = True


def main():
	# construct the argument parse and parse the arguments
	ap = argparse.ArgumentParser()
	ap.add_argument("-v", "--video",
		help = "path to the (optional) video file")
	args = vars(ap.parse_args())

	# grab the reference to the current frame, list of ROI
	# points and whether or not it is ROI selection mode
	global frame, roiPts, inputMode, roiBox, isInit[0]
	
	camera = cv2.VideoCapture(0)

	# setup the mouse callback
	cv2.namedWindow("frame")
	
	# initialize the termination criteria for cam shift, indicating
	# a maximum of ten iterations or movement by a least one pixel
	# along with the bounding box of the ROI
	termination = (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 10, 1)
	#counter = 0
	STOP_DISTANCE = 20.0
	DistanceList = []
	conList = []
	conList.append('ok')
	sonar = SonarThread(serial, conList, DistanceList, None, isInit)
	sonar.setDaemon(True)	
	
	while True:
		if isInit[0]:
			# grab the current frame
			(grabbed, readFrame) = camera.read()
			frame = cv2.flip(readFrame, -1)
			# check to see if we have reached the end of the
			# video
			if not grabbed:
				break

			# if the see if the ROI has been computed
			if roiBox is not None:
				# convert the current frame to the HSV color space
				# and perform mean shift
				hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
				backProj = cv2.calcBackProject([hsv], [0], roiHist, [0, 180], 1)

				# apply cam shift to the back projection, convert the
				# points to a bounding box, and then draw them
				(r, roiBox) = cv2.CamShift(backProj, roiBox, termination)
				pts = np.int0(cv2.boxPoints(r))
				if DistanceList and DistanceList[0] <= STOP_DISTANCE:
					serial.write('x'.encode())
					#print("Distance: " + str(DistanceList[0]))
					continue
				moveRobot(pts)
				
				cv2.polylines(frame, [pts], True, (0, 255, 0), 2)

			# show the frame and record if the user presses a key
			cv2.imshow("frame",frame)
			key = cv2.waitKey(1) & 0xFF

			if key == ord("q"):
				serial.write('x'.encode())
				break
		else:
			(grabbed, readFrame) = camera.read()
			frameInit = cv2.flip(readFrame, -1)
			init(frameInit)
			sonar.start()

	camera.release()
	cv2.destroyAllWindows()
	sonar.stop()
	sonar.join()

def getMinMax(pts):
	ptMax = pts[0]
	ptMin = pts[0]
	for pt in pts:
		if pt[0] < ptMin[0] and pt[1] < ptMin[0] :
			ptMin = pt
		if pt[0] > ptMax[0] and pt[1] > ptMax[1]:
			ptMax = pt
	return ptMin, ptMax

def moveRobot(pts):
	global width, height, turningState, currentState, counterW
	ptMin, ptMax = getMinMax(pts)
	#print("ptMin")
	#print(ptMin)
	#print("ptMax")
	#print(ptMax)
	incX = int((ptMax[0] - ptMin[0])/2)
	incY = int((ptMax[1] - ptMin[1])/2)
	ptMilieu = [ptMin[0] + incX, ptMin[1] + incY]

	if currentState == turningState.TS0:
		if ptMilieu[0] > 128:
			currentState = turningState.TS1
		serial.write("Q".encode())
	elif currentState == turningState.TS1:
		if ptMilieu[0] < 128:
			currentState = turningState.TS0
		elif ptMilieu[0] > 256:
			currentState = turningState.TS2
		serial.write("Q".encode())
	elif currentState == turningState.TS2:
		if ptMilieu[0] < 256:
			currentState = turningState.TS1
		elif ptMilieu[0] > 384:
			currentState = turningState.TS3
		if (int(math.sqrt(((ptMax[0] - ptMin[0]) ** 2) + ((ptMax[1] - ptMin[1]) ** 2))) < 250 and int(math.sqrt(((ptMax[0] - ptMin[0]) ** 2) + ((ptMax[1] - ptMin[1]) ** 2))) > 50):
			if counterW >= 5:
				serial.write("W".encode())
				counterW = 0
			else:
				 counterW = counterW + 1
		else:
			serial.write("x".encode())
	elif currentState == turningState.TS3:
		if ptMilieu[0] < 384:
			currentState = turningState.TS2
		elif ptMilieu[0] > 512:
			currentState = turningState.TS4
		serial.write("E".encode())
	elif currentState == turningState.TS4:
		if ptMilieu[0] < 512:
			currentState = turningState.TS3
		serial.write("E".encode())
	#if(ptMilieu[0] < int(width/2) - 20):
	#	print("q")
	#	serial.write("Q".encode())
	#elif(ptMilieu[0] > int(width/2) + 20):
	#	print("e")
	#	serial.write("E".encode())
	
	
	#string = 'w'
	#serial.write(string.encode())

if __name__ == "__main__":
	main()
