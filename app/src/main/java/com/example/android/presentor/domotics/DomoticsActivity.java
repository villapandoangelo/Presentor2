package com.example.android.presentor.domotics;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.utils.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DomoticsActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, View.OnLongClickListener {

    private final static String LOG_TAG = DomoticsActivity.class.getSimpleName();

    private static final int BT_REQUEST_STATE = 50;
    private static final int BT_SWITCH_STATE_ON = 1;
    private static final int BT_SWITCH_STATE_OFF = 2;

    private String[] applianceNameKey;

    private CardView[] mCardView = new CardView[8];
    private ImageView[] mBgImageView = new ImageView[8];
    private TextView[] mNameTextView = new TextView[8];
    private TextView[] mStateTextView = new TextView[8];
    private Switch[] mSwitchView = new Switch[8];

    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mBluetoothSocket;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = "00:21:13:00:99:DA";      //temporary

    private Switch sMasterSwitch;
    public static boolean[] sArduinoSwitchesState = new boolean[8];

    private void findViews() {
        mCardView[0] = findViewById(R.id.card_view_domotics_1);
        mCardView[1] = findViewById(R.id.card_view_domotics_2);
        mCardView[2] = findViewById(R.id.card_view_domotics_3);
        mCardView[3] = findViewById(R.id.card_view_domotics_4);
        mCardView[4] = findViewById(R.id.card_view_domotics_5);
        mCardView[5] = findViewById(R.id.card_view_domotics_6);
        mCardView[6] = findViewById(R.id.card_view_domotics_7);
        mCardView[7] = findViewById(R.id.card_view_domotics_8);
        //Bg find view
        mBgImageView[0] = mCardView[0].findViewById(R.id.image_view_appliance_status_1);
        mBgImageView[1] = mCardView[1].findViewById(R.id.image_view_appliance_status_2);
        mBgImageView[2] = mCardView[2].findViewById(R.id.image_view_appliance_status_3);
        mBgImageView[3] = mCardView[3].findViewById(R.id.image_view_appliance_status_4);
        mBgImageView[4] = mCardView[4].findViewById(R.id.image_view_appliance_status_5);
        mBgImageView[5] = mCardView[5].findViewById(R.id.image_view_appliance_status_6);
        mBgImageView[6] = mCardView[6].findViewById(R.id.image_view_appliance_status_7);
        mBgImageView[7] = mCardView[7].findViewById(R.id.image_view_appliance_status_8);
        //name findview
        mNameTextView[0] = mCardView[0].findViewById(R.id.text_view_appliance_name_1);
        mNameTextView[1] = mCardView[1].findViewById(R.id.text_view_appliance_name_2);
        mNameTextView[2] = mCardView[2].findViewById(R.id.text_view_appliance_name_3);
        mNameTextView[3] = mCardView[3].findViewById(R.id.text_view_appliance_name_4);
        mNameTextView[4] = mCardView[4].findViewById(R.id.text_view_appliance_name_5);
        mNameTextView[5] = mCardView[5].findViewById(R.id.text_view_appliance_name_6);
        mNameTextView[6] = mCardView[6].findViewById(R.id.text_view_appliance_name_7);
        mNameTextView[7] = mCardView[7].findViewById(R.id.text_view_appliance_name_8);
        //state find view
        mStateTextView[0] = mCardView[0].findViewById(R.id.text_view_appliance_status_1);
        mStateTextView[1] = mCardView[1].findViewById(R.id.text_view_appliance_status_2);
        mStateTextView[2] = mCardView[2].findViewById(R.id.text_view_appliance_status_3);
        mStateTextView[3] = mCardView[3].findViewById(R.id.text_view_appliance_status_4);
        mStateTextView[4] = mCardView[4].findViewById(R.id.text_view_appliance_status_5);
        mStateTextView[5] = mCardView[5].findViewById(R.id.text_view_appliance_status_6);
        mStateTextView[6] = mCardView[6].findViewById(R.id.text_view_appliance_status_7);
        mStateTextView[7] = mCardView[7].findViewById(R.id.text_view_appliance_status_8);

        mSwitchView[0] = mCardView[0].findViewById(R.id.switch_appliance_1);
        mSwitchView[1] = mCardView[1].findViewById(R.id.switch_appliance_2);
        mSwitchView[2] = mCardView[2].findViewById(R.id.switch_appliance_3);
        mSwitchView[3] = mCardView[3].findViewById(R.id.switch_appliance_4);
        mSwitchView[4] = mCardView[4].findViewById(R.id.switch_appliance_5);
        mSwitchView[5] = mCardView[5].findViewById(R.id.switch_appliance_6);
        mSwitchView[6] = mCardView[6].findViewById(R.id.switch_appliance_7);
        mSwitchView[7] = mCardView[7].findViewById(R.id.switch_appliance_8);
    }

    private void initListener() {
        for (Switch sw : mSwitchView) {
            sw.setOnCheckedChangeListener(this);
        }
        for (CardView cv : mCardView) {
            cv.setOnClickListener(this);
            cv.setOnLongClickListener(this);
        }
    }

    private void initView() {
        applianceNameKey = this.getResources().getStringArray(R.array.appliance_name);
        for (int i = 0; i < 8; i++) {
            String applianceName = initDomoticsName(applianceNameKey, i);
            mNameTextView[i].setText(applianceName);
            mStateTextView[i].setText("Off");
        }
    }

    private String initDomoticsName(String[] applianceNameKey, int keyIndex) {
        String applianceKey = applianceNameKey[keyIndex];
        String applianceName = Utility.getString(this, applianceKey);
        if (applianceName == null) {
            applianceName = applianceKey;
            Utility.saveString(this, applianceKey, applianceName);
        }
        return applianceName;
    }


    private void initMasterSwitch() {
        CardView masterSwitchCardView = (CardView) findViewById(R.id.card_view_master_switch);
        sMasterSwitch = (Switch) findViewById(R.id.switch_master);

        sMasterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                turnMasterSwitch(isChecked);
            }
        });

        masterSwitchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sMasterSwitch.setChecked(!sMasterSwitch.isChecked());
            }
        });
    }


    private void turnMasterSwitch(boolean state) {

        TextView masterSwitchStatusText = (TextView) findViewById(R.id.text_view_master_switch_status);
        ImageView masterSwitchStatus = (ImageView) findViewById(R.id.image_view_master_switch_status);
        Drawable d;

        for (Switch sw : mSwitchView) {
            sw.setChecked(state);
        }

        if (state) {
            //turn on
            d = DomoticsActivity.this.getResources().getDrawable(R.drawable.ic_power_bg_on);
            masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                    getString(R.string.appliance_status_on));
        } else {
            //turn off
            d = DomoticsActivity.this.getResources().getDrawable(R.drawable.ic_power_bg_off);
            masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                    getString(R.string.appliance_status_on));
        }
        masterSwitchStatus.setBackground(d);
    }

