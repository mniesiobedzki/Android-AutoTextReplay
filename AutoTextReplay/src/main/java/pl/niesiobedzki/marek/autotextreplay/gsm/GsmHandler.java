package pl.niesiobedzki.marek.autotextreplay.gsm;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by marek on 17/6/13.
 */
public class GsmHandler {

    private static final String TAG = "ATRService";
    private final TelephonyManager mTm;
    private final int mResponseInterval;
    private String message = "";
    private int phoneState = TelephonyManager.CALL_STATE_IDLE;
    private ResponseHandler respondHandler = null;

    private boolean isActivated;

    public GsmHandler(TelephonyManager tm, String message, int responseInterval) {
        mTm = tm;
        this.message = message;
        this.mResponseInterval = responseInterval;
        this.isActivated = true;
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }



    /**
     * Listener for phone calls
     */
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {

            if (isActivated) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        phoneState = TelephonyManager.CALL_STATE_RINGING;
                        Log.d(TAG, "CALL_STATE_RINGING " + incomingNumber);
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d(TAG, "CALL_STATE_OFFHOOK " + incomingNumber);
                        phoneState = TelephonyManager.CALL_STATE_OFFHOOK;
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        if (phoneState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            Log.d(TAG, "CALL_STATE_OFFHOOK -> CALL_STATE_IDLE");
                            // ODEBRANA ROZMOWA
                        } else if (phoneState == TelephonyManager.CALL_STATE_RINGING) {
                            Log.d(TAG, "CALL_STATE_RINGING -> CALL_STATE_IDLE -> sendSMS(" + incomingNumber + ", " + message + ")");
                            // NIEODEBRANA ROZMOWA

                            //sendSMS(incomingNumber, message);
                            respondHandler.notifyNewPhoneCall(incomingNumber); //TODO: zamienic false
                        } else {
                            Log.d(TAG, "CALL_STATE_IDLE");
                        }

                        phoneState = TelephonyManager.CALL_STATE_IDLE;
                        break;
                    default:
                        Log.d(TAG, "Unknown phone state=" + state);
                }
            }

        }
    };

    public void setMessage(String message) {
        this.message = message;
    }

}
