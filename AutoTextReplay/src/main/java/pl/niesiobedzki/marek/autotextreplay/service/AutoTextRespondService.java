package pl.niesiobedzki.marek.autotextreplay.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;

/**
 * @author Marek Adam Niesiobędzki
 */
public class AutoTextRespondService extends Service {

    private static final String TAG = "ATRService";

    public static final int SERVICE_DEACTIVATED = 0;
    public static final int SERVICE_ACTIVATED = 1;
    public static final int SERVICE_REGISTER_NEW_CLIENT = 2;
    public static final int SERVICE_UNREGISTER_CLIENT = 3;
    public static final int MSG_SET_RESPOND_ACTION = 4;
    public static final int CANCEL_RESPOND_ACTION = 5;

    private int phoneState = TelephonyManager.CALL_STATE_IDLE;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