//    private void turnAllSwitch(boolean b) {
//        for (int i = 0; i < 8; i++) {
//            mSwitchView[i].setChecked(b);
//            turnOnArduinoSwitch(i, b);
//        }
//        //mDsAdapter.notifyDataSetChanged();
//    }
//
//    //this function avoids redundant sending of data to arduino
//    public static void turnOnArduinoSwitch(int i, boolean turnOn) {
//        int powerOn = i + 1;
//        int powerOff = (i + 1) * 11;
//        if (turnOn) {
//            //turn on
//            if (!sArduinoSwitchesState[i]) {
//                Log.d("Arduino", "Turn on " + powerOn);
//                sArduinoSwitchesState[i] = true;
//                turnOnLed(i + 1);
//            }
//        } else {
//            //turn off
//            if (sArduinoSwitchesState[i]) {
//                Log.d("Arduino", "Turn off " + powerOff);
//                sArduinoSwitchesState[i] = false;
//                turnOffLed(i + 1);
//            }
//
//        }
//    }


    public static void turnOffLed(int switchNumber) {
        switchNumber = switchNumber * 11;
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.getOutputStream().write(switchNumber);
            } catch (IOException e) {
                //msg("Error");
            }
        }
    }

    public static void turnOnLed(int switchNumber) {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.getOutputStream().write(switchNumber);
            } catch (IOException e) {
                //msg("Error");
            }
        }
    }


    public void renameAppliance(final String key, final TextView tv) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Rename Switch");
        final EditText et = new EditText(this);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.setMargins(margins, margins, margins, 0);
        String oldName = Utility.getString(this, key);
        et.setLayoutParams(params);
        et.setText(oldName);
        et.setSelectAllOnFocus(true);
        et.setSingleLine();
        container.addView(et);
        adb.setView(container);

