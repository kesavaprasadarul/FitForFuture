package com.bosch.vaehiclefitness.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bosch.vaehiclefitness.R;
import com.bosch.vaehiclefitness.model.LocationData;
import com.bosch.vaehiclefitness.util.Constants;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class VehicleDriveTestActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    static double impulseWeight[] = new double[3];
    final double alpha = 0.45;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Sensor sensor;
    private int RawFilteringEnabled = 1;

    private double SpeedCurrent = 0, AccelerationCurrent = 0;

    private TextView SetStatusTxt;
    private double CompensatedSpeed = 0;
    private Button setSpdBtn, rstBtn, startTstBtn, brakingReadyBtn, pauseBrakeTstBtn, finishOdoEntryBtn;
    private EditText spdLimitTxt, odoStartTxt, odoEndTxt, fareStartTxt, fareEndTxt;
    private TextInputLayout odoEndTxtInputLayout, spdLimitTxtInputLayout, odoStartTxtInputLayout, tifareStartTxt, tifareEndTxt;
    private SwitchMaterial fareMeterSwt;
    private int stage = -1; // -1 - Preparation,  0 - Start, 1 - Speed reached, 2 - 40 km/h speedometer test passed, command to brake, 3 - braking, 4- end
    private boolean hasFailed = true;
    private double AccelerationPeak = 0;
    private double OdoStartKm, OdoEndKm, SpeedLimit;

    private ArrayList<LocationData> Coordinates;

    public boolean isVehicleClass2 = false; // not a passenger car - Vehicle class 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_drive_test);

        // Accelerometer Init
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        // Location Init
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

            }
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        //Controls Init
        SetStatusTxt = findViewById(R.id.statusTxt);
        startTstBtn = findViewById(R.id.startTstBtn);
        odoStartTxt = findViewById(R.id.odoStartTxt);
        odoStartTxtInputLayout = findViewById(R.id.odoStartTxtInputLayout);
        odoEndTxt = findViewById(R.id.odoEndTxt);
        odoEndTxtInputLayout = findViewById(R.id.odoEndTxtInputLayout);
        spdLimitTxt = findViewById(R.id.spdLimitTxt);
        spdLimitTxtInputLayout = findViewById(R.id.spdLimitTxtInputLayout);
        fareStartTxt = findViewById(R.id.fareStartTxt);
        fareEndTxt = findViewById(R.id.fareEndTxt);
        tifareStartTxt = findViewById(R.id.tifareStartTxt);
        tifareEndTxt = findViewById(R.id.tifareEndTxt);
        fareMeterSwt = findViewById(R.id.fareMeterSwt);
        fareMeterSwt.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVehicleClass2 = isChecked;
                if(!isVehicleClass2)
                {
                    fareStartTxt.setVisibility(View.GONE);
                    fareEndTxt.setVisibility(View.GONE);
                    tifareStartTxt.setVisibility(View.GONE);
                    tifareEndTxt.setVisibility(View.GONE);
                }
                else{
                    fareStartTxt.setVisibility(View.VISIBLE);
                    fareEndTxt.setVisibility(View.GONE);
                    tifareStartTxt.setVisibility(View.VISIBLE);
                    tifareEndTxt.setVisibility(View.GONE);
                }

            }

        });
        startTstBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(odoStartTxt.getText())) {
                    Toast.makeText(VehicleDriveTestActivity.this,
                            R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
                    return;
                }
                Coordinates = new ArrayList<>();
                OdoStartKm = OdoEndKm = Double.parseDouble(odoStartTxt.getText().toString());
                stage = 0;
                startTstBtn.setVisibility(View.GONE);
                odoStartTxt.setText("");
                spdLimitTxt.setText("");
                odoStartTxt.setVisibility(View.GONE);
                odoStartTxtInputLayout.setVisibility(View.GONE);
                spdLimitTxt.setVisibility(View.GONE);
                spdLimitTxtInputLayout.setVisibility(View.GONE);
                rstBtn.setVisibility(View.VISIBLE);
            }
        });
        setSpdBtn = findViewById(R.id.spdSetBtn);
        setSpdBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CompensatedSpeed = SpeedCurrent - 15;
                stage = 1;
                setSpdBtn.setVisibility(View.GONE);

            }
        });

        brakingReadyBtn = findViewById(R.id.brakingReadyBtn);
        brakingReadyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                brakingReadyBtn.setVisibility(View.GONE);
                stage = 2;
                pauseBrakeTstBtn.setVisibility(View.VISIBLE);
            }
        });

        pauseBrakeTstBtn = findViewById(R.id.pauseBrakingBtn);
        pauseBrakeTstBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                brakingReadyBtn.setVisibility(View.VISIBLE);
                stage = -2;
                pauseBrakeTstBtn.setVisibility(View.GONE);
            }
        });

        finishOdoEntryBtn = findViewById(R.id.finishOdoEntryBtn);
        finishOdoEntryBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(odoEndTxt.getText())) {
                    Toast.makeText(VehicleDriveTestActivity.this,
                            R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
                    return;
                }
                rstBtn.setVisibility(View.VISIBLE);
                OdoEndKm = Double.parseDouble(odoEndTxt.getText().toString());
                isOdoTestPassed = calculateDistance(OdoEndKm - OdoStartKm);
                stage = 6;
            }
        });


        rstBtn = findViewById(R.id.rstBtn);
        rstBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CompensatedSpeed = 0;
                SpeedCurrent = 0;
                AccelerationCurrent = 0;
                stage = -1;
                startTstBtn.setVisibility(View.VISIBLE);
                odoStartTxt.setVisibility(View.VISIBLE);
                odoStartTxtInputLayout.setVisibility(View.VISIBLE);
                spdLimitTxt.setVisibility(View.VISIBLE);
                spdLimitTxtInputLayout.setVisibility(View.VISIBLE);
                odoEndTxt.setVisibility(View.GONE);
                odoEndTxtInputLayout.setVisibility(View.GONE);
                setSpdBtn.setVisibility(View.GONE);
                brakingReadyBtn.setVisibility(View.GONE);
                rstBtn.setVisibility(View.GONE);
                startTstBtn.setVisibility(View.VISIBLE);
                OdoStartKm = OdoEndKm = 0;
                hasFailed = false;
            }
        });

        updateDisplay();
    }

    double CurrentTargetSpeed;
    public boolean isCheckDecelerating = false;
    public boolean isOdoTestPassed = false;

    private boolean calculateDistance(double odoMeterDistance) {
        double distance = 0;
        float[] temp = new float[1];
        for (int i = 0; i < Coordinates.size() - 1; i++) {
            Location.distanceBetween(Coordinates.get(i).Latitude, Coordinates.get(i).Longitude, Coordinates.get(i + 1).Latitude, Coordinates.get(i + 1).Longitude, temp);
            distance = distance + temp[0];
        }

        if (Math.abs((odoMeterDistance * 1000) - distance) < 500)
            return true;
        else
            return false;
    }

    Timer CheckSpeedDecelerationTimer = new Timer();
    Timer CheckAccelerometerPeaksAndFullBrakeTimer = new Timer();

    private void CheckSpeedDeceleration() {
        if (isCheckDecelerating == false && stage == 2) {
            isCheckDecelerating = true;
            CurrentTargetSpeed = 30;

            CheckSpeedDecelerationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new TimerTask() {
                        @Override
                        public void run() {
                            if (stage != 2) this.cancel();
                            Log.i("Current Speed", SpeedCurrent + "km/h");

                            if (Math.abs(SpeedCurrent) < (Math.abs(CurrentTargetSpeed) - 7.5)) {
                                isCheckDecelerating = false;
                                stage = 3;
                            }
                        }
                    });

                }
            }, 100, 200);
        }
    }

    public boolean isCheckAccelerometerandStop = false;
    public double MaxAccel = 0;

    private void CheckAccelerometerPeaksAndFullBrake() {

        isCheckAccelerometerandStop = true;
        CheckAccelerometerPeaksAndFullBrakeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        if (Math.abs(SpeedCurrent) > 5) {
                            MaxAccel = AccelerationCurrent > MaxAccel ? AccelerationCurrent : MaxAccel;
                        } else {
                            AccelerationPeak = MaxAccel;
                            isCheckAccelerometerandStop = false;
                            stage = 4;
                        }
                    }
                });
            }
        }, 100, 200);

    }

    // -1 - Preparation,  0 - Start, 1 - Speed reached, 2 - 40 km/h speedometer test passed, command to brake, 3 - braking, 4- end
    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        switch (stage) {
                            case -1:
                                SetStatusTxt.setText(R.string.info_drive_test_prepare);
                                break;
                            case 0:
                                SetStatusTxt.setText(R.string.info_drive_test_start);
                                setSpdBtn.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                if (Math.abs(CompensatedSpeed) > 3.5) {
                                    SetStatusTxt.setText(R.string.info_speedometer_test_failed);
                                    hasFailed = true;
                                    finish();
                                } else {
                                    SetStatusTxt.setText(R.string.info_speedometer_test_passed);
                                    brakingReadyBtn.setVisibility(View.VISIBLE);
                                }
                                break;
                            case 2:
                                SetStatusTxt.setText(R.string.info_brake_test);
                                if (!isCheckDecelerating)
                                    CheckSpeedDeceleration();
                                break;
                            case -2:
                                SetStatusTxt.setText(R.string.info_break_test_2);
                                isCheckDecelerating = false;
                                break;

                            case 3:
                                isCheckDecelerating = false;
                                if (CheckSpeedDecelerationTimer != null)
                                    CheckSpeedDecelerationTimer.cancel();
                                CheckSpeedDecelerationTimer = null;
                                SetStatusTxt.setText(R.string.info_breaking_detected);
                                if (!isCheckAccelerometerandStop)
                                    CheckAccelerometerPeaksAndFullBrake();
                                break;
                            case 4:
                                isCheckAccelerometerandStop = false;
                                if (CheckAccelerometerPeaksAndFullBrakeTimer != null)
                                    CheckAccelerometerPeaksAndFullBrakeTimer.cancel();
                                CheckAccelerometerPeaksAndFullBrakeTimer = null;
                                SetStatusTxt.setText(R.string.info_sequence_complete_processing);
                                if (AccelerationPeak > 5) {
                                    stage = 5;
                                } else {
                                    SetStatusTxt.setText(R.string.info_break_test_failed);
                                    hasFailed = true;
                                    finish();
                                    break;
                                }
                            case 5:
                                SetStatusTxt.setText(R.string.info_enter_odometer_reading);
                                pauseBrakeTstBtn.setVisibility(View.GONE);
                                odoEndTxt.setVisibility(View.VISIBLE);
                                odoEndTxtInputLayout.setVisibility(View.VISIBLE);
                                finishOdoEntryBtn.setVisibility(View.VISIBLE);
                                break;
                            case 6:
                                if (isOdoTestPassed) {
                                    SetStatusTxt.setText(R.string.info_drive_test_all_pass);
                                    odoEndTxt.setVisibility(View.GONE);
                                    odoEndTxtInputLayout.setVisibility(View.GONE);
                                    finishOdoEntryBtn.setVisibility(View.GONE);
                                    rstBtn.setVisibility(View.GONE);
                                } else {
                                    SetStatusTxt.setText(R.string.info_drive_test_failed);
                                    hasFailed = false;
                                    finish();
                                }
                        }
                    }
                });
            }
        }, 0, 200);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Constants.KEY_RESULT, hasFailed ? 0 : 1);
        setResult(RESULT_OK, data);
        super.finish();
    }


    @Override
    public void onLocationChanged(Location location) {

        SpeedCurrent = roundThreeDecimals(location.getSpeed() * 3.6);
        if (stage != -1)
            Coordinates.add(new LocationData(location.getLatitude(), location.getLongitude()));
        Log.i("Speed: ", SpeedCurrent + " km/h");

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] accel = new float[3];
        // setStatus();
        if (RawFilteringEnabled != 1) {
            accel = event.values;
        } else {
            impulseWeight[0] = alpha * impulseWeight[0] + (1 - alpha) * event.values[0];
            impulseWeight[1] = alpha * impulseWeight[1] + (1 - alpha) * event.values[1];
            impulseWeight[2] = alpha * impulseWeight[2] + (1 - alpha) * event.values[2];

            accel[0] = (float) (event.values[0] - impulseWeight[0]);
            accel[1] = (float) (event.values[1] - impulseWeight[1]);
            accel[2] = (float) (event.values[2] - impulseWeight[2]);
        }

        AccelerationCurrent = roundThreeDecimals(calculateResultant(accel));
        Log.i("Acceleration: ", AccelerationCurrent + "m/s");
    }

    double roundThreeDecimals(double d) {
        DecimalFormat threeDForm = new DecimalFormat("#.#");
        return Double.valueOf(threeDForm.format(d));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double calculateResultant(float[] accel) {
        float x2 = accel[0] * accel[0];
        float y2 = accel[1] * accel[1];
        float z2 = accel[2] * accel[2];

        return Math.sqrt(x2 + y2 + z2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
