package pl.niesiobedzki.marek.autotextreplay;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

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

    /**
     * MESSAGE section
     */

    /* Message provided by user, which will replay for incoming connections and text */
    private EditText messageEditText;

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
                //TODO: service start
            } else {
                Log.d(TAG, "ToggleButton Deactivated");
                //TODO: service stop
            }
        }
    };

    private TextView interval_freq_desc_textView;
    private TextView interval_freq_number_textView;

    /* interval duration seek bar */
    private SeekBar intervalSeekBar;
    private SeekBar.OnSeekBarChangeListener intervalSeekBarListener = new SeekBar.OnSeekBarChangeListener() {



        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.d(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
            //TODO: seekbar impementation
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTrackingTouch: " + seekBar.getProgress());
            //TODO: seekbar impementation
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
            //TODO: seekbar impementation
        }
    };

    /**
     *
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view.getId() == timeForRelativeLayout.getId()) {
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_FOR_LAYOUT )");
                //TODO: for time button implementation
            } else if (view.getId() == timeUpToRelativeLayout.getId()) {
                Log.d(TAG, "View.OnClickListener mClickListener.onClick( TIME_UP_TO_LAYOUT )");
                //TODO: up to time button implementation
            } else {
                Log.w(TAG, "Unhandled OnClickListener for view " + (view.getId()));
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Main Activity onCreate()");
        setContentView(R.layout.main);

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

}
