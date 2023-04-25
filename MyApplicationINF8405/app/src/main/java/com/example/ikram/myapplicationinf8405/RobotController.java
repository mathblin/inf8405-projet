package com.example.ikram.myapplicationinf8405;

import android.util.Log;

public class RobotController {
    private static final double TURNING_THRESHOLD = 1.0;
    private static final int MAX_SPEED = 100;
    private double MAX_SEND_VALUE = 8.0;
    private double lastValueEQ = 0;
    private double lastValueWS = 0;
    private VideoActivity.ClientThread clientThread;
    private boolean use_joystick_command = true;
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

    public double[] calculate2DVectorBetween(double[] linear_acceleration, double[] posXYZ) {
        // Project linear_acceleration and posXYZ onto the XY plane
        double[] linear_acceleration_2D = {linear_acceleration[0], linear_acceleration[1]};
        double[] posXYZ_2D = {posXYZ[0], posXYZ[1]};

        // Calculate the difference vector between the projected 2D vectors
        double[] differenceVector2D = new double[2];
        differenceVector2D[0] = linear_acceleration_2D[0] - posXYZ_2D[0];
        differenceVector2D[1] = linear_acceleration_2D[1] - posXYZ_2D[1];

        return differenceVector2D;
    }

    // Calculate the power using the accelerometer position.
    private int getPower(double[] linear_acceleration, double[] posXYZ){
        //TODO: calculate power.
        // double power_scale = calculateUnitVectorBetween(linear_acceleration,posXYZ);
        //int power = Math.floor(power_scale*MAX_SPEED);
        return 0;
    }

    public double[] mapToJoystick(double x, double y, double maxValue) {
        // Normalize the x and y values to the range of [-1, 1]
        double normalizedX = x / maxValue;
        double normalizedY = y / maxValue;

        // Clamp the values to the range of [-1, 1]
        normalizedX = Math.max(-1, Math.min(1, normalizedX));
        normalizedY = Math.max(-1, Math.min(1, normalizedY));

        // Create an array to store the mapped x and y values
        double[] joystickValues = {normalizedX, normalizedY};

        return joystickValues;
    }
    public double calculate2DAngle(double x, double y) {
        // Calculate the angle in radians using the atan2 function
        double angleRadians = Math.atan2(y, x);

        // Convert the angle to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        // Normalize the angle to the range [0, 360)
        angleDegrees = (angleDegrees + 360) % 360;

        return angleDegrees;
    }
    private void handleMovements(double[] linear_acceleration, double[] posXYZ) {

        int angle = getAngle(linear_acceleration,posXYZ);
        double[] vector = calculate2DVectorBetween(linear_acceleration,posXYZ);

        // TODO : get XZ vector and XY vector using linear_acceleration and posXYZ.
        // TODO : change the vector to use the angles
        double[] joystick_val = mapToJoystick(vector[0],vector[1],4);

        // TODO : use x : angle XZ , y : angle XY.
        double angl = calculate2DAngle(joystick_val[0],joystick_val[1]);

        //double angle_d = calculateAngle(linear_acceleration,posXYZ);
        //Log.d("angle accelerometer: ", String.valueOf(Math.floor(angle)));
        //Log.d("vector :", vector[0] + "," + vector[1]);
        //Log.d("Joystick : ", joystick_val[0]+","+joystick_val[1]);
        //Log.d("Joystick angle : ", angl +" degres");

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

    private int getAngle(double[] linear_acceleration, double[] posXYZ) {
        double[] vector = calculate2DVectorBetween(linear_acceleration,posXYZ);
        // Calculate the angle in radians using the atan2 function
        double angleRadians = Math.atan2(vector[1], vector[0]);

        // Convert the angle to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        // Normalize the angle to the range [0, 360)
        int angleDegree = (int) ((Math.floor(angleDegrees) + 360) % 360);

        return angleDegree;
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

