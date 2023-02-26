/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.otp;

import com.mailjet.client.ClientOptions;
import static com.mailjet.client.ClientOptions.builder;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.sms.SmsSend;
import static it.refill.action.ActionB.trackingAction;
import it.refill.db.Db_Bando;

/**
 *
 * @author rcosco
 */
public class Sms {

    //private static final String MESSAGE_HIGH_QUALITY = "GP";
//    public static final String MESSAGE_MEDIUM_QUALITY = "TI";
    //private static final String MESSAGE_LOW_QUALITY = "SI";

//    public static boolean sendSMS2021_MJ(String cell, String msg) {
//        try {
//            Db_Bando dbb = new Db_Bando();
//            String mailjet_smstoken = dbb.getPath("mailjet_smstoken");
//            dbb.closeDB();
//
//            ClientOptions options = builder().bearerAccessToken(mailjet_smstoken).build();
//
//            MailjetClient client = new MailjetClient(options);
//
//            MailjetRequest request = new MailjetRequest(SmsSend.resource)
//                    .property(SmsSend.FROM, nomevisual)
//                    .property(SmsSend.TO, "+39" + cell)
//                    .property(SmsSend.TEXT, msg);
//            MailjetResponse response = client.post(request);
//            return true;
//        } catch (Exception e) {
//            trackingAction("ERROR SYSTEM", estraiEccezione(e));
//        }
//        return false;
//    }
//    public static void main(String[] args) {
//        Db_OTP db = new Db_OTP();
//        String msg = db.getSMS(bando, 4);
//        db.closeDB();
//        sendSMS2022("3286137172", msg);
//    }

//    public static boolean sendSMS2021(String cell, String msg) {
//        try {
//            Db_Bando db1 = new Db_Bando();
//            String skebbyuser = db1.getPath("skebbyuser");
//            String skebbyPwd = db1.getPath("skebbyPwd");
//            String skebbyURL = db1.getPath("skebbyURL");
//            db1.closeDB();
//
//            String[] authKeys = login(skebbyuser, skebbyPwd, skebbyURL);
//            SendSMSRequest sendSMS = new SendSMSRequest();
//            sendSMS.setMessage(msg);
//            sendSMS.setMessageType(MESSAGE_MEDIUM_QUALITY);
//            sendSMS.addRecipient("+39" + cell);
//
//            boolean es = sendSMS(authKeys, sendSMS, skebbyURL);
//            return es;
//        } catch (Exception e) {
//            trackingAction("ERROR SYSTEM", estraiEccezione(e));
//        }
//        return false;
//    }

