/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import com.google.gson.JsonObject;
import it.refill.action.ActionB;
import static it.refill.action.ActionB.getDomandeComplete;
import static it.refill.action.ActionB.getRagioneSociale;
import static it.refill.action.ActionB.trackingAction;
import it.refill.action.Constant;
import static it.refill.action.Constant.checkRegistrazione;
import static it.refill.action.Constant.datainizio;
import static it.refill.action.Constant.timestampITA;
import static it.refill.action.Constant.timestampSQL;
import it.refill.db.Db_Bando;
import it.refill.entity.Ateco;
import it.refill.entity.Comuni_rc;
import it.refill.entity.Domandecomplete;
import it.refill.entity.UserBando;
import it.refill.entity.UserValoriReg;
import static it.refill.otp.OTP.generaOTP;
import static it.refill.util.GoogleRecaptcha.isValid;
import static it.refill.util.SendMailJet.sendMail;
import it.refill.util.Utility;
import static it.refill.util.Utility.convMd5;
import static it.refill.util.Utility.estraiEccezione;
import static it.refill.util.Utility.formatStringtoStringDate;
import static it.refill.util.Utility.formatUTFtoLatin;
import static it.refill.util.Utility.getRequestValue;
import static it.refill.util.Utility.randomP;
import static it.refill.util.Utility.redirect;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replace;
import org.joda.time.DateTime;

/**
 *
 * @author fplacanica
 */
@SuppressWarnings("serial")
public class Query extends HttpServlet {

    private static String correggi(String ing) {
        if (ing != null) {
            ing = ing.replaceAll("\\\\", "");
            ing = ing.replaceAll("\n", "");
            ing = ing.replaceAll("\r", "");
            ing = ing.replaceAll("\t", "");
            ing = ing.replaceAll("'", "\'");
            ing = ing.replaceAll("“", "\'");
            ing = ing.replaceAll("‘", "\'");
            ing = ing.replaceAll("”", "\'");
            return ing.replaceAll("\"", "\'");
        } else {
            return "-";
        }
    }

    protected void query1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/plain; charset=ISO-8859-1");
            response.setCharacterEncoding("ISO-8859-1");

            String data_da = getRequestValue(request, "data_da");
            String data_a = getRequestValue(request, "data_a");
            String stato = getRequestValue(request, "stato");

//            if (mobile) {
//                target = "target='_blank'";
//            }
            SimpleDateFormat sdf_in = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // creo l'oggetto
//            Date da = sdf_in.parse(data_da);
            String dt_a = "";
            String dt_da = "";
            if (!data_a.trim().equals("")) {
                Date a = sdf_in.parse(data_a);
                dt_a = sdf.format(a);
            }
            if (!data_da.trim().equals("")) {
                Date da = sdf_in.parse(data_da);
                dt_da = sdf.format(da);
            }
//            dt_da = sdf.format(da);


