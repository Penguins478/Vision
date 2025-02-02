package org.firstinspires.ftc.teamcode;

import com.disnodeteam.dogecv.detectors.skystone.SkystoneDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;


@Autonomous(name = "StoneAuto", group = "Autonomous")
//@Disabled
public class StoneAuto extends LinearOpMode {

    private DcMotor tl_motor;
    private DcMotor tr_motor;
    private DcMotor bl_motor;
    private DcMotor br_motor;

    private SkystoneIdentificationSample foria = new SkystoneIdentificationSample();
    private SkystoneDetector2 skystoneDetector = new SkystoneDetector2();
    private StoneDetector detector = new StoneDetector();
    private OpenCvCamera phoneCam;

    private static final double COUNTS_PER_MOTOR_REV = 1120;
    private static final double GEAR_RATIO = 2;
    private static final double WHEEL_DIAMETER_INCHES = 2.9527559055;
    private static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV) / (WHEEL_DIAMETER_INCHES * Math.PI * GEAR_RATIO);

    String pattern = "N/A";

    //
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private static final boolean PHONE_IS_PORTRAIT = false  ;

    private static final String VUFORIA_KEY =
            "AboXNav/////AAABmeGOS7NozU5qnTs51UZZ17g1BcHElTpLG7nsEbYufdDfWLm+ZbdqBWX7hOXxoHiu9tzNUQI4PIsRb96x9UHLizYXL8ZzqsCRKqmHx6iiGL81vuTLUyZ3yTybJ5AENkTY8h7YrKSJZnvS7W3V4EoZFxXRe4mEhxxWYsYSlBx6MEX7m9RAet8LGIf35OjCd5wzC/tSGMs+RGx+4tU+aCQkytnoaPcnEGzzt9nLu/0DypuJFc2pI+FuwGw0Y52PbDBrnb/cw+SLSRYGbYavN77to3eguAn7rG8vN0W0RrYYvkWxiRh1HY0WWtew91WaN4+hmkwWRoEFmC6CAt3kAzg1NSQWpRGXNreOlMqOf8Dr+H8j";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constant for Stone Target
    private static final float stoneZ = 2.00f * mmPerInch;

    // Constants for the center support targets
    private static final float bridgeZ = 6.42f * mmPerInch;
    private static final float bridgeY = 23 * mmPerInch;
    private static final float bridgeX = 5.18f * mmPerInch;
    private static final float bridgeRotY = 59;                                 // Units are degrees
    private static final float bridgeRotZ = 180;

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;
    private boolean targetVisible = false;
    private float phoneXRotate    = 0;
    private float phoneYRotate    = 0;
    private float phoneZRotate    = 0;
    //

    @Override
    public void runOpMode(){

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        phoneCam = new OpenCvInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        phoneCam.openCameraDevice();
        phoneCam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
        phoneCam.setPipeline(skystoneDetector);

        detector.useDefaults();

        tl_motor = hardwareMap.dcMotor.get("tl_motor");
        tr_motor = hardwareMap.dcMotor.get("tr_motor");
        bl_motor = hardwareMap.dcMotor.get("bl_motor");
        br_motor = hardwareMap.dcMotor.get("br_motor");

        tl_motor.setDirection(DcMotorSimple.Direction.FORWARD);
        bl_motor.setDirection(DcMotorSimple.Direction.FORWARD);
        tr_motor.setDirection(DcMotorSimple.Direction.REVERSE);
        br_motor.setDirection(DcMotorSimple.Direction.REVERSE);

        tr_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        tl_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        tr_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        tl_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        bl_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        br_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        ElapsedTime runtime = new ElapsedTime();
        runtime.reset();



        waitForStart();

        while(opModeIsActive()){



            if(skystoneDetector.isFound()){
                if (skystoneDetector.getScreenPosition().x < 120){
                    pattern = "left";
                } else if (80 <= skystoneDetector.getScreenPosition().x && skystoneDetector.getScreenPosition().x < 160){
                    pattern = "middle";
                }else{
                    pattern = "right";
                }
            }

            telemetry.addData("X-Value: ", skystoneDetector.getScreenPosition().x);
            telemetry.addData("Pattern: ", pattern);
            telemetry.update();

            //runtime.reset();

            /*

            while(!skystoneDetector.isFound() && runtime.milliseconds() <= 5000){ // go left
                tl_motor.setPower(-0.3);
                tr_motor.setPower(0.3);
                bl_motor.setPower(0.3);
                br_motor.setPower(-0.3);
                telemetry.addData("Time Elapsed (Milliseconds): ", runtime.milliseconds());
                telemetry.addData("Skystone? ", skystoneDetector.isFound());
                telemetry.update();
            }
            */

            //encoderDrive((30 - 1) * COUNTS_PER_INCH, (30 - 1) * COUNTS_PER_INCH, (30 - 1) * COUNTS_PER_INCH, (30 - 1) * COUNTS_PER_INCH, 0.5, 10, 250);

            //runtime.reset();


            /*
            while(!detector.isFound() || runtime.milliseconds() <= 5000){ // 5 seconds
                tl_motor.setPower(-0.5);
                tr_motor.setPower(0.5);
                bl_motor.setPower(0.5);
                br_motor.setPower(-0.5);
                telemetry.addData("Time Elapsed (Milliseconds): ", runtime.milliseconds());
                telemetry.addData("Stone? ", detector.isFound());
                telemetry.update();
            }
            */




            //telemetry.addData("Stone? ", detector.isFound());
            //telemetry.update();

        }
    }

    public void encoderDrive(double distance1, double distance2, double distance3, double distance4, double power, double error, long timeout){

        double start_tl = tl_motor.getCurrentPosition();
        double start_tr = tr_motor.getCurrentPosition();
        double start_bl = bl_motor.getCurrentPosition();
        double start_br = br_motor.getCurrentPosition();

        boolean there_tl = false;
        boolean there_tr = false;
        boolean there_bl = false;
        boolean there_br = false;

        while(!(there_tl && there_tr && there_bl && there_br)) {
            if(!there_tl) {
                if (tl_motor.getCurrentPosition() < (start_tl + distance1 - error) / 2) {
                    tl_motor.setPower(power);
                } else if (tl_motor.getCurrentPosition() >= (start_tl + distance1 - error) / 2 && tl_motor.getCurrentPosition() < start_tl + distance1 - error) {
                    tl_motor.setPower(power * 0.65);
                } else if (tl_motor.getCurrentPosition() > start_tl + distance1 + error) {
                    //tl_motor.setPower(power * -0.5);
                    tl_motor.setPower(-power);
                } else {
                    there_tl = true;
                    tl_motor.setPower(0);
                }
            }
            if(!there_tr) {
                if (tr_motor.getCurrentPosition() < (start_tr + distance2 - error) / 2) {
                    tr_motor.setPower(power);
                } else if (tr_motor.getCurrentPosition() >= (start_tr + distance2 - error) / 2 && tr_motor.getCurrentPosition() < start_tr + distance2 - error) {
                    tr_motor.setPower(power * 0.65);
                } else if (tr_motor.getCurrentPosition() > start_tr + distance2 + error) {
                    //tr_motor.setPower(power * -0.5);
                    tr_motor.setPower(-power);
                } else {
                    there_tr = true;
                    tr_motor.setPower(0);
                }
            }
            if(!there_bl) {
                if (bl_motor.getCurrentPosition() < (start_bl + distance3 - error) / 2) {
                    bl_motor.setPower(power);
                } else if (bl_motor.getCurrentPosition() >= (start_bl + distance3 - error) / 2 && bl_motor.getCurrentPosition() < start_bl + distance3 - error) {
                    bl_motor.setPower(power * 0.65);
                } else if (bl_motor.getCurrentPosition() > start_bl + distance3 + error) {
                    //bl_motor.setPower(power * -0.5);
                    bl_motor.setPower(-power);
                } else {
                    there_bl = true;
                    bl_motor.setPower(0);
                }
            }
            if(!there_br) {
                if (br_motor.getCurrentPosition() < (start_br + distance4 - error) / 2) {
                    br_motor.setPower(power);
                } else if (br_motor.getCurrentPosition() >= (start_br + distance4 - error) / 2 && br_motor.getCurrentPosition() < start_br + distance4 - error) {
                    br_motor.setPower(power * 0.65);
                } else if (br_motor.getCurrentPosition() > start_br + distance4 + error) {
                    //br_motor.setPower(power * -0.5);
                    br_motor.setPower(-power);
                } else {
                    there_br = true;
                    br_motor.setPower(0);
                }
            }

            telemetry.addData("tl power", tl_motor.getPower());
            telemetry.addData("tr power", tr_motor.getPower());
            telemetry.addData("bl power", bl_motor.getPower());
            telemetry.addData("br power", br_motor.getPower());
            telemetry.addData("Encoder tl", tl_motor.getCurrentPosition());
            telemetry.addData("Encoder tr", tr_motor.getCurrentPosition());
            telemetry.addData("Encoder bl", bl_motor.getCurrentPosition());
            telemetry.addData("Encoder br", br_motor.getCurrentPosition());
            telemetry.addData("Encoder tl goal", start_tl + distance1);
            telemetry.addData("Encoder tr goal", start_tr + distance2);
            telemetry.addData("Encoder bl goal", start_bl + distance3);
            telemetry.addData("Encoder br goal", start_br + distance4);
            telemetry.update();
        }

        sleep(timeout);

    }
}