    public static boolean sendSMS2022(String cell, String msg) {
        try {
            Db_Bando dbb = new Db_Bando();
            String mailjet_smstoken = dbb.getPath("mailjet_smstoken");
            String mailjet_smsname = dbb.getPath("mailjet_smsname");
            dbb.closeDB();

            ClientOptions options = builder().bearerAccessToken(mailjet_smstoken).build();
            MailjetClient client = new MailjetClient(options);
            MailjetRequest request = new MailjetRequest(SmsSend.resource)
                    .property(SmsSend.FROM, mailjet_smsname)
                    .property(SmsSend.TO, "+39" + cell)
                    .property(SmsSend.TEXT, msg);
            MailjetResponse response = client.post(request);
            if (response.getStatus() == 200) {
                return true;
            }
            trackingAction("ERROR SYSTEM", "sendSMS2022: " + response.getStatus() + " - " + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static String[] login(String username, String password, String BASEURL) throws IOException {
//        String link = BASEURL + "/login?username=" + username + "&password=" + password;
//        URL url = new URL(link);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        if (conn.getResponseCode() != 200) {
//            throw new RuntimeException(link + " - Failed : HTTP error code : " + conn.getResponseCode());
//        }
//        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String response = "";
//        String output;
//        while ((output = br.readLine()) != null) {
//            response += output;
//        }
//        conn.disconnect();
//        String[] parts = response.split(";");
//        return parts;
//    }

//    private static boolean sendSMS(String[] authKeys, SendSMSRequest sendSMS, String BASEURL) throws IOException {
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder.create();
//        String link = BASEURL + "/sms";
//        URL url = new URL(link);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestProperty("user_key", authKeys[0]);
//        conn.setRequestProperty("Session_key", authKeys[1]);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Accept", "application/json");
//        conn.setRequestProperty("Content-type", "application/json");
//        conn.setDoOutput(true);
//        String payload = gson.toJson(sendSMS);
//        OutputStream os = conn.getOutputStream();
//        os.write(payload.getBytes());
//        os.flush();
//
//        if (conn.getResponseCode() != 201) {
//            throw new RuntimeException(link + " - Failed : HTTP error code : " + conn.getResponseCode());
//        }
//        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String response = "";
//        String output;
//        while ((output = br.readLine()) != null) {
//            response += output;
//        }
//        conn.disconnect();
//        SendSMSResponse responseObj = gson.fromJson(response, SendSMSResponse.class);
//        return responseObj.isValid();
//    }
//
//    public static int checkSMS() {
//        try {
//            Db_Bando db1 = new Db_Bando();
//            String skebbyuser = db1.getPath("skebbyuser");
//            String skebbyPwd = db1.getPath("skebbyPwd");
//            String skebbyURL = db1.getPath("skebbyURL");
//            db1.closeDB();
//
//            String[] authKeys = login(skebbyuser, skebbyPwd, skebbyURL);
//            URL url = new URL(skebbyURL + "/status");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestProperty("user_key", authKeys[0]);
//            conn.setRequestProperty("Session_key", authKeys[1]);
//            conn.setRequestMethod("GET");
//            if (conn.getResponseCode() != 200) {
//                String error = "";
//                String output;
//                BufferedReader errorbuffer = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
//                while ((output = errorbuffer.readLine()) != null) {
//                    error += output;
//                }
//                trackingAction("ERROR SYSTEM", "checkSMS - Error! HTTP error code : " + conn.getResponseCode() + ", Body message : " + error);
//                conn.disconnect();
//                return 0;
//            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String response = "";
//            String output;
//            while ((output = br.readLine()) != null) {
//                response += output;
//            }
//            conn.disconnect();
//            JSONObject jsonObject = new JSONObject(response);
//            JSONArray datesArray = jsonObject.getJSONArray("sms");
//            for (int i = 0; i < datesArray.length(); i++) {
//                JSONObject datesObject = datesArray.getJSONObject(i);
//                if (datesObject.getString("type").equalsIgnoreCase(MESSAGE_MEDIUM_QUALITY)) {
//                    return (datesObject.getInt("quantity"));
//                }
//            }
//        } catch (Exception e) {
//            trackingAction("ERROR SYSTEM", estraiEccezione(e));
//        }
//        return 0;
//    }

}

//class SendSMSRequest {
//
//    /**
//     * The message body
//     */
//    private String message;
//
//    /**
//     * The message type
//     */
//    private String message_type = MESSAGE_MEDIUM_QUALITY;
//
//    /**
//     * Should the API return the remaining credits?
//     */
//    private boolean returnCredits = false;
//
//    /**
//     * The list of recipients
//     */
//    private List<String> recipient = new ArrayList<>();
//
//    /**
//     * The sender Alias (TPOA)
//     */
//    private String sender = null;
//
//    /**
//     * Postpone the SMS message sending to the specified date
//     */
//    private Date scheduled_delivery_time = null;
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getMessageType() {
//        return message_type;
//    }
//
//    public void setMessageType(String messageType) {
//        this.message_type = messageType;
//    }
//
//    public boolean isReturnCredits() {
//        return returnCredits;
//    }
//
//    public void setReturnCredits(boolean returnCredits) {
//        this.returnCredits = returnCredits;
//    }
//
//    public List<String> getRecipient() {
//        return recipient;
//    }
//
//    public String getSender() {
//        return sender;
//    }
//
//    public void setSender(String sender) {
//        this.sender = sender;
//    }
//
//    public Date getScheduledDeliveryTime() {
//        return scheduled_delivery_time;
//    }
//
//    public void setScheduledDeliveryTime(Date scheduled_delivery_time) {
//        this.scheduled_delivery_time = scheduled_delivery_time;
//    }
//
//    public void addRecipient(String recipient) {
//        this.recipient.add(recipient);
//    }
//}
//
///**
// * This class represents the API Response. It is automatically created starting
// * from the JSON object returned by the server, using GSon
// */
//class SendSMSResponse {
//
//    private String result;
//    private String order_id;
//    private int total_sent;
//    private int remaining_credits;
//    private String internal_order_id;
//
//    public String getResult() {
//        return result;
//    }
//
//    public String getOrderId() {
//        return order_id;
//    }
//
//    public int getTotalSent() {
//        return total_sent;
//    }
//
//    public int getRemainingCredits() {
//        return remaining_credits;
//    }
//
//    public String getInternalOrderId() {
//        return internal_order_id;
//    }
//
//    public boolean isValid() {
//        return "OK".equals(result);
//    }
//}
