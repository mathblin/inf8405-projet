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

    private boolean once = true;
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

    public double[] calculate3DVectorBetween(double[] linear_acceleration, double[] posXYZ) {
        // Project linear_acceleration and posXYZ onto the XY plane
        double[] linear_acceleration_3D = {linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]};
        double[] posXYZ_3D = {posXYZ[0], posXYZ[1], posXYZ[2]};

        // Calculate the difference vector between the projected 2D vectors
        double[] differenceVector3D = new double[3];
        differenceVector3D[0] = linear_acceleration_3D[0] - posXYZ_3D[0];
        differenceVector3D[1] = linear_acceleration_3D[1] - posXYZ_3D[1];
        differenceVector3D[2] = linear_acceleration_3D[2] - posXYZ_3D[2];

        return differenceVector3D;
    }

    // Calculate the power using the accelerometer position.
    private int getPower(double[] linear_acceleration, double[] posXYZ){
        //TODO: calculate power.
        // double power_scale = calculateUnitVectorBetween(linear_acceleration,posXYZ);
        //int power = Math.floor(power_scale*MAX_SPEED);
        return 0;
    }
    public double[] mapAngleToJoystick(double angleX, double angleY, double maxAngle) {
        // Normalize the angle values to the range of [-1, 1]
        double normalizedX = angleX / maxAngle;
        double normalizedY = angleY / maxAngle;

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

    public double[] getRotationAngles(double[] linear_acceleration, double[] posXYZ) {
        // Calculate the relative acceleration vector
        double ax = linear_acceleration[0] - posXYZ[0];
        double ay = linear_acceleration[1] - posXYZ[1];
        double az = linear_acceleration[2] - posXYZ[2];

        // Normalize the relative acceleration vector
        double magnitude = Math.sqrt(ax * ax + ay * ay + az * az);
        ax /= magnitude;
        ay /= magnitude;
        az /= magnitude;

        // Calculate pitch (rotation around the x-axis)
        double pitch = Math.atan2(-ax, Math.sqrt(ay * ay + az * az));
        pitch = Math.toDegrees(pitch);

        // Calculate roll (rotation around the y-axis)
        double roll = Math.atan2(ay, Math.sqrt(ax * ax + ( az * az + ay*ay)));
        roll = Math.toDegrees(roll);


        double[] rotationAngles = {pitch, roll};
        return rotationAngles;
    }
    double initialPitch;
    double initialRoll;

    private void handleMovements(double[] linear_acceleration, double[] posXYZ) {

        if(once) {
            double[] initialRotationAngles = getRotationAngles(linear_acceleration, posXYZ);
            initialPitch = initialRotationAngles[0];
            initialRoll = initialRotationAngles[1];
            once = false;
        }

        // TODO: la tablette est a l'envers
        double[] rotations = getRotationAngles(linear_acceleration,posXYZ);
        Log.d("rotations: ", "pitch: "+String.valueOf(rotations[0])+" roll: "+String.valueOf(rotations[1]));
        double diff_pitch = rotations[0]-initialPitch;
        double diff_roll = rotations[1];

        double[] diff_rot = {diff_pitch, diff_roll};

        Log.d("diff_rot: ", "pitch: "+String.valueOf(diff_rot[0])+" roll: "+String.valueOf(diff_rot[1]));
        if(Math.abs(diff_rot[0]) < 20 && Math.abs(diff_rot[1]) < 20 ){
            sendMessageAndLog("0,0,0;","sent 0,0,0;");
            return;
        }
        // angle pitch-roll
        int angle  = (int) Math.floor(calculate2DAngle(diff_rot[1],diff_rot[0]));
        Log.d("angle: ", String.valueOf(angle));

        double power = 50;
        //Log.d("power: ", String.valueOf(power));

        // TODO: Send command (0, angle, power)
        sendMessageAndLog("0,"+String.valueOf(angle)+","+String.valueOf(power)+";",
                "sent : "+"0,"+String.valueOf(angle)+","+String.valueOf(power)+";");
    }

    public double[] normalizeVector(double[] vector) {
        double magnitude = calculateVectorMagnitude(vector);
        double[] normalizedVector = new double[vector.length];

        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = vector[i] / magnitude;
        }

        return normalizedVector;
    }
    public double calculateVectorMagnitude(double[] vector) {
        double sum = 0;

        // Sum the square of each component of the vector
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }

        // Calculate the square root of the sum to find the magnitude
        double magnitude = Math.sqrt(sum);

        return magnitude;
    }
    private int getAngle(double[] linear_acceleration, double[] posXYZ) {
        double[] vector = calculate3DVectorBetween(linear_acceleration,posXYZ);
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

