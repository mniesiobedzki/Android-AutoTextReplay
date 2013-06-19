package pl.niesiobedzki.marek.autotextreplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import pl.niesiobedzki.marek.autotextreplay.service.AutoTextRespondService;

/**
 * Main activity class
 *
 * @author Marek Adam Niesiobędzki
 */
public class AutoTextReplayMainActivity extends FragmentActivity {

    /* log's tag */
    private static final String TAG = "AutoTextReplayMainActivity";

    /**
     * TIME section
     */

    /* Time For/upTo/selected Layouts */
    private RelativeLayout timeForRelativeLayout;
    private RelativeLayout timeUpToRelativeLayout;

    private long finishTime;

    private boolean timeForMode;
    private boolean timeUpToMode;


    /**
     * Listener for clicks
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view.getId() == timeForRelativeLayout.getId()) {
                /* for time */
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_FOR_LAYOUT )");
                //TODO: for time button implementation
            } else if (view.getId() == timeUpToRelativeLayout.getId()) {
                /* Up To time */
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_UP_TO_LAYOUT )");
                //TODO: up to time button implementation
            } else {
                Log.w(TAG, "Unhandled OnClickListener for view " + (view.getId()));
            }

        }
    };

    /**
     * MESSAGE section
     */

    /* Message provided by user, which will replay for incoming connections and text */
    private EditText messageEditText;
    private CheckBox addLocationCheckBox;
    private CheckBox sendEmailCheckBox;

    /**
     * Listener for Checkboxes in Message Section
     */
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            Log.d(TAG, "CheckBox Listener");

            /* ADD LOCATION CheckBOx */
            if (compoundButton.getId() == addLocationCheckBox.getId()) {
                /* add location checkbox */
                Log.d(TAG, "Checkbox Add location " + isChecked);
                if (isChecked) {

                    /** Check if enabled and if not display a dialog and suggesting togo to the settings */
                    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                    boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!enabled) {
                        EnableGpsDialogFragment enableGpsDialogFragment = new EnableGpsDialogFragment();
                        enableGpsDialogFragment.show(getSupportFragmentManager(), "Enable");
                    }
                }
            } else if (compoundButton.getId() == sendEmailCheckBox.getId()){
                //TODO: impelement
            } else {
                Log.w(TAG, "Unhandled OnCheckedChangeListener for compoundButton " + (compoundButton.getId()));
            }
        }
    };

    /**
     * ANSWER INTERVAL section
     */

    private TextView interval_freq_desc_textView;
    private TextView interval_freq_number_textView;

    /* interval duration seek bar */
    private SeekBar intervalSeekBar;
    int answerIntervalTime;

    /* SeekBar Listener */
    private SeekBar.OnSeekBarChangeListener intervalSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.d(TAG, "onProgressChanged: " + seekBar.getProgress());
            answerIntervalTime = seekBarToMinutes(seekBar.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTrackingTouch: " + seekBar.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
        }
    };


    /* Listener for activatingToggleButton */
    private CompoundButton.OnCheckedChangeListener activatorToggleButtonListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                Log.d(TAG, "ToggleButton Activated");
                activateReplay();
            } else {
                Log.d(TAG, "ToggleButton Deactivated");
                deactivateReplay();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Main Activity onCreate()");
        setContentView(R.layout.main);

        /* TIME */
        this.finishTime = 0;

        /* For time Layout */
        timeForRelativeLayout = (RelativeLayout) findViewById(R.id.Main_Time_FOR_Layout);
        timeForRelativeLayout.setOnClickListener(mClickListener);

        /* Up To time Layout */
        timeUpToRelativeLayout = (RelativeLayout) findViewById(R.id.Main_Time_UPTO_Layout);
        timeUpToRelativeLayout.setOnClickListener(mClickListener);

        /* Time Selected Layout - when user activate service for specific time */
        RelativeLayout timeSelectedRelativeLayout = (RelativeLayout) findViewById(R.id.Main_Time_Selected);
        timeSelectedRelativeLayout.setOnClickListener(mClickListener);

        /* massage from user to replay */
        messageEditText = (EditText) findViewById(R.id.Main_Message_EditText);
        messageEditText.setEnabled(true);
        messageEditText.setFocusable(false);
        messageEditText.setFocusableInTouchMode(true);

        sendEmailCheckBox = (CheckBox) findViewById(R.id.Main_Message_EMAIL_checkBox);
        sendEmailCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        sendEmailCheckBox.setVisibility(View.GONE); // TODO: implement E-mail notyfication

        addLocationCheckBox = (CheckBox) findViewById(R.id.checkBox_add_location);
        addLocationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

        interval_freq_desc_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_DESC_textView);
        interval_freq_number_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_NUMBER_textView);

        intervalSeekBar = (SeekBar) findViewById(R.id.Main_Answer_Frequency_SeekBar1);
        intervalSeekBar.setOnSeekBarChangeListener(intervalSeekBarListener);
        intervalSeekBar.setProgress(25);


        /*
      ACTIVATION BUTTON
     */
        ToggleButton activationToggleButton = (ToggleButton) findViewById(R.id.Main_ACTIVATE_ToggleButton);
        activationToggleButton.setOnCheckedChangeListener(activatorToggleButtonListener);

        restoreMe(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        checkIfServiceIsRunning();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause()");
        doUnBindService();
    }

    /**
     * Inflate the menu. Adds items to the action bar if it is present.
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.respond, menu);
        return true;
    }

    /**
     * Stores application's variables state
     *
     * @param outState - state of the Activity
     * @see Activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("timeFor", timeForMode);
        outState.putBoolean("timeUpTo", timeUpToMode);
        outState.putLong("finishTime", finishTime);
        outState.putString("message", messageEditText.getText().toString());
        outState.putBoolean("gpsLocation", addLocationCheckBox.isChecked());
        outState.putBoolean("isEmail", sendEmailCheckBox.isChecked());
        outState.putInt("responseInterval", intervalSeekBar.getProgress());
    }

    /**
     * Restores variables from saved state
     *
     * @param state - state of the Activity
     * @see Activity
     */
    private void restoreMe(Bundle state) {
        if (state != null) {
            timeForMode = state.getBoolean("timeFor", false);
            timeUpToMode = state.getBoolean("timeUpTo", false);
            finishTime = state.getLong("finishTime", 0);
            messageEditText.setText(state.getString("message"));
            addLocationCheckBox.setChecked(state.getBoolean("gpsLocation"));
            sendEmailCheckBox.setChecked(state.getBoolean("isEmail"));
            intervalSeekBar.setProgress(state.getInt("responseInterval"));
        } else {
            timeForMode = false;
            timeUpToMode = false;
        }
    }

    /**
     * Converts seekbar position from 0..100 progress to time durations and changes application view
     *
     * @param progress - seekbar state as integer 0..100
     * @return - minutes of response interval
     */
    private int seekBarToMinutes(int progress) {
        Integer[] timeInMinutes = {0, 5, 30, 60, 120, 240, 720, 1440, 2880, 10080,
                20160, -1};
        int[] timesS = {0, 5, 30, 1, 2, 4, 8, 1, 2, 7, 14, -1};
        int group = (int) (progress / 8.4);
        Log.d(TAG, "seekBarToMinutes(" + progress + ") " + progress + "/12="
                + group + " timeInMinutes[" + group + "]=" + timeInMinutes[group]);
        if (group == 0) {
            interval_freq_desc_textView.setText(getString(R.string.eachTime1) + " ");
            interval_freq_number_textView.setText(getString(R.string.eachTime2) + " ");
        } else if (group > 0 && group < 3) {
            interval_freq_desc_textView.setText(getString(R.string.oneAnswerPer) + " ");
            interval_freq_number_textView
                    .setText(timesS[group] + getString(R.string.min) + " ");
        } else if (group > 2 && group < 7) {
            interval_freq_desc_textView.setText(getString(R.string.oneAnswerPer) + " ");
            interval_freq_number_textView.setText(timesS[group] + getString(R.string.hour)
                    + " ");
        } else if (group == 7) {
            interval_freq_desc_textView.setText(getString(R.string.oneAnswerPer) + " ");
            interval_freq_number_textView
                    .setText(timesS[group] + getString(R.string.day) + " ");
        } else if (group > 7 && group < 11) {
            interval_freq_desc_textView.setText(getString(R.string.oneAnswerPer) + " ");
            interval_freq_number_textView.setText(timesS[group] + getString(R.string.days)
                    + " ");
        } else {
            interval_freq_desc_textView.setText(getString(R.string.oneAnswerPer) + " ");
            interval_freq_number_textView.setText(getString(R.string.event) + " ");
        }
        return timeInMinutes[group];
    }

    /** for comunication service -> acrivity */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    /**
     * Recieved message from service
     *
     * @author marek niesiobędzki
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AutoTextRespondService.SERVICE_ACTIVATED:
                    Log.d(TAG, "Service is activated");
                    break;
                case AutoTextRespondService.SERVICE_DEACTIVATED:
                    Log.d(TAG, "Service is deactivated");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    Messenger messageService = null;
    /**
     * Interface for monitoring state of connection between the Activity and the Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            messageService = new Messenger(service);
            Log.i(TAG, "Service Attached.");
            try {
                Message msg = Message.obtain(null,
                        AutoTextRespondService.SERVICE_REGISTER_NEW_CLIENT);
                msg.replyTo = messageService;
                messageService.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "The service has crashed before we could even do anything with it");
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        /** is called when the connection with the service has been
         *  unexpectedly disconnected - process crashed */
        public void onServiceDisconnected(ComponentName className) {
            messageService = null;
            Log.i(TAG, "Service Disconnected!");
        }
    };


    /**
     * If the service is running when the activity starts, binds to it automatically.
     */
    private void checkIfServiceIsRunning() {
        if (AutoTextRespondService.isRunning()) {
            doBindService();
        } else {
            Intent serviceIntent = new Intent(this, AutoTextRespondService.class);
            startService(serviceIntent);
            doBindService();
        }
    }

    private boolean isBound = false;
    /**
     * Binds the Activity to the Service
     */
    private void doBindService() {
        Intent bindIndent = new Intent(this, AutoTextRespondService.class);
        bindService(bindIndent,
                serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Log.i(TAG, "Activity Bound to Service");
    }

    /**
     * Unbinds the Activity from the Service
     */
    private void doUnBindService() {
        if (isBound) {
            if (messageService != null) {
                Message msg = Message.obtain(null,
                        AutoTextRespondService.SERVICE_UNREGISTER_CLIENT);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while sending activation replay message from activity to the service");
                    e.printStackTrace();
                }
            }
            unbindService(serviceConnection);
            isBound = false;
            Log.d(TAG, "Activity unBound to Service");
        }
    }

    /**
     * Activates replay message in the service
     */
    private void activateReplay() {
        if (isBound) {
            if (messageService != null) {
                Message msg = Message.obtain(null, AutoTextRespondService.MSG_SET_RESPOND_ACTION);
                Bundle msgBundle = new Bundle();
                msgBundle.putString("message", messageEditText.getText().toString());
                msgBundle.putLong("finishTime", finishTime);
                msgBundle.putInt("responseInterval", answerIntervalTime);
                msgBundle.putBoolean("gpsLocation", addLocationCheckBox.isChecked());
                msg.setData(msgBundle);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while sending activation replay message from activity to the service");
                    e.printStackTrace();

                }
            }
        } else {
            Log.w(TAG,
                    "activateReplay(): Activity is not bound with service. CAN'T SEND MSG");
        }
    }

    /**
     * Deactivates replay message in the service
     */
    private void deactivateReplay() {
        Log.d(TAG, "deactivateReplay()");

        if (isBound) {
            Log.d(TAG, "deactivateReplay() isBound");

            if (messageService != null) {

                Log.i(TAG,
                        "deactivateReplay() messageService != null");
                Message msg = Message.obtain(null,
                        AutoTextRespondService.CANCEL_RESPOND_ACTION);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while deactivating bind between the activity and the service");
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Log.w(TAG,
                    "activateReplay(): Activity is not bound with service. CAN'T SEND MSG");
        }
    }

    /**
     * Dialog with question to enable GPS module if it not enabled.
     * @see DialogFragment
     */
    public class EnableGpsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.enableGpsQM)
                    .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            addLocationCheckBox.setChecked(false);
                        }
                    });
            return builder.create();
        }
    }


}
