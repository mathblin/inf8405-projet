package com.example.ikram.myapplicationinf8405;

import android.util.Log;

public class RobotController {
    private static final double TURNING_THRESHOLD = 1.0;
    private static final int MAX_SPEED = 255;
    private double MAX_SEND_VALUE = 8.0;
    private double lastValueEQ = 0;
    private double lastValueWS = 0;
    private VideoActivity.ClientThread clientThread;
    private boolean use_joystick_command = false;
    public RobotController(VideoActivity.ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    public void handleRobotMovement(double[] linear_acceleration, double[] posXYZ, double topIntervalX, double bottomIntervalX) {
        double maxLinearAcceleration = Math.max(linear_acceleration[0], posXYZ[0]);

        if(use_joystick_command){
            handleMovements(linear_acceleration,posXYZ);
            return;
        }

        double stopValue;
        if (maxLinearAcceleration == linear_acceleration[0]) {
            stopValue = maxLinearAcceleration - posXYZ[0];
        } else {
            stopValue = maxLinearAcceleration - linear_acceleration[0];
        }

        if (Math.abs(linear_acceleration[1]) <= TURNING_THRESHOLD &&
            Math.abs(stopValue)<1.0) {
            sendMessageAndLog("1,x;", "x mini 333");
        }

        if (Math.abs(linear_acceleration[1]) > TURNING_THRESHOLD) {
            handleTurning(linear_acceleration, posXYZ);
        } else {
            handleForwardBackwardMovement(linear_acceleration, posXYZ, topIntervalX, bottomIntervalX);
        }


    }

    public double calculateAngle(double[] linear_acceleration, double[] posXYZ) {
        // Calculate the dot product of the two vectors
        double dotProduct = linear_acceleration[0] * posXYZ[0] + linear_acceleration[1] * posXYZ[1] + linear_acceleration[2] * posXYZ[2];

        // Calculate the magnitudes of both vectors
        double linearAccelerationMagnitude = Math.sqrt(linear_acceleration[0] * linear_acceleration[0] + linear_acceleration[1] * linear_acceleration[1] + linear_acceleration[2] * linear_acceleration[2]);
        double posXYZMagnitude = Math.sqrt(posXYZ[0] * posXYZ[0] + posXYZ[1] * posXYZ[1] + posXYZ[2] * posXYZ[2]);

        // Calculate the cosine of the angle
        double cosAngle = dotProduct / (linearAccelerationMagnitude * posXYZMagnitude);

        // Clamp the cosine value between -1 and 1 to avoid errors in the acos function
        cosAngle = Math.max(-1, Math.min(1, cosAngle));

        // Calculate the angle in radians
        double angleRadians = Math.acos(cosAngle);

        // Convert the angle to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        return angleDegrees;
    }

    // Calculate angle using the accelerometer position.
    private int getAngle(double[] linear_acceleration, double[] posXYZ){
        // Calculate x:
        double x=1;

        // Calculate y:
        double y=1;

        // Calculate the angle in radians
        double angleRadians = Math.atan2(y, x);
        // Convert the angle from radians to degrees
        double angleDegrees = Math.toDegrees(angleRadians);
        // Normalize the angle to the range [0, 360)
        if (angleDegrees < 0) {
            //angleDegrees += 360;
        }
        return (int) Math.floor(angleDegrees);
    }
    // Calculate the power using the accelerometer position.
    private int getPower(double[] linear_acceleration, double[] posXYZ){
        //TODO: calculate power.
       // double power_scale = calculateUnitVectorBetween(linear_acceleration,posXYZ);
        //int power = Math.floor(power_scale*MAX_SPEED);
        return 0;
    }

    public double[] calculateUnitVector(double[] vector) {
        double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);

        double[] unitVector = new double[3];
        unitVector[0] = vector[0] / magnitude;
        unitVector[1] = vector[1] / magnitude;
        unitVector[2] = vector[2] / magnitude;

        return unitVector;
    }

