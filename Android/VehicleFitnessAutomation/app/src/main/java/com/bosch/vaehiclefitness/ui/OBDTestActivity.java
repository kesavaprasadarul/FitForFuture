package com.bosch.vaehiclefitness.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bosch.vaehiclefitness.R;
import com.bosch.vaehiclefitness.util.CSVFile;
import com.bosch.vaehiclefitness.util.Constants;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public class OBDTestActivity extends AppCompatActivity {

    public final int ACCPEDAL_D_INDEX = 2,
            ACCPEDAL_E_INDEX = 3,
            BATT_V_INDEX = 5,
            COOLNT_T_INDEX = 8,
            DISTANCE_W_MIL_INDEX = 10,
            EPM_N_INDEX = 13,
            MAF_INDEX = 16,
            MAP_INDEX = 17,
            SPEED_INDEX = 21,
            MIL_STATUS_INDEX = 23;

    public Button LoadCSVBtn, FinishBtn;
    public TextView statusTxt;
    private boolean isIMSTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd_test);
        isIMSTest = getIntent().getBooleanExtra("TYPE_IMS_TEST", false);

        LoadCSVBtn = findViewById(R.id.LoadCSVBtn);
        statusTxt = findViewById(R.id.statusTxt);
        if (isIMSTest) {
            statusTxt.setText(R.string.info_ims_test);
        }
        LoadCSVBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openFile();
                FinishBtn.setVisibility(View.GONE);

            }
        });

        FinishBtn = findViewById(R.id.finshBtn);
        FinishBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();

            }
        });

    }


    private static final int PICK_CSV_FILE = 2;

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_CSV_FILE);
        } else {
            Toast.makeText(this, "No application installed on your device to choose csv files",
                    Toast.LENGTH_SHORT).show();
        }
    }

    InputStream iStream;
    CSVFile csvFile;
    HashSet<Double> AccPed = new HashSet<>();

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0d;
    }

    public boolean RunChecks(List rawData) {
        AccPed = new HashSet<>();
        for (int i = 1; i < rawData.size(); i++) {
            String[] row = (String[]) rawData.get(i);
            double Epm = parseDouble(row[EPM_N_INDEX]);
            double BattV = parseDouble(row[BATT_V_INDEX]);
            double CoolntT = parseDouble(row[COOLNT_T_INDEX]);
            double DistMIL = parseDouble(row[DISTANCE_W_MIL_INDEX]);
            double AccPD = row[ACCPEDAL_D_INDEX].isEmpty() ? 0 : parseDouble(row[ACCPEDAL_D_INDEX]);
            double AccPE = row[ACCPEDAL_E_INDEX].isEmpty() ? 0 : parseDouble(row[ACCPEDAL_E_INDEX]);
            double MAF = parseDouble(row[MAF_INDEX]);
            double MAP = parseDouble(row[MAP_INDEX]);
            double Speed = parseDouble(row[SPEED_INDEX]);
            Boolean MILSTATUS = Boolean.parseBoolean(row[MIL_STATUS_INDEX]);

            //Acc Pedal Plausibility
            if (Math.abs((AccPD / 2) - (AccPE)) > 2) {
                statusTxt.setText(R.string.info_obd_test_acc_pedal_failed);
                return false;
            }

            //CoolantCheck
            if (CoolntT > 90) {
                statusTxt.setText(R.string.info_obd_test_coolant_check_failed);
                return false;
            }

            //MIL STATUS
            if (MILSTATUS) {
                statusTxt.setText(R.string.info_obd_test_mil_test_fail);
                return false;
            }

            // MIL Distance check since reset
            if (DistMIL > 0) {
                statusTxt.setText(R.string.info_obd_test_mil_distance_error);
                return false;
            }

            //MAF
            if (MAF < 5) {
                statusTxt.setText(R.string.info_obd_test_maf_value_error);
                return false;
            }

            //MAP
            if (MAP < 85) {
                statusTxt.setText(R.string.info_obd_test_map_value_error);
                return false;
            }


            //Battery Voltage Check
            if (Epm > 800) {
                if (BattV < 12) {
                    statusTxt.setText(R.string.info_obd_test_battery_voltage_fail);
                    return false;
                }
            }

            AccPed.add(AccPedRound(AccPD));
        }

        if (AccPed.size() < 4) {
            statusTxt.setText(R.string.info_obd_test_acc_pedal_stuck_check_fail);
            return false;
        }

        statusTxt.setText(R.string.info_obd_test_pass);
        return true;
    }


    public double AccPedRound(double AccPedVal) {
        return 5 * (Math.floor(Math.abs(AccPedVal / 5)));
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Constants.KEY_RESULT, hasFailed ? 0 : 1);
        setResult(RESULT_OK, data);
        super.finish();
    }

    boolean hasFailed = true;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data.getData() != null) {
            if (isIMSTest) {
                hasFailed = false;
                FinishBtn.setVisibility(View.VISIBLE);
                return;
            }
            Uri uri = data.getData();
            try {
                iStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            csvFile = new CSVFile(iStream);
            List rawData = csvFile.read();
            Boolean Res = RunChecks(rawData);
            hasFailed = !Res;
            FinishBtn.setVisibility(View.VISIBLE);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}