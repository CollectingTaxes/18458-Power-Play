package org.firstinspires.ftc.teamcode.Auto.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Auto.pipelines.BlueWarehouseDuckDetect;
import org.firstinspires.ftc.teamcode.RoadRunner.Drive.RoadrunnerTankDrive;
import org.firstinspires.ftc.teamcode.RoadRunner.TrajectorySequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.hardware.AutoValues;
import org.firstinspires.ftc.teamcode.hardware.Hardware_18458;
import org.firstinspires.ftc.teamcode.teleops.testing.TuningStart;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@Autonomous(name="BlueWarehouse", group="Roadrunner Paths")
public class BlueWarehouse extends LinearOpMode {
    @Override
    public void runOpMode() {
        TuningStart.initializeTuning();
        RoadrunnerTankDrive drive = new RoadrunnerTankDrive(hardwareMap);
        Hardware_18458 robot = new Hardware_18458();
        AutoValues auto = new AutoValues();
        robot.init(hardwareMap);

        double armHeight = 1;

        double hubDistance = 0;

        // move camera
        robot.Arm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        BlueWarehouseDuckDetect detector = new BlueWarehouseDuckDetect();


        /* Open CV */

        // Obtain camera id to allow for camera preview
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        // Obtain webcam name
        WebcamName webcam = hardwareMap.get(WebcamName.class, "Webcam 1");

        // Initialize OpenCvWebcam
        // With live preview
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcam, cameraMonitorViewId);

        // Open the Camera Device Asynchronously
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                // Start Camera Streaming

                // NOTE: this must be called *before* you call startStreaming(...)
                camera.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED);

                // Start camera stream with 1280x720 resolution
                camera.startStreaming(1280,720, OpenCvCameraRotation.UPRIGHT);

                camera.setPipeline(detector);
                telemetry.addData("you shoudl see this", "ill be mad if you dont");
            }
            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera status", "Camera failed :(");
            }
        });
        sleep(4000);
        // camera dection print
        telemetry.addData("Duck position", detector.getAnalysis());
        telemetry.addData("hi", "hi");

        // Before start

        // close servos

        robot.g1.setPosition(auto.Servo_Closed);
        robot.g2.setPosition(auto.Servo_Closed);

        // On start

        waitForStart();
        if(isStopRequested()) return;

        if (detector.getAnalysis() == BlueWarehouseDuckDetect.DuckPosition.RIGHT) {
            armHeight = auto.Arm_High;
            hubDistance = auto.Arm_High_dis;
        }

        if (detector.getAnalysis() == BlueWarehouseDuckDetect.DuckPosition.CENTER) {
            armHeight = auto.Arm_Mid;
            hubDistance = auto.Arm_Mid_dis;
        }

        if (detector.getAnalysis() == BlueWarehouseDuckDetect.DuckPosition.LEFT) {
            armHeight = auto.Arm_Low;
            hubDistance = auto.Arm_Low_dis;

        }









        Pose2d startPose = new Pose2d(-11, 64, Math.toRadians(0));
        ElapsedTime timer = new ElapsedTime();

        drive.setPoseEstimate(startPose);

        TrajectorySequence Trajectory1 = drive.trajectorySequenceBuilder(startPose)
                .forward(hubDistance)
                .build();

        TrajectorySequence Trajectory2 = drive.trajectorySequenceBuilder(Trajectory1.end())
                .back(hubDistance - 4)
                .turn(Math.toRadians(360), 2, 2)
                .turn(Math.toRadians(270), 2, 2)
                .forward(9)
                .build();


        camera.stopStreaming();

        //lift arm
        robot.Arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.Arm.setTargetPosition((int) armHeight);
        robot.Arm.setPower(auto.Arm_Power);

        // Run trajectory 1
        drive.followTrajectorySequence((Trajectory1));

        // Drop freight
        robot.g1.setPosition(auto.Servo_Open);
        robot.g2.setPosition(auto.Servo_Open);
        sleep(1000);

        // Run trajectory 2
        drive.followTrajectorySequence((Trajectory2));

        sleep(1000);


    }
}
