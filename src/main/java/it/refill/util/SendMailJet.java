/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.util;

/**
 *
 * @author srotella
 */
import com.mailjet.client.ClientOptions;
import static com.mailjet.client.ClientOptions.builder;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import static com.mailjet.client.resource.Emailv31.MESSAGES;
import static com.mailjet.client.resource.Emailv31.Message.ATTACHMENTS;
import static com.mailjet.client.resource.Emailv31.Message.BCC;
import static com.mailjet.client.resource.Emailv31.Message.CC;
import static com.mailjet.client.resource.Emailv31.Message.FROM;
import static com.mailjet.client.resource.Emailv31.Message.HTMLPART;
import static com.mailjet.client.resource.Emailv31.Message.SUBJECT;
import static com.mailjet.client.resource.Emailv31.Message.TO;
import static com.mailjet.client.resource.Emailv31.resource;
import com.mailjet.client.resource.Statcounters;
import static it.refill.action.ActionB.trackingAction;
import it.refill.db.Db_Bando;
import it.refill.entity.FileDownload;
import static it.refill.util.Utility.estraiEccezione;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import static java.nio.file.Files.probeContentType;
import java.util.List;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.io.IOUtils.toByteArray;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author setasrl
 */
public class SendMailJet {

    public static boolean sendMail(String name, String[] to, String[] cc, String txt, String subject) throws MailjetException {
        return sendMail(name, to, cc, txt, subject, null);

    }

    public static boolean sendMailListAttach(String name, String[] to, String[] cc, String txt, String subject, List<FileDownload> listfile) {

        try {
            MailjetClient client;
            MailjetRequest request;
            MailjetResponse response;

            Db_Bando dbb = new Db_Bando();
            String mailjet_api = dbb.getPath("mailjet_api");
            String mailjet_secret = dbb.getPath("mailjet_secret");
            String mailjet_name = dbb.getPath("mailjet_name");
            dbb.closeDB();

            ClientOptions options = builder()
                    .apiKey(mailjet_api)
                    .apiSecretKey(mailjet_secret)
                    .build();

            client = new MailjetClient(options);

            JSONArray dest = new JSONArray();
            JSONArray ccn = new JSONArray();
            JSONArray ccj = new JSONArray();

            if (to != null) {
                for (String s : to) {
                    dest.put(new JSONObject().put("Email", s)
                            .put("Name", ""));
                }
            }

            if (cc != null) {
                for (String s : cc) {
                    ccj.put(new JSONObject().put("Email", s)
                            .put("Name", ""));
                }
            }

            JSONObject mail = new JSONObject().put(FROM, new JSONObject()
                    .put("Email", mailjet_name)
                    .put("Name", name))
                    .put(TO, dest)
                    .put(CC, ccj)
                    .put(BCC, ccn)
                    .put(SUBJECT, subject)
                    .put(HTMLPART, txt);

            if (!listfile.isEmpty()) {

                JSONArray contentfiles = new JSONArray();

                try {
                    listfile.forEach(fileingresso -> {
                        JSONObject content = new JSONObject()
                                .put("ContentType", fileingresso.getMimeType())
                                .put("Filename", fileingresso.getName())
                                .put("Base64Content", fileingresso.getContent());
                        contentfiles.put(content);
                    });

                    if (!contentfiles.isEmpty()) {
                        mail.put(ATTACHMENTS, contentfiles);
                    }
                } catch (Exception e) {
                    trackingAction("ERROR SYSTEM", estraiEccezione(e));
                }
            }

            request = new MailjetRequest(resource)
                    .property(MESSAGES, new JSONArray()
                            .put(mail));

            response = client.post(request);

            boolean ok = response.getStatus() == 200;

            if (!ok) {
                trackingAction("ERROR SYSTEM", "sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
            }

            return ok;
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;

    }
    
    public static boolean sendMail(String name, String[] to, String[] cc, String txt, String subject, File file) throws MailjetException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String filename = "";
        String content_type = "";
        String b64 = "";

        Db_Bando dbb = new Db_Bando();
        String mailjet_api = dbb.getPath("mailjet_api");
        String mailjet_secret = dbb.getPath("mailjet_secret");
        String mailjet_name = dbb.getPath("mailjet_name");
        dbb.closeDB();

        ClientOptions options = builder()
                .apiKey(mailjet_api)
                .apiSecretKey(mailjet_secret)
                .build();

        client = new MailjetClient(options);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();
        JSONArray ccj = new JSONArray();

        if (to != null) {
            for (String s : to) {
                dest.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        if (cc != null) {
            for (String s : cc) {
                ccj.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        JSONObject mail = new JSONObject().put(FROM, new JSONObject()
                .put("Email", mailjet_name)
                .put("Name", name))
                .put(TO, dest)
                .put(CC, ccj)
                .put(BCC, ccn)
                .put(SUBJECT, subject)
                .put(HTMLPART, txt);

        if (file != null) {
            try {
                filename = file.getName();
                content_type = probeContentType(file.toPath());
                try (InputStream i = new FileInputStream(file)) {
                    b64 = new String(encodeBase64(toByteArray(i)));
                }
            } catch (Exception e) {
                trackingAction("ERROR SYSTEM", estraiEccezione(e));
            }
            mail.put(ATTACHMENTS, new JSONArray()
                    .put(new JSONObject()
                            .put("ContentType", content_type)
                            .put("Filename", filename)
                            .put("Base64Content", b64)));
        }

        request = new MailjetRequest(resource)
                .property(MESSAGES, new JSONArray()
                        .put(mail));

        response = client.post(request);

        boolean ok = response.getStatus() == 200;
        
        if (!ok) {
            trackingAction("ERROR SYSTEM", "sendMail - "+response.getStatus()+" -- "+response.getRawResponseContent()+" --- "+response.getData().toList());
        }

        return ok;

    }
    
    public static int countMail(String today) {
        int sending = 0;
        try {
            Db_Bando dbb = new Db_Bando();
            String mailjet_api = dbb.getPath("mailjet_api");
            String mailjet_secret = dbb.getPath("mailjet_secret");
            dbb.closeDB();
            MailjetClient client;
            ClientOptions options = builder()
                    .apiKey(mailjet_api)
                    .apiSecretKey(mailjet_secret)
                    .build();
            client = new MailjetClient(options);
            MailjetRequest request;
            MailjetResponse response;
            if (today == null) {
                today = new DateTime().withMillisOfDay(0).toString("yyyy-MM-dd'T'HH:mm:ss");
            }
            request = new MailjetRequest(Statcounters.resource).filter(Statcounters.COUNTERSOURCE, "APIKey").filter(Statcounters.COUNTERTIMING, "Message")
                    .filter(Statcounters.COUNTERRESOLUTION, "Day").filter(Statcounters.FROMTS, today).filter(Statcounters.TOTS, today);
            response = client.get(request);
            if (response.getStatus() == 200) {
                try {
                    sending = new JSONArray(response.getData()).getJSONObject(0).getInt("Total");
                } catch (Exception e) {
                    sending = 0;
                }
            } else {
                sending = -1;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
            sending = -1;
        }
        return sending;
    }
}
