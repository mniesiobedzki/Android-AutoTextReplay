package pl.niesiobedzki.marek.autotextreplay.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;

import pl.niesiobedzki.marek.autotextreplay.activity.AutoTextReplayMainActivity;
import pl.niesiobedzki.marek.autotextreplay.gsm.RespondHandler;
import pl.niesiobedzki.marek.autotextreplay.location.MyLocation;

/**
 * @author Marek Adam Niesiobędzki
 */
public class AutoTextReplayService extends Service {

    private static final String TAG = "ATRService";
    public static final int NEW_GPS_COORDINATES = 6;


    private MyLocation myLocation;
    private RespondHandler respondHandler;

    private long finishTime;
    private String message;
    private boolean gpsLocation;

    public static final int SERVICE_DEACTIVATED = 0;
    public static final int SERVICE_ACTIVATED = 1;
    public static final int SERVICE_REGISTER_NEW_CLIENT = 2;
    public static final int SERVICE_UNREGISTER_CLIENT = 3;
    public static final int MSG_SET_RESPOND_ACTION = 4;
    public static final int CANCEL_RESPOND_ACTION = 5;

    public static final String GPS_LATITUDE = "gpsLatitude";
    public static final String GPS_LONGITUDE = "gpsLongitude";
    public static final String GPS_ALTITUDE = "gpsAltitude";
    public static final String GPS_ACCURACY = "gpsAccuracy";

    private static boolean isActivated = false;
    private static boolean isRunning = false;

    private int phoneState = TelephonyManager.CALL_STATE_IDLE;

    private int responseInterval;

    final Messenger messageService = new Messenger(new IncomingHandler());

    private ArrayList<Messenger> mClients;

    /**
     * Handler for incomming msg from activity to service
     */
    class IncomingHandler extends Handler { // Handler of incoming messages from

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "New message from the Activity: "+msg.what);
            switch (msg.what) {
                case SERVICE_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    Log.d(TAG, "IncomingHandler.handleMessage(SERVICE_DEACTIVATED)");
                    break;
                case SERVICE_REGISTER_NEW_CLIENT:
                    Log.d(TAG, "IncomingHandler.handleMessage(SERVICE_ACTIVATED)");
                    /* add client to list of activities conencted to the service */
                    mClients.add(msg.replyTo);
                    Message msg_activated = Message.obtain(null, AutoTextReplayMainActivity.ACTIVATED);
                    try {
                        messageService.send(msg_activated);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error while sending activation msg service->activity");
                    }
                    break;
                case MSG_SET_RESPOND_ACTION:
                    Log.i(TAG, "New respond event activation");
                    Bundle msgBundle = msg.getData();

                    if (msgBundle.containsKey("message")) {
                        message = msgBundle.getString("message");
                    } else {
                        message = "No message from the user";
                    }

                    finishTime = msgBundle.getLong("finishTime", 0);
                    responseInterval = msgBundle.getInt("responseInterval", 0);
                    gpsLocation = msgBundle.getBoolean("gpsLocation", false);

                    respondHandler = new RespondHandler(message, finishTime, responseInterval);
                    if (gpsLocation) {
                        myLocation.requestLocationUpdates();
                        respondHandler.setMyLocation(myLocation);
                    }

                    isActivated = true;
                    break;
                case CANCEL_RESPOND_ACTION:
                    Log.i(TAG, "New respond event deactivation");
                    isActivated = false;

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Listener for incoming phone calls
     */
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {

            if (isActivated) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        /*  */
                        phoneState = TelephonyManager.CALL_STATE_RINGING;
                        Log.d(TAG, "CALL_STATE_RINGING " + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        /*  */
                        Log.d(TAG, "CALL_STATE_OFFHOOK " + incomingNumber);
                        phoneState = TelephonyManager.CALL_STATE_OFFHOOK;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (phoneState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            /* Answered call */
                            Log.d(TAG, "CALL_STATE_OFFHOOK -> CALL_STATE_IDLE");
                        } else if (phoneState == TelephonyManager.CALL_STATE_RINGING) {
                            /* Missed call */
                            Log.d(TAG, "CALL_STATE_RINGING -> CALL_STATE_IDLE -> sendSMS(" + incomingNumber + ", " + message + ")");
                            respondHandler.notifyNewPhoneCall(incomingNumber);
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

    @Override
    public IBinder onBind(Intent intent) {
        return messageService.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "AutoTextReplayService.onCreate");
        this.mClients = new ArrayList<Messenger>();
        this.myLocation = new MyLocation(getApplicationContext(), this);
        this.respondHandler = new RespondHandler();
        // Intent i = new Intent(AutoTextReplayMainActivity.class)
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //GsmHandler gsmHandler = new GsmHandler(tm, this.message, this.responseInterval);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "Service stopped.");
    }

    /**
     * @return true if service is running
     */
    public static boolean isRunning() {
        return isRunning;
    }

    public void sendToActivityNewGpsCordinates(Location location) {
        Log.d(TAG, "sendToActivityNewGpsCordinates");
        if (!mClients.isEmpty()) {
            Message msg = Message.obtain(null, AutoTextReplayService.NEW_GPS_COORDINATES);
            Bundle msgBundle = new Bundle();
            msgBundle.putDouble(GPS_LATITUDE, location.getLatitude());
            msgBundle.putDouble(GPS_LONGITUDE, location.getLongitude());
            msgBundle.putDouble(GPS_ALTITUDE, location.getAltitude());
            msgBundle.putFloat(GPS_ACCURACY, location.getAccuracy());
            msg.setData(msgBundle);
            sendMessageToActivty(msg);
        } else {
            Log.w(TAG, "No clients connected");
        }
    }

    /**
     * Sens message to all activities connected to the service
     * - in most cases it will be at most one activity
     * @param msg - message from service to activity
     * @see Message
     */
    private void sendMessageToActivty(Message msg) {
       // msg.replyTo = messageService;
        for (Messenger messenger : mClients) {
            try {
                messenger.send(msg);
                Log.i(TAG, "Message from service to activity send");
            } catch (RemoteException e) {
                /* if the client is dead I have to remove him from the list */
                mClients.remove(messenger);
            }
        }
    }

}
