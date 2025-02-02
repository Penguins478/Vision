package org.firstinspires.ftc.teamcode.FinalOpModes.FinalAutos;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.SkystoneDetector2;
import org.firstinspires.ftc.teamcode.StoneDetector;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

@Autonomous(name = "RedAlliance2Skystones", group = "Autonomous")
//@Disabled
public class RedAlliance2Skystones extends LinearOpMode {

    private SkystoneDetector2 skystoneDetector = new SkystoneDetector2();
    private StoneDetector detector = new StoneDetector();
    private OpenCvCamera phoneCam;

    String pattern = "N/A";

    private Servo servo;

    private DcMotor tilt_motor;
    private DcMotor slide_motor;

    private static final int MAX = 1225;
    private static final int MIN = 25;

    private DcMotor tl_motor;
    private DcMotor tr_motor;
    private DcMotor bl_motor;
    private DcMotor br_motor;

    private static final double COUNTS_PER_MOTOR_REV = 1120;
    private static final double GEAR_RATIO = 2;
    private static final double WHEEL_DIAMETER_INCHES = 2.9527559055;
    private static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV) / (WHEEL_DIAMETER_INCHES * Math.PI * GEAR_RATIO);

    private BNO055IMU imu;
    private BNO055IMU.Parameters params;
    private double angle;

    private static final double ANGLE_ERROR = 5;

    ElapsedTime runtime = new ElapsedTime();

    public void runOpMode(){

        imu = hardwareMap.get(BNO055IMU.class, "imu");

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        phoneCam = new OpenCvInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        phoneCam.openCameraDevice();
        phoneCam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
        phoneCam.setPipeline(skystoneDetector);

        detector.useDefaults();

        servo = hardwareMap.servo.get("stone_servo");

        tl_motor = hardwareMap.dcMotor.get("tl_motor");
        tr_motor = hardwareMap.dcMotor.get("tr_motor");
        bl_motor = hardwareMap.dcMotor.get("bl_motor");
        br_motor = hardwareMap.dcMotor.get("br_motor");

        tl_motor.setDirection(DcMotorSimple.Direction.REVERSE);
        tr_motor.setDirection(DcMotorSimple.Direction.FORWARD);
        bl_motor.setDirection(DcMotorSimple.Direction.REVERSE);
        br_motor.setDirection(DcMotorSimple.Direction.FORWARD);

        tl_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        tr_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br_motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        tl_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        tr_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        bl_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        br_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        tilt_motor = hardwareMap.dcMotor.get("tilt_motor");
        slide_motor = hardwareMap.dcMotor.get("slide_motor");

        tilt_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slide_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        tilt_motor.setDirection(DcMotorSimple.Direction.FORWARD);
        slide_motor.setDirection(DcMotorSimple.Direction.REVERSE);

        tilt_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slide_motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        runtime.reset();

        waitForStart();

        while(opModeIsActive()){

            runtime.reset();

            while(runtime.milliseconds() <= 1500) {         // 1.5 second
                if (skystoneDetector.isFound()) {
                    if (skystoneDetector.getScreenPosition().x < 80) {
                        pattern = "left";
                    } else if (80 <= skystoneDetector.getScreenPosition().x && skystoneDetector.getScreenPosition().x < 160) {
                        pattern = "middle";
                    } else {
                        pattern = "right";
                    }
                }

                telemetry.addData("X-Value: ", skystoneDetector.getScreenPosition().x);
                telemetry.addData("Pattern: ", pattern);
                telemetry.update();
            }

            runtime.reset();



            //sleep(100);

            //drives straight for 24 inches
            encoderDrive(24 + 8, 'y', 1, 50, 50);


            if(pattern.equals("left")){
                //drive left 8 inches
                encoderDrive(-8, 'x', 1, 50, 50);
                //use mech
                encoderDrive(-3, 'y', 1, 50, 50);
                //
                encoderDrive(56, 'x', 1, 50, 50);
            }else if (pattern.equals("middle")){
                // just use mech
                encoderDrive(-3, 'y', 1, 50, 50);
                encoderDrive(48, 'x', 1, 50, 50);
            }else{  // right but need for N/A
                encoderDrive(8, 'x', 1, 50, 50);
                // use mech
                encoderDrive(-3, 'y', 1, 50, 50);
                encoderDrive(40, 'x', 1, 50, 50);
            }

            encoderDrive(-24, 'x', 1, 50, 50);

            encoderDrive(-24-8+3+3, 'y', 1, 50, 50);    // added a +3

            encoderDrive(-48, 'x', 1, 50, 50); // added an extra -0 to compensate


            encoderDrive(24 + 8, 'y', 1, 50, 50);


            if (pattern.equals("left") || pattern.equals("middle")){
                // just use mech
                encoderDrive(-3, 'y', 1, 50, 50);
                encoderDrive(72, 'x', 1, 50, 50);
            }else{  // right but need for N/A
                encoderDrive(8, 'x', 1, 50, 50);
                // use mech
                encoderDrive(-3, 'y', 1, 50, 50);
                encoderDrive(64, 'x', 1, 50, 50);
            }

            encoderDrive(-12, 'x', 1, 50, 50);

            tl_motor.setPower(0);
            tr_motor.setPower(0);
            bl_motor.setPower(0);
            br_motor.setPower(0);

            break;
        }
    }

    private void encoderDrive(double inches, char direction, double power, double error, long timeout) {
        double dist = inches * COUNTS_PER_INCH;

        double tl_dist = 0;
        double tr_dist = 0;
        double bl_dist = 0;
        double br_dist = 0;

        boolean there_tl = false;
        boolean there_tr = false;
        boolean there_bl = false;
        boolean there_br = false;

        double start_tl = tl_motor.getCurrentPosition();
        double start_tr = tr_motor.getCurrentPosition();
        double start_bl = bl_motor.getCurrentPosition();
        double start_br = br_motor.getCurrentPosition();

        if (direction == 'y') {
            tl_dist = dist;
            tr_dist = dist;
            bl_dist = dist;
            br_dist = dist;
        } else if (direction == 'x') {
            tl_dist = dist;
            tr_dist = -dist;
            bl_dist = -dist;
            br_dist = dist;
        }

        while (!(there_tl && there_tr && there_bl && there_br)) {
            angle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
            there_tl = adjust_motor(tl_motor, tl_dist, start_tl, power, error);
            there_tr = adjust_motor(tr_motor, tr_dist, start_tr, power, error);
            there_bl = adjust_motor(bl_motor, bl_dist, start_bl, power, error);
            there_br = adjust_motor(br_motor, br_dist, start_br, power, error);

            while (Math.abs(angle) > ANGLE_ERROR) {
                double adjustment = (Math.abs(angle) - ANGLE_ERROR) / 45;
                if (adjustment < 0.1) {
                    adjustment = 0.1;
                }
                if (angle > 0) {
                    tl_motor.setPower(adjustment);
                    tr_motor.setPower(-adjustment);
                    bl_motor.setPower(adjustment);
                    br_motor.setPower(-adjustment);
                } else {
                    tl_motor.setPower(-adjustment);
                    tr_motor.setPower(adjustment);
                    bl_motor.setPower(-adjustment);
                    br_motor.setPower(adjustment);
                }
                angle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;

                telemetry.addData("angle", angle);
                telemetry.update();
            }

            telemetry.addData("angle", angle);
            telemetry.addData("tl position", tl_motor.getCurrentPosition());
            telemetry.addData("tl goal", start_tl + tl_dist);
            telemetry.addData("tr position", tr_motor.getCurrentPosition());
            telemetry.addData("tr goal", start_tr + tr_dist);
            telemetry.addData("bl position", bl_motor.getCurrentPosition());
            telemetry.addData("bl goal", start_bl + bl_dist);
            telemetry.addData("br position", br_motor.getCurrentPosition());
            telemetry.addData("br goal", start_br + br_dist);

            telemetry.update();
        }

        sleep(timeout);
    }

    private boolean adjust_motor(DcMotor motor, double distance, double start, double power, double error) {
        double coeff = (start + distance - motor.getCurrentPosition()) / distance;
        coeff += ((1 - coeff) / 2);
        if (coeff < 0.5) {
            coeff = 0.5;
        }
        if (motor.getCurrentPosition() < start + distance - error) {
            motor.setPower(coeff * power);
            return false;
        } else if (motor.getCurrentPosition() > start + distance + error) {
            motor.setPower(-coeff * power);
            return false;
        } else {
            motor.setPower(0);
            return true;
        }
    }
}
