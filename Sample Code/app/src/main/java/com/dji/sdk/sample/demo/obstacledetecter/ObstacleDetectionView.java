package com.dji.sdk.sample.demo.obstacledetecter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.demo.gimbal.MoveGimbalWithSpeedView;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.view.BaseThreeBtnView;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.imu.IMUState;
import dji.common.flightcontroller.imu.SensorState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.sdkmanager.DJISDKManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 障害物検出を行うためのクラス
 *Created by maki on 18/04/17.
 */

public class ObstacleDetectionView extends BaseThreeBtnView {
    private Timer timer;
    private MoveGimbalWithSpeedView.GimbalRotateTimerTask gimbalRotationTimerTask;

    public ObstacleDetectionView(Context context) { super(context); }

    @Override
    protected int getMiddleBtnTextResourceId() { return R.string.detect_obstacle_opt_flow; }

    @Override
    protected int getLeftBtnTextResourceId() { return R.string.detect_obstacle_no_filter; }

    @Override
    protected int getRightBtnTextResourceId() { return R.string.detect_obstacle_masking; }

    @Override
    protected int getDescriptionResourceId() { return R.string.detect_obstacle_description; }

    @Override
    protected String getWatchResourceValue() { return FlightControllerKey.IMU_STATE_ACCELEROMETER_STATE; }

    @Override
    protected void handleMiddleBtnClick() {
        if (timer == null) {
            timer = new Timer();
            gimbalRotationTimerTask = new MoveGimbalWithSpeedView.GimbalRotateTimerTask(10);
            timer.schedule(gimbalRotationTimerTask, 0, 100);
        }
    }

    @Override
    protected void handleLeftBtnClick() {
        //String acc = FlightControllerKey.IMU_STATE_ACCELEROMETER_STATE;
        DJIKey accKey = FlightControllerKey.create(FlightControllerKey.IMU_STATE_ACCELEROMETER_STATE);
        //Create Flightcontroller altitude key object
        DJIKey altitudeKey = FlightControllerKey.create(FlightControllerKey.ALTITUDE);

        //Add a listener with KeyListener callback for altitude key. NOTE: You can add multiple listeners for the same key
        DJISDKManager.getInstance().getKeyManager().addListener(accKey, new KeyListener() {
            @Override public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue) {
                if (newValue instanceof Float) {
                    final float acc = (Float) newValue;
                    //Do something with altitude value
                }
            }
        });
        if (timer != null) {
            if(gimbalRotationTimerTask != null) {
                gimbalRotationTimerTask.cancel();
            }
            timer.cancel();
            timer.purge();
            gimbalRotationTimerTask = null;
            timer = null;
        }

        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            DJISampleApplication.getProductInstance().getGimbal().
                    rotate(null, new CommonCallbacks.CompletionCallback() {

                        @Override
                        public void onResult(DJIError error) {

                        }
                    });
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (timer != null) {
            if(gimbalRotationTimerTask != null) {
                gimbalRotationTimerTask.cancel();
            }
            timer.cancel();
            timer.purge();
            gimbalRotationTimerTask = null;
            timer = null;
        }
    }

    @Override
    protected void handleRightBtnClick() {
        if (timer == null) {
            timer = new Timer();
            gimbalRotationTimerTask = new MoveGimbalWithSpeedView.GimbalRotateTimerTask(-10);
            timer.schedule(gimbalRotationTimerTask, 0, 100);
        }
    }

    @Override
    public int getDescription() {
        return R.string.gimbal_listview_rotate_gimbal;
    }

    private static class GimbalRotateTimerTask extends TimerTask {
        float pitchValue;

        GimbalRotateTimerTask(float pitchValue) {
            super();
            this.pitchValue = pitchValue;
        }

        @Override
        public void run() {
            if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                DJISampleApplication.getProductInstance().getGimbal().
                        rotate(new Rotation.Builder().pitch(pitchValue)
                                .mode(RotationMode.SPEED)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(0)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {

                            }
                        });
            }
        }
    }
}