//        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                switch (i) {
//                    case DialogInterface.BUTTON_POSITIVE:
//                        //rename appliance method;
//                        if(TextUtils.isEmpty(et.getText())){
//                            et.setError("Switch name must not be empty!");
//                            return;
//                        }
//                        Utility.saveString(getApplicationContext(), key, et.getText().toString().trim());
//                        tv.setText(Utility.getString(getApplicationContext(), key));
//
//                }
//            }
//        };


        adb.setPositiveButton(R.string.rename, null);
        adb.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = adb.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = (alertDialog).getButton(DialogInterface.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(et.getText())) {
                            et.setError("Switch name must not be empty!");
                            return;
                        }
                        Utility.saveString(getApplicationContext(), key, et.getText().toString().trim());
                        tv.setText(Utility.getString(getApplicationContext(), key));
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private boolean isAllSwitchOpen() {
        for (int i = 0; i < 8; i++) {
            boolean somethingIsOff = !mSwitchView[i].isChecked();
            if (somethingIsOff) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllSwitchOff() {
        for (int i = 0; i < 8; i++) {
            boolean somethingIsOn = mSwitchView[i].isChecked();
            if (somethingIsOn) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics);

        findViews();
        initView();
        initListener();
        initMasterSwitch();

        new BtConnectThread().start();

        //new BtConnectAsyncTask().execute();
    }

    @Override
    protected void onDestroy() {
        Log.e("DomoticsActivity", "onDestroy() callback");
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {

        }
        mBluetoothSocket = null;
        mBluetoothAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int position = -1;
        switch (compoundButton.getId()) {
            case R.id.switch_appliance_1:
                position = 0;
                break;
            case R.id.switch_appliance_2:
                position = 1;
                break;
            case R.id.switch_appliance_3:
                position = 2;
                break;
            case R.id.switch_appliance_4:
                position = 3;
                break;
            case R.id.switch_appliance_5:
                position = 4;
                break;
            case R.id.switch_appliance_6:
                position = 5;
                break;
            case R.id.switch_appliance_7:
                position = 6;
                break;
            case R.id.switch_appliance_8:
                position = 7;
                break;
        }
        if (position != -1) {
            if (mSwitchView[position].isChecked()) {
                turnOnLed(position + 1);
                if (isAllSwitchOpen()) {
                    sMasterSwitch.setChecked(true);
                }
                mStateTextView[position].setText("On");
                mBgImageView[position].setBackground(getResources().getDrawable(R.drawable.ic_power_bg_on));
            } else {
                turnOffLed(position + 1);
                if (isAllSwitchOff()) {
                    sMasterSwitch.setChecked(false);
                }
                mStateTextView[position].setText("Off");
                mBgImageView[position].setBackground(getResources().getDrawable(R.drawable.ic_power_bg_off));
            }
        }
    }

    @Override
    public void onClick(View view) {
        int position = -1;
        switch (view.getId()) {
            case R.id.card_view_domotics_1:
                position = 0;
                break;
            case R.id.card_view_domotics_2:
                position = 1;
                break;
            case R.id.card_view_domotics_3:
                position = 2;
                break;
            case R.id.card_view_domotics_4:
                position = 3;
                break;
            case R.id.card_view_domotics_5:
                position = 4;
                break;
            case R.id.card_view_domotics_6:
                position = 5;
                break;
            case R.id.card_view_domotics_7:
                position = 6;
                break;
            case R.id.card_view_domotics_8:
                position = 7;
                break;
        }
        if (position != -1) {
            mSwitchView[position].setChecked(!mSwitchView[position].isChecked());
        }

    }

    @Override
    public boolean onLongClick(View view) {
        int position = -1;
        switch (view.getId()) {
            case R.id.card_view_domotics_1:
                position = 0;
                break;
            case R.id.card_view_domotics_2:
                position = 1;
                break;
            case R.id.card_view_domotics_3:
                position = 2;
                break;
            case R.id.card_view_domotics_4:
                position = 3;
                break;
            case R.id.card_view_domotics_5:
                position = 4;
                break;
            case R.id.card_view_domotics_6:
                position = 5;
                break;
            case R.id.card_view_domotics_7:
                position = 6;
                break;
            case R.id.card_view_domotics_8:
                position = 7;
                break;
        }
        renameAppliance(applianceNameKey[position], mNameTextView[position]);
        return true;
    }

    class BtConnectThread extends Thread {
        private ProgressDialog mProgressDialog;

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(DomoticsActivity.this, null, "Connecting to Presentor");
                }
            });
            try {
                if (mBluetoothSocket == null) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice presentorBtDevice = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    mBluetoothSocket = presentorBtDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBluetoothSocket.connect();//start connection
//                    for (int i = 0; i < 2; i++) {
//                        int state = mBluetoothSocket.getInputStream().read();
//                        Log.e("BtStateReceiverThread", "State received: " + state);
                    new BtReceiveStateThread().start();
                    mBluetoothSocket.getOutputStream().write(0);
                }
            } catch (IOException e) {
                finish();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                }
            });


        }
    }

    class BtReceiveStateThread extends Thread{

        boolean waitForMessage = true;
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[256];
        int bytes;
        String finalMessage;

        public char[] receivedState(){
            return finalMessage.toCharArray();
        }

        public void updateInterface(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    char[] switchState = receivedState();
                    for(int i = 0; i < 8; i++){
                        if(switchState[i] == '1'){
                            //is on
                            mSwitchView[i].setChecked(true);
                        }else{
                            mSwitchView[i].setChecked(false);
                        }
                    }
                }
            });
        }

        @Override
        public void run() {
            try {
                InputStream mBtInputStream = mBluetoothSocket.getInputStream();
                while (waitForMessage) {
                    bytes = mBtInputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    sb.append(incomingMessage);
                    int endOfLineIndex = sb.indexOf(".");
                    if (endOfLineIndex > 0 ) {
                        finalMessage = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());
                        if(finalMessage.length() == 8){
                            waitForMessage = false;
                        }
                    }
                }
                //update the interface.
                updateInterface();
            } catch (IOException e) {
                //TODO HANDLE ERRORS
            }
        }
    }
}
