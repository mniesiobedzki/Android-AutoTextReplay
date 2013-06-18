package pl.niesiobedzki.marek.autotextreplay.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import pl.niesiobedzki.marek.autotextreplay.gsm.RespondHandler;
import pl.niesiobedzki.marek.autotextreplay.location.MyLocation;

/**
 * @author Marek Adam Niesiobędzki
 */
public class AutoTextRespondService extends Service {

    private static final String TAG = "ATRService";

    private MyLocation myLocation;
    private String locationString = "";
    private boolean isLocation;
    private RespondHandler respondHandler;

    private long finishTime;
    private String message;
    private boolean gpsLocation;

    public AutoTextRespondService() {
        Log.d(TAG, "Service initialized");

    }

    public static final int SERVICE_DEACTIVATED = 0;
    public static final int SERVICE_ACTIVATED = 1;
    public static final int SERVICE_REGISTER_NEW_CLIENT = 2;
    public static final int SERVICE_UNREGISTER_CLIENT = 3;
    public static final int MSG_SET_RESPOND_ACTION = 4;
    public static final int CANCEL_RESPOND_ACTION = 5;

    //TODO: poprwaic active
    private static boolean isActivated = false;
    private static boolean isRunning = false;

    private int phoneState = TelephonyManager.CALL_STATE_IDLE;

    private int responseInterval;

    final Messenger messageService = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler { // Handler of incoming messages from

        // clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_DEACTIVATED:
                    // mClients.add(msg.replyTo);
                    Log.d(TAG, "IncomingHandler.handleMessage(SERVICE_DEACTIVATED)");
                    break;
                case SERVICE_ACTIVATED:
                    Log.d(TAG, "IncomingHandler.handleMessage(SERVICE_ACTIVATED)");
                    // mClients.remove(msg.replyTo);
                /*Message msg_activated = Message.obtain(null, AutoTextRespondActivity.ACTIVATED);
                try {
					messageService.send(msg_activated);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
                    break;
                case SERVICE_REGISTER_NEW_CLIENT:
                    // incrementby = msg.arg1;
                    break;
                case SERVICE_UNREGISTER_CLIENT:
                    // incrementby = msg.arg1;
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

                    //message = msgBundle.getString("msg");
                    //int respondInterval = msgBundle.getInt("duration");
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
        //isLocation = intent.getBooleanExtra("isLocation", false);


        return messageService.getBinder();
    }

    public AutoTextRespondService(String message, int responseInterval) {

        this.message = message;
        this.responseInterval = responseInterval;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "AutoTextRespondService.onCreate");
        this.myLocation = new MyLocation(getApplicationContext());
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
     * Sends message
     *
     * @param phoneNumber
     * @param messageToSend
     */
    private void sendSMS(String phoneNumber, String messageToSend) {
        SmsManager sms = SmsManager.getDefault();
        Log.d(TAG, "sendSMS(" + phoneNumber + ", " + messageToSend + ")");
        sms.sendTextMessage(phoneNumber, null, messageToSend, null, null);
        Log.d(TAG, "SMS sent");
    }

    /**
     * @return true if service is running
     */
    public static boolean isRunning() {
        return isRunning;
    }

}
