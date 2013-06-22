package pl.niesiobedzki.marek.autotextreplay.gsm;

import android.telephony.SmsManager;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

import pl.niesiobedzki.marek.autotextreplay.location.MyLocation;

/**
 * @author Marek Adam Niesiobędzki
 */
public class ResponseHandler {

    private static final String TAG = "RespondLog";

    /**
     * Static container for callers phone numbers and time when they get last
     * response message <Phone number, time>
     */
    private static HashMap<String, Long> respondLog = new HashMap<String, Long>();

    private static Long MIN_IN_MILLIS = (long) 60000;
    private  MyLocation myLocation;

    private String message;

    private long finishTime;
    private int repsondInterval;


//    public ResponseHandler(String message, int repsondInterval) {
//        this.message = message;
//        this.repsondInterval = (long) repsondInterval * MIN_IN_MILLIS;
//        Log.v(TAG, "New ResponseHandler: respondInterval: "
//                + this.repsondInterval + " ");
//    }

    public ResponseHandler(String message, long finishTime, int responseInterval) {
        this.message = message;
        this.finishTime = finishTime;
        this.repsondInterval = responseInterval;
        Log.v(TAG, "New ResponseHandler: respondInterval: "
                + this.repsondInterval + " ");

    }

    public ResponseHandler() {
        //TODO:
    }

    /**
     * Adds phone number to respond's log with actual time.
     *
     * @param phoneNumber
     *            - ex <i>+48601234567</i>
     */
    public void addPhoneNumber(String phoneNumber) {
        respondLog.put(phoneNumber, new Date().getTime());

    }

    /**
     * Checks if person was calling during this Respond Action.
     *
     * @param phoneNumber
     * @return <b>true</b> if there was connection from <i>phoneNumber</i>
     *         before, <b>false</b> if doesn't.
     */
    public boolean wasCalling(String phoneNumber) {
        if (respondLog.containsKey(phoneNumber)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTimeToRespond(String phoneNumber, Long respondFrequnece) {
        if (respondLog.containsKey(phoneNumber)) {
            if (respondFrequnece > (new Date().getTime() - respondLog
                    .get(phoneNumber))) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     *
     * @param incomingNumber
     *
     */
    public void notifyNewPhoneCall(String incomingNumber) {

        String locationString;
       // if(myLocation.isActive()){
            //locationString = myLocation;
        //} else {
         //   locationString = "";
        //}
        if (!wasCalling(incomingNumber)) {
            Log.d(TAG, "newPhoneCall TRUE");

            sendSMS(incomingNumber);
        } else {
            Log.d(TAG, "newPhoneCall FALSE");
            if (canSendText(incomingNumber)) {
                sendSMS(incomingNumber);
            }
        }
    }

    /**
     *
     * @param incomingNumber
     * @return
     */
    private boolean canSendText(String incomingNumber) {
        Long time = new Date().getTime();
        Log.d(TAG,
                "canSendText -> Time " + time + " - "
                        + respondLog.get(incomingNumber) + " = "
                        + (time - respondLog.get(incomingNumber)));
        if ((time - respondLog.get(incomingNumber)) > this.getRepsondInterval()) {
            Log.d(TAG, "canSendText TRUE");
            return true;
        } else {
            Log.d(TAG, "canSendText FALSE");
            return false;
        }
    }

    /**
     * Sends message
     *
     * @param phoneNumber
     * @param
     */
    private void sendSMS(String phoneNumber) {
        String messageToSend = this.message;
        if(myLocation.isActive()){
            messageToSend += " "+  myLocation.toString();
        }
        SmsManager sms = SmsManager.getDefault();
        Log.d(TAG, "sendSMS(" + phoneNumber + ", " + messageToSend + ")");
        sms.sendTextMessage(phoneNumber, null, messageToSend, null, null);
        Log.d(TAG, "SMS sent");
        addPhoneNumber(phoneNumber);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRepsondInterval() {
        return repsondInterval;
    }

    /**
     * Sets minimum time between responses.
     *
     * @param repsondInterval
     *            - time in milliseconds
     */
    public void setRepsondInterval(int repsondInterval) {
        this.repsondInterval = repsondInterval;
    }

    public void setMyLocation(MyLocation myLocation) {
        this.myLocation = myLocation;
    }
}