            ArrayList<Domandecomplete> lista = getDomandeComplete(dt_da, dt_a, stato);

//            ArrayList<Domandecomplete> lista = getDomandeComplete(dt_da, dt_a, stato);
            try (PrintWriter out = response.getWriter()) {
                String inizio = "{ \"aaData\": [";
                String fine = "] }";
                String valore = "";
                if (!lista.isEmpty()) {
                    for (int i = 0; i < lista.size(); i++) {
                        String azioni = "";

                        String statodomanda = lista.get(i).formatStatoDomanda();
                        
//                        boolean convenzionedainviare = db.countDocumentConvenzioni(lista.get(i).getUsername()) == 3;
//                        boolean convenzioneinviataROMA =  db.getInvioEmailROMA(lista.get(i).getUsername()).equals("1");
//                        boolean convenzionecaricatacontrofirmata = !db.getConvenzioneROMA(lista.get(i).getUsername()).trim().equals("");

                        if (lista.get(i).getStatoDomanda().equals("S")) {
                            azioni = "<li><a class='btn btn-sm white popovers' data-toggle='modal' onclick='return setAccettaDomanda(&#34" + lista.get(i).getUsername() + "&#34)' href='#accettamodal' data-trigger='hover' data-placement='top' data-content='Accetta'><i class='fa fa-check-circle'></i> Accetta domanda</a></li>"
                                    + "<li><a class='btn btn-sm white popovers' data-toggle='modal' onclick='return setRigettaDomanda(&#34" + lista.get(i).getUsername() + "&#34)' href='#scartamodal2' data-trigger='hover' data-placement='top' data-content='Rigetta'><i class='fa fa-times-circle'></i>  Rigetta domanda</a></li>";
                        }
                        if (lista.get(i).isConvenzionedainviare()) {
                            azioni = "<li><a class='btn btn-sm white popovers fancyBoxRafreload' data-toggle='modal' href='bando_conv.jsp?userdoc=" + lista.get(i).getUsername() + "' data-trigger='hover' data-placement='top' data-content='Visualizza'><i class='fa fa-eye'></i> Visualizza Convenzione</a></li>";

                        }
                        if (lista.get(i).isConvenzioneinviataROMA()) {
                            if (!lista.get(i).isConvenzionecaricatacontrofirmata()) {
                                azioni = azioni + "<li><a class='btn btn-sm white popovers' data-toggle='modal' onclick='return setCaricaDocumento(&#34" + lista.get(i).getUsername() + "&#34)' href='#caricadocumento' data-trigger='hover' data-placement='top' data-content='Carica'><i class='fa fa-check-circle'></i> Carica Convenzione controfirmata</a></li>";
                            }
                        }
                        
                        
                        
                        
                        azioni += "<li><a class='btn btn-sm white fancyBoxRaf' " + "href='bando_updoc_rup.jsp?userdoc=" + lista.get(i).getUsername() + "'><i class='fa fa-upload'></i> Carica Allegati Domanda</a></li>";
                        

                        String az2 = "<div class='clearfix'><div class='task-config'>"
                                + "<div class='task-config-btn btn-group' style='text-align:left;'>"
                                + "<a class='btn btn-default btn-xs' href='#' data-toggle='dropdown' data-hover='dropdown' data-close-others='true'><i class='fa fa-cog'>"
                                + "</i><i class='fa fa-angle-down'></i></a><ul class='dropdown-menu pull-right'>"
                                + "<li><a href='bando_visdocall.jsp?userdoc=" + lista.get(i).getUsername() + "' "
                                + "class='btn btn-sm white popovers fancyBoxRaf' container='body' data-trigger='hover' data-container='body' data-placement='top' "
                                + "data-content='Visualizza Documenti'>"
                                + "<i class='fa fa-file'></i> Visualizza documenti</a></li>"
                                + azioni
                                + "</ul></div></div></div>";
                        //fancyBoxSimone
                        valore = valore + " [ \""
                                + correggi(lista.get(i).getId()) + "\", \""
                                + correggi(lista.get(i).getRagsoc()).toUpperCase() + "\", \""
                                + correggi(lista.get(i).getPiva()).toUpperCase() + "\", \""
                                + correggi(lista.get(i).getCodfissog()).toUpperCase() + "\", \""
                                + formatStringtoStringDate(lista.get(i).getDatainvio(), timestampSQL, timestampITA, false) + "\", \""
                                + statodomanda + "\", \""
                                + correggi(lista.get(i).getProtocollo()).toUpperCase() + "\", \""
                                + correggi(lista.get(i).getDecreto()).toUpperCase() + "\", \""
                                + az2 + "\"],";
                    }
                    String x = inizio + valore.substring(0, valore.length() - 1) + fine;
                    out.print(x);
                } else {
                    out.print(inizio + fine);
                }
            }
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
    }

    protected void comuninazioni(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String q = request.getParameter("q");
        Db_Bando db = new Db_Bando();
        ArrayList<Comuni_rc> result = db.query_comuninazioni_rc(q);
        db.closeDB();
        try (PrintWriter out = response.getWriter()) {
            String inizio = "{ \"items\": [ ";
            String fine = "]}";
            String valore = "";
            if (result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    valore = valore + "{"
                            + "      \"id\": \"" + result.get(i).getId() + "\","
                            + "      \"name\": \"" + formatUTFtoLatin(result.get(i).getNome()) + "\","
                            + "      \"prov\": \"" + formatUTFtoLatin(result.get(i).getCodiceprovincia()) + "\","
                            + "      \"reg\": \"" + formatUTFtoLatin(result.get(i).getCodiceregione()) + "\","
                            + "      \"cf\": \"" + result.get(i).getCodicefiscale() + "\","
                            + "      \"full_name\": \"" + formatUTFtoLatin(result.get(i).getNome()) + "\""
                            + "},";
                }
                String x = inizio + valore.substring(0, valore.length() - 1) + fine;
                out.print(x);
            } else {
                out.print(inizio + fine);
            }
        }

    }

    protected void comune(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String q = request.getParameter("q");
        Db_Bando db = new Db_Bando();
        ArrayList<Comuni_rc> result = db.query_comuni_rc(q);
        db.closeDB();
        try (PrintWriter out = response.getWriter()) {
            String inizio = "{ \"items\": [ ";
            String fine = "]}";
            String valore = "";
            if (result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    valore = valore + "{"
                            + "      \"id\": \"" + result.get(i).getId() + "\","
                            + "      \"name\": \"" + formatUTFtoLatin(result.get(i).getNome()) + "\","
                            + "      \"prov\": \"" + formatUTFtoLatin(result.get(i).getCodiceprovincia()) + "\","
                            + "      \"reg\": \"" + formatUTFtoLatin(result.get(i).getCodiceregione()) + "\","
                            + "      \"full_name\": \"" + formatUTFtoLatin(result.get(i).getNome()) + "\""
                            + "},";
                }
                String x = inizio + valore.substring(0, valore.length() - 1) + fine;
                out.print(x);
            } else {
                out.print(inizio + fine);
            }
        }
    }

    protected void ateco(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String q = request.getParameter("q");
        Db_Bando db = new Db_Bando();
        ArrayList<Ateco> result = db.query_ateco(q);
        db.closeDB();
        try (PrintWriter out = response.getWriter()) {
            String inizio = "{ \"items\": [ ";
            String fine = "]}";
            String valore = "";
            if (result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    valore = valore + "{"
                            + "      \"id\": \"" + result.get(i).getCodice() + "\","
                            + "      \"name\": \"" + formatUTFtoLatin(result.get(i).getDescrizione()) + "\","
                            + "      \"full_name\": \"" + result.get(i).getCodice()
                            + " - " + formatUTFtoLatin(result.get(i).getDescrizione()) + "\""
                            + "},";
                }
                String x = inizio + valore.substring(0, valore.length() - 1) + fine;
                out.print(x);
            } else {
                out.print(inizio + fine);
            }
        }
    }

    protected void verificaneet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String gRecaptchaResponse = getRequestValue(request, "g-recaptcha-response");
        String piva = getRequestValue(request, "piva1");
        String cfuser = getRequestValue(request, "cfuser1");
        String protocollo = getRequestValue(request, "protocollo1");
        String decreto = getRequestValue(request, "decreto1");
        String datadecreto = getRequestValue(request, "datadecreto1");
        boolean presente = false;

        JsonObject resp = new JsonObject();

        boolean verify = isValid(gRecaptchaResponse);

        if (verify) {
            String usernamenew = null;
            Db_Bando neet = new Db_Bando(true);
            try {

                String query = "SELECT username FROM bando_neet_mcn WHERE pivacf = '" + piva + "' AND cf = '" + cfuser + "' AND protocollo = '" + protocollo + "' "
                        + "AND decreto = '" + decreto + "' AND datadecreto='" + datadecreto + "' AND stato_domanda='A'";
                try (Statement st = neet.getConnection().createStatement(); ResultSet rs = st.executeQuery(query)) {
                    if (rs.next()) {
                        presente = true;
                        usernamenew = rs.getString(1);
                    }
                }

                if (presente && usernamenew != null) {

                    String numcell = null;
                    String email = null;

                    String query1 = "SELECT cell,mail FROM users WHERE username='" + usernamenew + "'";

                    try (Statement st1 = neet.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(query1)) {
                        if (rs1.next()) {
                            numcell = rs1.getString(1);
                            email = rs1.getString(2);
                        }
                    }

                    if (numcell != null && email != null) {
                        Db_Bando dbb = new Db_Bando();
                        String linkweb = dbb.getPath("linkweb");
                        String linknohttpweb = remove(linkweb, "https://");
                        linknohttpweb = removeEnd(linknohttpweb, "/");
                        String pass = randomP();
                        String passmd5 = convMd5(pass);
                        String dateReg = new DateTime().toString("yyyy-MM-dd HH:mm:ss");
                        UserBando ub = new UserBando(
                                usernamenew, passmd5, Constant.bando,
                                dateReg, numcell, email, "0", "SI"
                        );

                        boolean ins = dbb.insertUserReg(ub);
                        if (ins) {
                            ArrayList<UserValoriReg> liout = new ArrayList<>();

                            String query2 = "SELECT campo,valore FROM usersvalori WHERE username='" + usernamenew + "'";
                            try (Statement st2 = neet.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(query2)) {
                                while (rs2.next()) {
                                    liout.add(
                                            new UserValoriReg(Constant.bando, usernamenew, rs2.getString("campo"), rs2.getString("valore"), dateReg)
                                    );
                                }
                                liout.add(
                                        new UserValoriReg(Constant.bando, usernamenew, "accreditato", "SI", dateReg)
                                );
                            }

                            int insert = dbb.insertUserRegistration(liout);

                            if (insert > 0) {
                                if (checkRegistrazione()) {

                                    String esitoOTP = generaOTP(Constant.bando, numcell, usernamenew);
                                    if (esitoOTP.equals("SUCCESS")) {
                                        String dest[] = {email};
                                        String mailreg = replace(dbb.getPath("mail.registrazioneutente"), "@username", usernamenew);
                                        mailreg = replace(mailreg, "@password", pass);
                                        mailreg = replace(mailreg, "@linkweb", linkweb);
                                        mailreg = replace(mailreg, "@linknohttpweb", linknohttpweb);

                                        if (!mailreg.equals("-")) {
                                            String mailsender = dbb.getPath("mailsender");
                                            boolean es = sendMail(mailsender, dest, new String[]{}, mailreg, mailsender + " Registrazione");
                                            if (es) {
                                                trackingAction("service", "Registrato nuovo Utente: " + usernamenew);
                                                resp.addProperty("result", true);
                                            } else {
                                                trackingAction("service", "registraUtente - Error: MAIL");
                                                resp.addProperty("result", false);
                                                resp.addProperty("message", "Si &#232; verificato un errore durante l'invio della mail. Controllare l'indirizzo inserito o contattare il supporto tecnico.");
                                            }
                                        } else {
                                            trackingAction("service", "registraUtente - Error: MAIL DB NOT FOUND");
                                            resp.addProperty("result", false);
                                            resp.addProperty("message", "Si &#232; verificato un errore durante l'invio della mail. Controllare l'indirizzo inserito o contattare il supporto tecnico.");
                                        }
                                    } else {
                                        trackingAction("service", "registraUtente - Error: OTP");
                                        resp.addProperty("result", false);
                                        resp.addProperty("message", "Si &#232; verificato un errore durante l'invio del codice OTP. Contattare il supporto tecnico.");
                                    }
                                } else {
                                    String dest[] = {email};
                                    String mailreg = replace(dbb.getPath("mail.registrazioneutente.datainizio"), "@datainizio", datainizio);
                                    mailreg = replace(mailreg, "@linkweb", linkweb);
                                    mailreg = replace(mailreg, "@linknohttpweb", linknohttpweb);
                                    if (!mailreg.equals("-")) {
                                        String mailsender = dbb.getPath("mailsender");
                                        boolean es = sendMail(mailsender, dest, new String[]{}, mailreg, mailsender + " Registrazione");
                                        if (es) {
                                            dbb.cambiaStatoUser(usernamenew, "R");
                                            trackingAction("service", "Registrato nuovo Utente: " + usernamenew);
                                            resp.addProperty("result", true);
                                        } else {
                                            trackingAction("service", "registraUtente - Error: MAIL");
                                            resp.addProperty("result", false);
                                            resp.addProperty("message", "Si &#232; verificato un errore durante l'invio della mail. Controllare l'indirizzo inserito o contattare il supporto tecnico.");
                                        }
                                    } else {
                                        resp.addProperty("result", false);
                                        resp.addProperty("message", "Si &#232; verificato un errore durante l'invio della mail. Controllare l'indirizzo inserito o contattare il supporto tecnico.");
                                        trackingAction("service", "registraUtente - Error: MAIL DB NOT FOUND");
                                    }
                                }
                            } else {
                                resp.addProperty("result", false);
                                resp.addProperty("message", "Errore durante l'inserimento dei dati. Controllare.");
                            }
                        } else {
                            resp.addProperty("result", false);
                            resp.addProperty("message", "Errore durante l'inserimento dei dati. Controllare.");
                        }
                        dbb.closeDB();
                    } else {
                        resp.addProperty("result", false);
                        resp.addProperty("message", "Errore: I dati inseriti non corrispondono a quelli di un soggetto attuatore accreditato. Controllare i dati.");
                    }
                } else {
                    resp.addProperty("result", false);
                    resp.addProperty("message", "Errore: I dati inseriti non corrispondono a quelli di un soggetto attuatore accreditato. Controllare i dati.");
                }
            } catch (Exception e) {
                trackingAction("ERROR SYSTEM", estraiEccezione(e));
                resp = new JsonObject();
                resp.addProperty("result", false);
                resp.addProperty("message", "Errore: I dati inseriti non corrispondono a quelli di un soggetto attuatore accreditato. Controllare i dati.");
            }
            neet.closeDB();
        } else {
            trackingAction("service", "registraUtente - Error: ROBOT GOOGLE");
            resp = new JsonObject();
            resp.addProperty("result", false);
            resp.addProperty("message", "Errore: Captcha Google non verificato, riprovare.");
        }
        response.getWriter().write(resp.toString());
        response.getWriter().flush();
        response.getWriter().close();
    }

    protected void verificaok(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String value = getRequestValue(request, "value");
        List<String> ok = ActionB.elencoaccreditati();
        try (PrintWriter out = response.getWriter()) {
            if (ok.contains(value)) {
                out.print("KO");
            } else {
                out.print("OK");
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = getRequestValue(request, "tipo");
        if (request.getSession().getAttribute("username") == null) {
            switch (type) {
                case "ateco":
                    ateco(request, response);
                    break;
                case "comune":
                    comune(request, response);
                    break;
                case "verificaok":
                    verificaok(request, response);
                    break;
                case "verificaneet":
                    verificaneet(request, response);
                    break;
                default:
                    redirect(request, response, "home.jsp");
                    break;
            }
        } else {
            response.setContentType("text/plain; charset=ISO-8859-1");
            response.setCharacterEncoding("ISO-8859-1");
            switch (type) {
                case "ateco":
                    ateco(request, response);
                    break;
                case "comune":
                    comune(request, response);
                    break;
                case "1":
                    query1(request, response);
                    break;
                case "getRagSoc":
                    getRagSoc(request, response);
                    break;
                case "comuninazioni":
                    comuninazioni(request, response);
                    break;
                default:
                    redirect(request, response, "home.jsp");
                    break;
            }

        }

    }

    protected void getRagSoc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter("user");
        String regSoc = getRagioneSociale(user);
        response.getWriter().print(regSoc);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