    public double[] calculateUnitVectorBetween(double[] linear_acceleration, double[] posXYZ) {
        // Calculate the difference vector between linear_acceleration and posXYZ
        double[] differenceVector = new double[3];
        differenceVector[0] = linear_acceleration[0] - posXYZ[0];
        differenceVector[1] = linear_acceleration[1] - posXYZ[1];
        differenceVector[2] = linear_acceleration[2] - posXYZ[2];

        // Calculate the unit vector of the difference vector
        double[] unitVector = calculateUnitVector(differenceVector);

        return unitVector;
    }
    private void handleMovements(double[] linear_acceleration, double[] posXYZ) {

        int angle = getAngle(linear_acceleration,posXYZ);
        double angle_d = calculateAngle(linear_acceleration,posXYZ);
        Log.d("angle accelerometer: ", String.valueOf(Math.floor(angle_d)));

        /*int power = getPower(linear_acceleration,posXYZ);

        double angleDegrees = Math.toDegrees(angle);
        if (angleDegrees <0){
            double angleFinal =  (180 + Math.abs(angleDegrees));
            angleDegrees = angleFinal;
        } else if (angleDegrees == 0)  {
            double  angleFinal = (int) 0;
            angleDegrees = angleFinal;
        } else {
            double  angleFinal = (int) (180 -angleDegrees);
            angleDegrees = angleFinal;
        }

        int pwr = (int) Math.floor(power);
        int angl = (int) Math.floor(angleDegrees);
        String command = "0," + pwr +  ","+ angl +";" ;
        sendMessageAndLog(command,"sent: "+command);*/
    }

    private void handleTurning(double[] linear_acceleration, double[] posXYZ) {
        double sendValue;
        String message;
        String logMessage;

        if (linear_acceleration[1] > posXYZ[1]) {
            sendValue = calculateSendValue(linear_acceleration[1], posXYZ[1]);

            if (sendValue >= MAX_SEND_VALUE) {
                message = "1,d;";
                logMessage = "d mini 351";
            } else {
                message = (sendValue > lastValueEQ) ? "1,E;" : "1,e;";
                logMessage = (sendValue > lastValueEQ) ? "E maj 359" : "e mini 361";
            }
        } else {
            sendValue = calculateSendValue(posXYZ[1], linear_acceleration[1]);

            if (sendValue >= MAX_SEND_VALUE) {
                message = "1,a;";
                logMessage = "a mini 374";
            } else {
                message = (sendValue > lastValueEQ) ? "1,Q;" : "1,q;";
                logMessage = (sendValue > lastValueEQ) ? "Q maj 380" : "q mini 385";
            }
        }

        lastValueEQ = sendValue;
        sendMessageAndLog(message, logMessage);
    }

    private void handleForwardBackwardMovement(double[] linear_acceleration, double[] posXYZ, double topIntervalX, double bottomIntervalX) {
        double sendValue;
        String message;
        String logMessage;

        if (linear_acceleration[0] > posXYZ[0]) {
            sendValue = calculateSendValue(linear_acceleration[0], posXYZ[0]);

            if (Math.abs(sendValue) < 1.0) {
                message = "1,x;";
                logMessage = "x mini 333";
            } else {
                message = (sendValue > lastValueWS) ? "1,S;" : "1,s;";
                logMessage = (sendValue > lastValueWS) ? "s mini 405" : "s mini 409";
            }
        } else {
            sendValue = calculateSendValue(posXYZ[0], linear_acceleration[0]);

            if (Math.abs(sendValue) < 1.0) {
                message = "1,x;";
                logMessage = "x mini 333";
            } else {
                message = (sendValue > lastValueWS) ? "1,W;" : "1,w;";
                logMessage = (sendValue > lastValueWS) ? "W maj 428" : "w mini 434";
            }
        }

        lastValueWS = sendValue;
        sendMessageAndLog(message, logMessage);
    }

    private double calculateSendValue(double value1, double value2) {
        return Math.floor(value1 - value2);
    }

    private void sendMessageAndLog(String message, String logMessage) {
        clientThread.sendMessage(message);
        Log.d("letter", logMessage);
    }
}

