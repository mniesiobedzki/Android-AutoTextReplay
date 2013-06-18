package pl.niesiobedzki.marek.autotextreplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
public class AutoTextReplayMainActivity extends Activity {

    /* log's tag */
    private static final String TAG = "AutoTextReplayMainActivity";

    /**
     * TIME section
     */

    /* Time For/upTo/selected Layouts */
    private RelativeLayout timeForRelativeLayout;
    private RelativeLayout timeUpToRelativeLayout;
    private RelativeLayout timeSelectedRelativeLayout;

    private long finishTime;

    /**
     * MESSAGE section
     */

    /* Message provided by user, which will replay for incoming connections and text */
    private EditText messageEditText;
   private CheckBox addLicationCheckBox;
    private boolean addLocation = false;

    //private LocationManager locationManager;

    //TODO: e-mail sender to send e-mials and text both together

    /**
     * ANSWER INTERVAL section
     */

    /* For activating and deactivating service */
    private ToggleButton activationToggleButton;

    /* Listener for activatingToggleButton */
    private CompoundButton.OnCheckedChangeListener activatorToggleButtonListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                Log.d(TAG, "ToggleButton Activated");
                sendNewRespondAction();
            } else {
                Log.d(TAG, "ToggleButton Deactivated");
                sendRespondActionCancelation();
            }
        }
    };

    private TextView interval_freq_desc_textView;
    private TextView interval_freq_number_textView;
    private int responseIntervalMIN = 0;

    /* interval duration seek bar */
    private SeekBar intervalSeekBar;
    int answerIntervalTime;
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
     * Listener for Checkboxes
     */
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Log.d(TAG, "CheckBox Listener");

            if(compoundButton.getId() == addLicationCheckBox.getId()){
                /* add location checkbox */
                Log.d(TAG, "Checkbox Add location " + b);
                if(b){

                    //TODO: check if GPS is On and display msg to the user http://www.vogella.com/articles/AndroidLocationAPI/article.html 2.8

                    addLocation = true;
                   // myLocation.requestLocationUpdates();
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
                } else {
                    addLocation = false;
                    //locationManager.removeGpsStatusListener((GpsStatus.Listener) locationListener);
                   // myLocation.removeGpsStatusListener();
                }
            } else {
                Log.w(TAG, "Unhandled OnCheckedChangeListener for compoundButton " + (compoundButton.getId()));
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
        timeSelectedRelativeLayout = (RelativeLayout) findViewById(R.id.Main_Time_Selected);
        timeSelectedRelativeLayout.setOnClickListener(mClickListener);

        /* massage from user to replay */
        messageEditText = (EditText) findViewById(R.id.Main_Message_EditText);
        messageEditText.setEnabled(true);
        messageEditText.setFocusable(false);
        messageEditText.setFocusableInTouchMode(true);

        addLicationCheckBox = (CheckBox) findViewById(R.id.checkBox_add_location);
        addLicationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

        interval_freq_desc_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_DESC_textView);
        interval_freq_number_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_NUMBER_textView);

        intervalSeekBar = (SeekBar) findViewById(R.id.Main_Answer_Frequency_SeekBar1);
        intervalSeekBar.setOnSeekBarChangeListener(intervalSeekBarListener);
        intervalSeekBar.setProgress(25);


        activationToggleButton = (ToggleButton) findViewById(R.id.Main_ACTIVATE_ToggleButton);
        activationToggleButton.setOnCheckedChangeListener(activatorToggleButtonListener);

        restoreMe(savedInstanceState);

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        CheckIfServiceIsRunning();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.respond, menu);
        return true;
    }

    /**
     * Stores variables state
     *
     * @param outState
     * @see Activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("msg", messageEditText.getText().toString());
        //outState.putBoolean("isEmail", messageEmail.isChecked());
        outState.putInt("duration", intervalSeekBar.getProgress());
    }

    /**
     * Restores variables from saved state
     *
     * @param state - Bundle
     */
    private void restoreMe(Bundle state) {
        if (state != null) {
            messageEditText.setText(state.getString("msg"));
            //messageEmail.setChecked(state.getBoolean("isEmail"));
            intervalSeekBar.setProgress(state.getInt("duration"));
        }
    }

    /**
     * Converts seekbar position from 0..100 progress to time durations and changes application view
     *
     * @param progress - seekbar state as integer 0..100
     * @return - minutes of response interval
     */
    private int seekBarToMinutes(int progress) {
        Integer[] timeInMinutes = { 0, 5, 30, 60, 120, 240, 720, 1440, 2880, 10080,
                20160, -1 };
        int[] timesS = { 0, 5, 30, 1, 2, 4, 8, 1, 2, 7, 14, -1 };
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

    /**
     * Recieved message from service
     *
     * @author marek niesiobędzki
     *
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
     * Podpięcie Activity do Servisu
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
                // In this case the service has crashed before we could even do
                // anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected - process crashed.
            messageService = null;
            Log.i(TAG, "Service Disconnected!");
        }
    };

    private boolean isBound = false;
    private void CheckIfServiceIsRunning() {
        // If the service is running when the activity starts, we want to
        // automatically bind to it.
        Log.i(TAG, "CheckIfServiceIsRunning()");
        if (AutoTextRespondService.isRunning()) {
            doBindService();
        } else {
            Intent serviceIntent = new Intent(this, AutoTextRespondService.class);
            startService(serviceIntent);
            doBindService();
        }

    }
    /**
     * Łączy activity z servisem
     */
    private void doBindService() {
        Intent bindIndent = new Intent(this, AutoTextRespondService.class);
               bindService(bindIndent,
                serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Log.i(TAG, "Activity Bound to Service");
    }

    private void doUnBindService() {
        if (isBound) {
            // If we have received the service, and hence registered with it,
            // then now is the time to unregister.
            if (messageService != null) {
                Message msg = Message.obtain(null,
                        AutoTextRespondService.SERVICE_UNREGISTER_CLIENT);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            unbindService(serviceConnection);
            isBound = false;
            Log.i(TAG, "Activity unBound to Service");
        }
    }

    private void sendNewRespondAction() {
        if (isBound) {
            if (messageService != null) {
                Message msg = Message.obtain(null,
                        AutoTextRespondService.MSG_SET_RESPOND_ACTION);
                Bundle msgBundle = new Bundle();
                msgBundle
                        .putString("message", messageEditText.getText().toString());
                //msgBundle.putBoolean("isEmail", messageCheckBox.isChecked());
                msgBundle.putLong("finishTime", finishTime);
                msgBundle.putInt("responseInterval", responseIntervalMIN);
                msgBundle.putBoolean("gpsLocation", addLicationCheckBox.isChecked());
                msg.setData(msgBundle);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            Log.w(TAG,
                    "sendNewRespondAction(): Activity is not bound with service. CAN'T SEND MSG");
        }
    }

    private void sendRespondActionCancelation() {
        Log.i(TAG, "sendRespondActionCancelation()");

        if (isBound) {
            Log.i(TAG, "sendRespondActionCancelation() isBound");

            if (messageService != null) {

                Log.i(TAG,
                        "sendRespondActionCancelation() messageService != null");
                Message msg = Message.obtain(null,
                        AutoTextRespondService.CANCEL_RESPOND_ACTION);
                msg.replyTo = messageService;
                try {
                    messageService.send(msg);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            Log.w(TAG,
                    "sendNewRespondAction(): Activity is not bound with service. CAN'T SEND MSG");
        }
    }


}
