package pl.niesiobedzki.marek.autotextreplay.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.GregorianCalendar;

import pl.niesiobedzki.marek.autotextreplay.R;
import pl.niesiobedzki.marek.autotextreplay.service.AutoTextReplayService;

/**
 * Main activity class
 *
 * @author Marek Adam Niesiobędzki
 */
public class AutoTextReplayMainActivity extends FragmentActivity {

    /* log's tag */
    private static final String TAG = "AutoTextReplayMainActivity";

    public static final String PREFS_NAME = "ATRpref";

    final Messenger messageActivtyService = new Messenger(new IncomingHandler());

    public static final int ACTIVATED = 1;

    /**
     * TIME section
     */

    /* Time For/upTo/selected Layouts */
    private RelativeLayout timeForRelativeLayout;
    private RelativeLayout timeUpToRelativeLayout;
    private RelativeLayout timeSelectedRelativeLayout;

    private long finishTime;
    private GregorianCalendar finishDate;

    private boolean timeForMode;
    private boolean timeUpToMode;

    private int mYear;
    private int mMonthOfYear;
    private int mDayOfMonth;

    private int mHour;
    private int mMinute;


    /**
     * Listener for clicks
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view.getId() == timeForRelativeLayout.getId()) {
                /* for time */
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_FOR_LAYOUT )");

                DialogFragment newFragment = new TimeForTimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "Timeicker");

            } else if (view.getId() == timeUpToRelativeLayout.getId()) {
                /* Up To time */
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_UP_TO_LAYOUT )");

                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getSupportFragmentManager(), "DatePicker");

            } else if (view.getId() == activatedCloseXTextView.getId()) {
                /* cancel activation finish time */
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( X )");


                finishTime = 0;
                timeForMode = false;
                timeUpToMode = false;

                AutoTextReplayMainActivity.this.changeToTimeModesMenu();


            } else {
                Log.w(TAG, "Unhandled OnClickListener for view " + (view.getId()) + " Resource name: " + getResources().getResourceEntryName(view.getId()));
            }

        }
    };

    /**
     * MESSAGE section
     */

    /* Message provided by user, which will replay for incoming connections and text */
    private EditText messageEditText;
    private CheckBox addLocationCheckBox;

    private boolean gpsLocation;

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
            } else {
                Log.w(TAG, "Unhandled OnCheckedChangeListener for compoundButton " + (compoundButton.getId()));
            }
        }
    };

    /**
     * NO MESSAGE DIALOG
     * If message is empty
     */
    public class FireMissilesDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_no_message_msg).setTitle(R.string.dialog_no_message_title)
                    .setPositiveButton(R.string.go_back_and_write, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            return builder.create();
        }
    }

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

    /**
     *  Listener for activatingToggleButton
     */
    private CompoundButton.OnCheckedChangeListener activatorToggleButtonListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                Log.d(TAG, "ToggleButton Activated");
                if (timeForMode || timeUpToMode) {
                    setFinishTime();
                    changeToTimeSummary(finishTime);
                } else {
                    changeTimeModeToNoModeSelected();
                }
                if(messageEditText.getText().toString().length() == 0){
                    activationToggleButton.setChecked(false);
                    FireMissilesDialogFragment fragment = new FireMissilesDialogFragment();
                    fragment.show(getSupportFragmentManager(), "Enable");
                } else {
                activateReplay();
                disableGUI();
                activated = true;
                }
            } else {
                Log.d(TAG, "ToggleButton Deactivated");

                deactivateReplay();
                enableGUI();
                activated = false;
                restoreTimeLayout();
            }
        }
    };
    private Messenger mService = null;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TextView activatedTillTextView;
    private TextView activatedTIMETextView;
    private TextView activatedCloseXTextView;
    private boolean activated;
    private ToggleButton activationToggleButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Main Activity onCreate()");
        setContentView(R.layout.main);

        /* SharePreferences */
        loadSharedPreferences();

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

        activatedTillTextView = (TextView) findViewById(R.id.Main_Time_Selected_textView_Title);
        activatedTIMETextView = (TextView) findViewById(R.id.Main_Time_Selected_textView_Time);


        activatedCloseXTextView = (TextView) findViewById(R.id.Main_Time_Selected_textView_X);
        activatedCloseXTextView.setClickable(true);
        activatedCloseXTextView.setOnClickListener(mClickListener);

        /* MESSAGE from user to replay */
        messageEditText = (EditText) findViewById(R.id.Main_Message_EditText);
        messageEditText.setEnabled(true);
        messageEditText.setFocusable(false);
        messageEditText.setFocusableInTouchMode(true);

        addLocationCheckBox = (CheckBox) findViewById(R.id.checkBox_add_location);
        addLocationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        addLocationCheckBox.setChecked(gpsLocation);

        interval_freq_desc_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_DESC_textView);
        interval_freq_number_textView = (TextView) findViewById(R.id.Main_Answer_Frequency_NUMBER_textView);

        intervalSeekBar = (SeekBar) findViewById(R.id.Main_Answer_Frequency_SeekBar1);
        intervalSeekBar.setOnSeekBarChangeListener(intervalSeekBarListener);
        intervalSeekBar.setProgress(25);


        /*
      ACTIVATION BUTTON
     */

        activationToggleButton = (ToggleButton) findViewById(R.id.Main_ACTIVATE_ToggleButton);
        activationToggleButton.setOnCheckedChangeListener(activatorToggleButtonListener);

        restoreMe(savedInstanceState);


        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("serviceToActivity"));

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Double currentSpeed = intent.getDoubleExtra("currentSpeed", 20);
            Double currentLatitude = intent.getDoubleExtra("latitude", 0);
            Double currentLongitude = intent.getDoubleExtra("longitude", 0);
            //  ... react to local broadcast message
            Log.d(TAG, "NOWE WSPÓŁRZEDNE " + currentLatitude + " " + currentLongitude);
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        checkIfServiceIsRunning();
        checkIfReplayIsactivated();
        restoreTimeLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        doUnBindService();
    }

    /**
     * Inflate the menu. Adds items to the action bar if it is present.
     */
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
        outState.putInt("responseInterval", intervalSeekBar.getProgress());
        outState.putBoolean("activated", activated);
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
            intervalSeekBar.setProgress(state.getInt("responseInterval"));
            activated = state.getBoolean("activated", false);
        } else {
            timeForMode = false;
            timeUpToMode = false;
            activated = false;
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

    /**
     * for comunication service -> acrivity
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Recieved message from service
     *
     * @author marek niesiobędzki
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle msgBundle = msg.getData();
            Log.i(TAG, "NEW MESSAGE with ID=" + msg.what);
            switch (msg.what) {
                case AutoTextReplayService.SERVICE_ACTIVATED:
                    Log.d(TAG, "Service is activated");
                    break;
                case AutoTextReplayService.SERVICE_DEACTIVATED:
                    Log.d(TAG, "Service is deactivated");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    /**
     * Interface for monitoring state of connection between the Activity and the Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.i(TAG, "Service Attached.");
            try {
                Message msg = Message.obtain(null,
                        AutoTextReplayService.SERVICE_REGISTER_NEW_CLIENT);
                msg.replyTo = messageActivtyService;
                mService.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "The service has crashed before we could even do anything with it");
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        /** is called when the connection with the service has been
         *  unexpectedly disconnected - process crashed */
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.i(TAG, "Service Disconnected!");
        }
    };


    /**
     * If the service is running when the activity starts, binds to it automatically.
     */
    private void checkIfServiceIsRunning() {
        if (AutoTextReplayService.isRunning()) {
            doBindService();
        } else {
            Intent serviceIntent = new Intent(this, AutoTextReplayService.class);
            startService(serviceIntent);
            doBindService();
        }
    }

    private void checkIfReplayIsactivated() {
        if (activated) {
            disableGUI();

        } else {
            enableGUI();
        }
    }

    private boolean isBound = false;

    /**
     * Binds the Activity to the Service
     */
    private void doBindService() {
        Intent bindIndent = new Intent(this, AutoTextReplayService.class);
        bindService(bindIndent, serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Log.i(TAG, "Activity Bound to Service");

    }

    /**
     * Unbinds the Activity from the Service
     */
    private void doUnBindService() {
        Log.d(TAG, "doUnBindService");
        if (isBound) {
            if (messageActivtyService != null) {
                Message msg = Message.obtain(null,
                        AutoTextReplayService.SERVICE_UNREGISTER_CLIENT);
                msg.replyTo = messageActivtyService;
                try {
                    messageActivtyService.send(msg);
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
        if (isFinishTimeInFuture() || !(timeForMode || timeUpToMode)) {
            /* if time mode was selected, selected time is in the future */
            if (isBound) {
                if (mService != null) {
                    Message msg = Message.obtain(null, AutoTextReplayService.MSG_SET_RESPOND_ACTION);
                    Bundle msgBundle = new Bundle();
                    msgBundle.putString("message", messageEditText.getText().toString());
                    msgBundle.putBoolean("timeFor", timeForMode);
                    msgBundle.putBoolean("timeUpTo", timeUpToMode);
                    msgBundle.putLong("finishTime", finishTime);
                    msgBundle.putInt("responseInterval", answerIntervalTime);
                    msgBundle.putBoolean("gpsLocation", addLocationCheckBox.isChecked());
                    msg.setData(msgBundle);
                    msg.replyTo = messageActivtyService;
                    try {
                        mService.send(msg);
                        this.activated = true;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error while sending activation replay message from activity to the service");
                        e.printStackTrace();
                        this.activated = false;
                    }
                }
            } else {
                /* activity is not bound with the service */
                Log.w(TAG,"activateReplay(): Activity is not bound with service. CAN'T SEND MSG");
            }
        } else {
            /* if time mode was selected, selected time is in the past */
            //TODO: dialog że nie można aktywować alarmu w przeszłości
            Log.e(TAG, "Selected Time is in the past");
        }
    }

    /**
     * Deactivates replay message in the service
     */
    private void deactivateReplay() {
        Log.d(TAG, "deactivateReplay()");

        if (isBound) {
            Log.d(TAG, "deactivateReplay() isBound");

            if (mService != null) {

                Log.i(TAG,
                        "deactivateReplay() messageActivtyService != null");
                Message msg = Message.obtain(null,
                        AutoTextReplayService.CANCEL_RESPOND_ACTION);
                msg.replyTo = messageActivtyService;
                try {
                    mService.send(msg);
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
     *
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

    /**
     * Restore preferences
     */
    private void loadSharedPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gpsLocation = settings.getBoolean(AutoTextReplayService.GPS_LOCATION, false);
        if (gpsLocation) {
            //TODO: laod position somewhere ;)
        }
    }

    /**
     * UP TO TIME DatePicker
     */
    public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonthOfYear = monthOfYear;
            mDayOfMonth = dayOfMonth;

            DialogFragment newFragment = new UptoTimePickerFragment();
            newFragment.show(getSupportFragmentManager(), "TimePicker");
        }
    }

    /**
     * UP TO TIME TimePicker
     */
    public class UptoTimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            mHour = hour;
            mMinute = minute;

            changeTimeModeToUpToTime(mYear, mMonthOfYear, mDayOfMonth, mHour, mMinute);
        }
    }

    /**
     * FOR TIME TimePicker
     */
    public class TimeForTimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = 0;
            int minute = 0;

            // create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            mHour = hour;
            mMinute = minute;

            changeTimeModeToForTime(hour, minute);
        }
    }


    private void setFinishTime() {
        Calendar calendarFinish = Calendar.getInstance();

        if (timeUpToMode) {
            /* up to time */
            calendarFinish.set(Calendar.YEAR, mYear);
            calendarFinish.set(Calendar.MONTH, mMonthOfYear);
            calendarFinish.set(Calendar.DAY_OF_MONTH, mDayOfMonth);
            calendarFinish.set(Calendar.SECOND, 0);
            calendarFinish.set(Calendar.MILLISECOND, 0);
            calendarFinish.set(Calendar.HOUR_OF_DAY, mHour);
            calendarFinish.set(Calendar.MINUTE, mMinute);

            finishTime = calendarFinish.getTimeInMillis();
        } else {
            finishTime = Calendar.getInstance().getTimeInMillis()
                    + (mMinute * 60 * 1000) + (mHour * 60 * 60 * 1000);

        }
    }

    private boolean isFinishTimeInFuture() {
        Calendar calendarNow = Calendar.getInstance();
        if (calendarNow.getTimeInMillis() < finishTime) {
            /* future */
            return true;
        } else {
            /* past */
            return false;
        }
    }

    /**
     * Sets finish time in millis and finish date.
     *
     * @param time - long in milliseconds
     */
    private void setFinishTimeDate(long time) {
        this.finishTime = time;
        finishDate = new GregorianCalendar();
        finishDate.setTimeInMillis(time);
        Log.i(TAG, "New finish time: " + time + " " + finishDate.toString());
    }

    /**
     * Change Time section after activation to summary layout,
     * if any mode was selected (up to or for).
     */
    private void changeToTimeSummary(long timeInMillis) {
        this.timeForRelativeLayout.setVisibility(View.GONE);
        this.timeUpToRelativeLayout.setVisibility(View.GONE);
        this.timeSelectedRelativeLayout.setVisibility(View.VISIBLE);

        finishDate = new GregorianCalendar();
        finishDate.setTimeInMillis(timeInMillis);
        this.activatedTIMETextView.setText("" + finishDate.get(GregorianCalendar.DAY_OF_MONTH) + "/"
                + (finishDate.get(GregorianCalendar.MONTH) + 1) + "/" +
                finishDate.get(GregorianCalendar.YEAR) + " " + finishDate.get(GregorianCalendar.HOUR_OF_DAY) + ":"
                + finishDate.get(GregorianCalendar.MINUTE) + ":"
                + finishDate.get(GregorianCalendar.SECOND));
    }

    /**
     * Show option "Time for" and "Up to"
     */
    private void changeToTimeModesMenu() {
        this.timeForRelativeLayout.setVisibility(View.VISIBLE);
        this.timeUpToRelativeLayout.setVisibility(View.VISIBLE);
        this.timeSelectedRelativeLayout.setVisibility(View.GONE);
    }

    /**
     * Changing Time segment from buttons to "FOR TIME" inactive mode
     *
     * @param hour
     * @param minutes
     */
    private void changeTimeModeToForTime(int hour, int minutes) {
        this.timeForRelativeLayout.setVisibility(View.GONE);
        this.timeUpToRelativeLayout.setVisibility(View.GONE);
        this.timeSelectedRelativeLayout.setVisibility(View.VISIBLE);

        this.activatedTillTextView.setText("Activation for");

        this.activatedTIMETextView.setText("" + hour + "h "
                + ((minutes < 10) ? "0" + minutes : "" + minutes) + "min");

        this.timeForMode = true;
    }

    /**
     * Changing Time segment from buttons to "UP TO TIME" inactive mode
     *
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     * @param hour
     * @param minute
     */
    private void changeTimeModeToUpToTime(int year, int monthOfYear, int dayOfMonth, int hour, int minute) {
        this.timeForRelativeLayout.setVisibility(View.GONE);
        this.timeUpToRelativeLayout.setVisibility(View.GONE);
        this.timeSelectedRelativeLayout.setVisibility(View.VISIBLE);

        this.activatedTillTextView.setText("Activation up to");

        this.activatedTIMETextView.setText(""
                + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year + " "
                + hour + ":" + ((minute < 10) ? "0" + minute : "" + minute));

        this.timeUpToMode = true;
    }

    private void changeTimeModeToNoModeSelected() {
        this.timeForRelativeLayout.setVisibility(View.GONE);
        this.timeUpToRelativeLayout.setVisibility(View.GONE);
        this.timeSelectedRelativeLayout.setVisibility(View.VISIBLE);

        this.activatedTillTextView.setText("No mode selected");

        this.timeSelectedRelativeLayout.setVisibility(View.GONE);
    }

    /**
     * Disable all controls for time when Replay msg is activated
     */
    private void disableGUI() {
        this.activatedCloseXTextView.setVisibility(View.GONE);
        this.activatedTillTextView.setEnabled(false);
        this.messageEditText.setEnabled(false);
        this.addLocationCheckBox.setEnabled(false);
        this.intervalSeekBar.setEnabled(false);
    }

    /**
     * Enable all controls for time when Replay msg is deactivated
     */
    private void enableGUI() {
        this.activatedCloseXTextView.setVisibility(View.VISIBLE);
        this.activatedTillTextView.setEnabled(true);
        this.messageEditText.setEnabled(true);
        this.addLocationCheckBox.setEnabled(true);
        this.intervalSeekBar.setEnabled(true);
        this.timeSelectedRelativeLayout.setVisibility(View.VISIBLE);
    }

    private void restoreTimeLayout() {
        if (activated) {
            changeToTimeSummary(finishTime);
        } else if (timeForMode) {
            changeTimeModeToForTime(mHour, mMinute);
        } else if (timeUpToMode) {
            changeTimeModeToUpToTime(mYear, mMonthOfYear, mDayOfMonth, mHour, mMinute);
        } else {
            changeToTimeModesMenu();
        }
    }

}