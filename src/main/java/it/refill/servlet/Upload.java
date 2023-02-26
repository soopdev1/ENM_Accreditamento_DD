/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import static it.refill.action.ActionB.domandaAnnullata;
import static it.refill.action.ActionB.getConvenzioneROMA;
import static it.refill.action.ActionB.insertConvenzioneROMA;
import static it.refill.action.ActionB.insertDocumentUserConvenzioni;
import static it.refill.action.ActionB.isPresenzaDocumento;
import static it.refill.action.ActionB.listaDocRichiesti;
import static it.refill.action.ActionB.preparefileforupload;
import static it.refill.action.ActionB.trackingAction;
import static it.refill.action.ActionB.verificaPresenzaConvenzioni;
import it.refill.action.Constant;
import static it.refill.action.Constant.bando;
import static it.refill.action.Pdf_new.extractSignatureInformation_P7M;
import static it.refill.action.Pdf_new.extractSignatureInformation_PDF;
import static it.refill.action.Pdf_new.verificaQR;
import static it.refill.action.Pdf_new.verificaQRNOPDFA;
import it.refill.db.Db_Bando;
import it.refill.entity.Docbandi;
import it.refill.entity.Docuserbandi;
import it.refill.entity.Docuserconvenzioni;
import it.refill.entity.SignedDoc;
import it.refill.otp.Db_OTP;
import static it.refill.otp.Sms.sendSMS2022;
import static it.refill.util.SendMailJet.sendMail;
import static it.refill.util.Utility.checkExtension;
import static it.refill.util.Utility.checkFile;
import static it.refill.util.Utility.createDir;
import static it.refill.util.Utility.estraiEccezione;
import static it.refill.util.Utility.redirect;
import java.io.File;
import static java.io.File.separator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import static org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.substring;
import org.joda.time.DateTime;

/**
 *
 * @author raffaele
 */
@SuppressWarnings("serial")
public class Upload extends HttpServlet {

    //upload rup
    protected void simpleRUP(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Db_Bando dbb = new Db_Bando();
        String pathtemp = dbb.getPath("pathtemp");
        createDir(pathtemp);
        String tipodoc = "RALL";
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String usr = request.getSession().getAttribute("username").toString();
        String username = "";
        DateTime el = new DateTime();
        String dateReg = el.toString("yyyy-MM-dd HH:mm:ss");
        String dateFile = el.toString("yyyyMMddHHmmssSSS");
        boolean isMultipart = isMultipartContent(request);
        File nomefile = null;
        String msg = "0";
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("userdoc")) {
                            username = item.getString();
                        }
                    } else {
                        String fileName = item.getName();
                        if (fileName != null) {
                            if (!tipodoc.equals("")) {
                                if (fileName.lastIndexOf(".") > 0) {
                                    File pathdir = new File(pathtemp + el.toString("yyyyMMdd"));
                                    pathdir.mkdirs();
                                    String estensione = fileName.substring(fileName.lastIndexOf("."));
                                    if (estensione.equalsIgnoreCase(".pdf") || estensione.equalsIgnoreCase(".p7m")) {
                                        String name = bandorif + tipodoc + username + "_R" + dateFile + estensione;
                                        nomefile = new File(pathdir + separator + name);
                                        try {
                                            FileUtils.copyInputStreamToFile(item.getInputStream(), nomefile);
                                        } catch (Exception ex) {
                                            trackingAction(username, estraiEccezione(ex));
                                            msg = "1";
                                            nomefile = null;
                                        }
                                    } else {
                                        msg = "1";
                                        trackingAction(username, "Error:uploadSimple: Estensione non valida");
                                        nomefile = null;
                                    }
                                } else {
                                    msg = "1";
                                    trackingAction(username, "Error:uploadSimple: File senza estensione");
                                    nomefile = null;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                trackingAction(username, estraiEccezione(ex));
                msg = "1";
            }
        }
        if (nomefile != null) {
            String fileb64 = preparefileforupload(nomefile);
            if (fileb64.equals("FILE ERROR")) {
                msg = "2";
            } else {
                Docuserbandi dub = new Docuserbandi(bandorif, username, tipodoc, "1",
                        fileb64,
                        dateReg, null,
                        "CARICATO DA OPERATORE MCN: " + usr);
                boolean es1 = dbb.insertDocUserBando(dub);
                if (!es1) {
                    msg = "3";
                }
            }
        } else {
            msg = "2";
        }
        dbb.closeDB();
        if (msg.equals("0")) {
            trackingAction(usr, "CARICATO DOCUMENTO DA OPERATORE MCN SU DOMANDA CON USERNAME " + username);
            redirect(request, response, "bando_updoc_rup.jsp?userdoc=" + username + "&esito=" + msg);
        } else {
            redirect(request, response, "bando_updoc_rup.jsp?userdoc=" + username + "&esito=" + msg);
        }
    }

    //upload classico doc
    protected void simple(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Db_Bando dbb = new Db_Bando();
        String pathtemp = dbb.getPath("pathtemp");
        createDir(pathtemp);
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String username = request.getSession().getAttribute("username").toString();
        String tipodoc = "";
        String tipologia = "-";
        String note = "-";
        DateTime el = new DateTime();
        String dateReg = el.toString("yyyy-MM-dd HH:mm:ss");
        String dateFile = el.toString("yyyyMMddHHmmssSSS");
        boolean isMultipart = isMultipartContent(request);
        File nomefile = null;
        String msg = "0";

        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("tipodoc")) {
                            tipodoc = item.getString();
                        }
                        if (item.getFieldName().equals("tipologia")) {
                            tipologia = item.getString();
                        }
                        if (item.getFieldName().equals("note")) {
                            note = item.getString();
                        }
                    } else {
                        String fileName = item.getName();
                        if (fileName != null) {
                            if (!tipodoc.equals("")) {
                                if (isPresenzaDocumento(username, tipodoc)) {
                                    redirect(request, response, "bando_index.jsp?esito=kodop");
                                } else {
                                    if (fileName.lastIndexOf(".") > 0) {
                                        File pathdir = new File(pathtemp + el.toString("yyyyMMdd"));
                                        pathdir.mkdirs();
                                        String estensione = substring(fileName, fileName.lastIndexOf("."));
                                        if (checkExtension(fileName)) {
                                            String name = bandorif + tipodoc + username + dateFile + estensione;
                                            nomefile = new File(pathdir + separator + name);
                                            try {
                                                FileUtils.copyInputStreamToFile(item.getInputStream(), nomefile);
                                                if (!checkFile(nomefile)) {
                                                    nomefile = null;
                                                }
                                            } catch (Exception ex) {
                                                trackingAction(username, estraiEccezione(ex));
                                                msg = "1";
                                                nomefile = null;
                                            }
                                        } else {
                                            msg = "1";
                                            trackingAction(username, "ESTENSIONE NON VALIDA");
                                            nomefile = null;
                                        }
                                    } else {
                                        msg = "1";
                                        trackingAction(username, "FILE SENZA ESTENSIONE");
                                        nomefile = null;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                trackingAction(username, estraiEccezione(ex));
                msg = "1";
            }
        }

        boolean check = true;
        boolean isaltro = false;
        if (getExtension(nomefile.getName()).endsWith("p7m")) {
            String cfuser = dbb.getCF_user(username);
            SignedDoc dc = extractSignatureInformation_P7M(readFileToByteArray(nomefile));
            if (dc.isValido()) {
                if (!dc.getCodicefiscale().toUpperCase().contains(cfuser.toUpperCase())) {
                    msg = "RA2"; //
                    trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                    check = false;
                } else {
                    byte[] content = dc.getContenuto();
                    if (content == null) {
                        msg = "RA1"; //
                        trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                        check = false;
                    } else {
                        String esitoqr = verificaQR(tipodoc, username, content);
                        if (!esitoqr.equals("OK")) {
                            msg = "RA1"; //
                            trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                            check = false;
                        }
                    }
                }
            } else {
                msg = "RA1"; //
                trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + dc.getErrore());
                check = false;
            }
        } else {

            if (Constant.test) { //RIMUOVERE
                check = true;
            } else {
                if (tipodoc.equals("DONLA")
                        || tipodoc.equals("DONLB")
                        || tipodoc.equals("DONLD")
                        || tipodoc.equals("CONV")
                        || tipodoc.equals("MOD1")
                        || tipodoc.equals("MOD2")) {//DOCUMENTI CHE DEVONO ESSERE FIRMATI
                    SignedDoc dc = extractSignatureInformation_PDF(readFileToByteArray(nomefile), 
                                new File(nomefile.getPath() + "_tempcheck.pdf"));
                    if (dc.isValido()) {
                        String cfuser = dbb.getCF_user(username);
                        if (!dc.getCodicefiscale().toUpperCase().contains(cfuser.toUpperCase())) {
                            msg = "RA2"; //
                            trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                            check = false;
                        } else {
                            byte[] content = dc.getContenuto();
                            if (content == null) {
                                msg = "RA1"; //
                                trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                                check = false;
                            } else {
                                String esitoqr = verificaQRNOPDFA(tipodoc, username, content);
                                if (!esitoqr.equals("OK")) {
                                    msg = "RA1"; //
                                    trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                                    check = false;
                                }
                            }
                        }
                    } else {
                        msg = "RA1"; //
                        trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + dc.getErrore());
                        check = false;
                    }
                } else {
                    String esitoqr = verificaQR(tipodoc, username, readFileToByteArray(nomefile));
                    if (!esitoqr.equals("OK")) {
                        msg = "RA1"; //
                        trackingAction(username, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                        check = false;
                    }
                }
            }

        }
        if (nomefile != null && check) {
            ArrayList<Docbandi> lid1 = listaDocRichiesti(bandorif);
            Docbandi d = null;
            for (int i = 0; i < lid1.size(); i++) {

                if (lid1.get(i).getCodicedoc().equals(tipodoc)) {
                    d = lid1.get(i);
                }
            }
            if (d != null) {

                String fileb64 = preparefileforupload(nomefile);
                if (fileb64.equals("FILE ERROR")) {
                    msg = "2";
                } else {
                    isaltro = d.getCollegati().equals("1");
                    Docuserbandi dub = new Docuserbandi(bandorif, username, tipodoc, "1",
                            fileb64,
                            dateReg, tipologia, note);
                    boolean es1 = dbb.insertDocUserBando(dub);
                    if (!es1) {
                        msg = "10";
                    }
                }

            } else {
                msg = "2";
            }
        }

//        if (!check) {
//            msg = "10";
//        }
        //1 errore caricamento file
        //2 file non conforme 
        //3 documento già presente o errore inserimento
        dbb.closeDB();
        if (msg.equals("0")) {
            trackingAction(username, "Caricamento documento: " + tipodoc);
            if (isaltro) {
                redirect(request, response, "bando_updoc.jsp?esito=ok&tipodoc=" + tipodoc);
            } else {
                redirect(request, response, "bando_index.jsp?esito=ok");
            }
        } else {
            redirect(request, response, "bando_updoc.jsp?tipodoc=" + tipodoc + "&esito=" + msg);
        }

    }

    //upload classico doc
    protected void simpleConvenzioni(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        Utility.printRequest(request);
//        if (true) {
//            return;
//        }

        String username = "";
        Db_Bando dbb = new Db_Bando();
        String pathtemp = dbb.getPath("pathtemp");
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String data_da = "";
        String data_a = "";
        String ut = request.getSession().getAttribute("username").toString();
        String tipodoc = "";
        DateTime el = new DateTime();
        String dateFile = el.toString("yyyyMMddHHmmssSSS");
        boolean isMultipart = isMultipartContent(request);
        File nomefile = null;
        String msg = "0";
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("tipodoc")) {
                            tipodoc = item.getString();
                        }
                        if (item.getFieldName().equals("username")) {
                            username = item.getString();
                        }
                        if (item.getFieldName().equals("data_da")) {
                            data_da = item.getString();
                        }
                        if (item.getFieldName().equals("data_a")) {
                            data_a = item.getString();
                        }
                    } else {

                        String fileName = item.getName();
                        if (fileName != null) {
                            if (!tipodoc.equals("")) {
                                if (isPresenzaDocumento(username, tipodoc)) {
                                    redirect(request, response, "bando_index.jsp?esito=kodop");
                                } else {
                                    if (fileName.lastIndexOf(".") > 0) {
                                        File pathdir = new File(pathtemp + el.toString("yyyyMMdd"));
                                        pathdir.mkdirs();
                                        String estensione = substring(fileName, fileName.lastIndexOf("."));
                                        if (checkExtension(fileName)) {
                                            String name = bandorif + tipodoc + username + dateFile + estensione;
                                            nomefile = new File(pathdir + separator + name);
                                            try {
                                                FileUtils.copyInputStreamToFile(item.getInputStream(), nomefile);
                                                if (!checkFile(nomefile)) {
                                                    nomefile = null;
                                                }
                                            } catch (Exception ex) {
                                                trackingAction(username, estraiEccezione(ex));
                                                msg = "1";
                                                nomefile = null;
                                            }
                                        } else {
                                            msg = "1";
                                            trackingAction(username, "ESTENSIONE NON VALIDA");
                                            nomefile = null;
                                        }
                                    } else {
                                        msg = "1";
                                        trackingAction(username, "FILE SENZA ESTENSIONE");
                                        nomefile = null;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (FileUploadException ex) {
                trackingAction(ut, estraiEccezione(ex));
                msg = "1";
            }
        }

        dbb.closeDB();

        if (msg.equals("0")) {
            if (getConvenzioneROMA(username).trim().equals("")) {
                trackingAction(username, "Caricamento documento: " + tipodoc);

                String fileb64 = preparefileforupload(nomefile);
                if (fileb64.equals("FILE ERROR")) {
                    response.sendRedirect("bando_visdoc.jsp?esito=ko2&search=1&data_da=" + data_da + "&data_a=" + data_a);
                } else {

                    if (insertConvenzioneROMA(username, fileb64)) {

                        Db_Bando db = new Db_Bando();

                        String emailuser = db.getEmailUtente(username);
                        String numuser = db.getCellUtente(username);
                        String text = db.getPath("mail.invioconvenzione.controfirma");
                        db.closeDB();

                        String[] dest = {emailuser};

                        if (Constant.test
                                && username.toLowerCase().startsWith("lmarin")) { //   RIMUOVERE

                            dest[0] = "raffaele.cosco@faultless.it";
                            numuser = "3286137172";
                        }

                        try {
                            sendMail(
                                    Constant.nomevisual, dest, new String[]{}, text,
                                    Constant.nomevisual + " - Caricamento convenzione controfirmata da Ente Nazionale Microcredito");

                            trackingAction(ut, "Invio email al SA: " + username);

                            //SMS S-A
                            Db_OTP dbo = new Db_OTP();
                            String sms = dbo.getSMS(bando, 5);
                            dbo.closeDB();
                            sendSMS2022(numuser, sms);

                        } catch (Exception e) {
                            trackingAction(username, estraiEccezione(e));
                            trackingAction(ut, "Errore invio email al SA: " + e.getMessage());
                        }
                        response.sendRedirect("bando_visdoc.jsp?esito=ok1&search=1&data_da=" + data_da + "&data_a=" + data_a);
                    } else {
                        response.sendRedirect("bando_visdoc.jsp?esito=ko1&search=1&data_da=" + data_da + "&data_a=" + data_a);
                    }
                }
            } else {
                response.sendRedirect("bando_visdoc.jsp?esito=ko2&search=1&data_da=" + data_da + "&data_a=" + data_a);
            }
        } else {
            response.sendRedirect("bando_visdoc.jsp?esito=ko3&search=1&data_da=" + data_da + "&data_a=" + data_a);
        }

    }

    //upload classico doc
    protected void simpleConvenzioniSA(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Db_Bando dbb = new Db_Bando();
        String pathtemp = dbb.getPath("pathtemp");
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String ut = request.getSession().getAttribute("username").toString();
//        String tipologiaDoc = request.getParameter("tipodoc");
        DateTime el = new DateTime();
        String dateFile = el.toString("yyyyMMddHHmmssSSS");

        String tipodoc = "";
        boolean isMultipart = isMultipartContent(request);
        File nomefile = null;
        String msg = "0";
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("tipodoc")) {
                            tipodoc = item.getString();
                        }
                    } else {
                        String fileName = item.getName();
                        if (fileName != null) {
                            if (fileName.lastIndexOf(".") > 0) {
                                File pathdir = new File(pathtemp + el.toString("yyyyMMdd"));
                                pathdir.mkdirs();
                                String estensione = fileName.substring(fileName.lastIndexOf("."));
                                if (checkExtension(fileName)) {
                                    String name = bandorif + tipodoc + ut + dateFile + estensione;
                                    nomefile = new File(pathdir + separator + name);
                                    try {
                                        FileUtils.copyInputStreamToFile(item.getInputStream(), nomefile);
//                                        item.write(nomefile);
                                        if (!checkFile(nomefile)) {
                                            nomefile = null;
                                        }
                                    } catch (Exception ex) {
                                        msg = "1";
                                        trackingAction(ut, estraiEccezione(ex));
                                        nomefile = null;
                                    }
                                } else {
                                    msg = "1";
                                    trackingAction(ut, "ESTENSIONE NON VALIDA");
                                    nomefile = null;
                                }
                            } else {
                                msg = "1";
                                trackingAction(ut, "FILE SENZA ESTENSIONE");
                                nomefile = null;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                trackingAction(ut, estraiEccezione(ex));
                msg = "1";
            }
        }

        dbb.closeDB();
        if (verificaPresenzaConvenzioni(ut, tipodoc)) {
            response.sendRedirect("bando_index.jsp?esito=kodop");
        } else if (nomefile == null) {
            response.sendRedirect("bando_index.jsp?esito=ko");
        } else {
            boolean check = true;
            if (getExtension(nomefile.getName()).endsWith("p7m")) {
                String cfuser = dbb.getCF_user(ut);
                SignedDoc dc = extractSignatureInformation_P7M(readFileToByteArray(nomefile));
                if (dc.isValido()) {
                    if (!dc.getCodicefiscale().toUpperCase().contains(cfuser.toUpperCase())) {
                        msg = "RA2"; //
                        trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                        check = false;
                    } else {
                        byte[] content = dc.getContenuto();
                        if (content == null) {
                            msg = "RA1"; //
                            trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                            check = false;
                        } else {
                            String esitoqr = verificaQR(tipodoc, ut, content);
                            if (!esitoqr.equals("OK")) {
                                msg = "RA1"; //
                                trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                                check = false;
                            }
                        }
                    }
                } else {
                    msg = "RA1"; //
                    trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + dc.getErrore());
                    check = false;
                }
            } else {
                if (Constant.test) { //RIMUOVERE
                    check = true;
                } else {
                    if (tipodoc.equals("DONLA")
                            || tipodoc.equals("DONLB")
                            || tipodoc.equals("CONV")
                            || tipodoc.equals("MOD1")
                            || tipodoc.equals("MOD2")) { //DOCUMENTI CHE DEVONO ESSERE FIRMATI
                        SignedDoc dc = extractSignatureInformation_PDF(readFileToByteArray(nomefile), 
                                new File(nomefile.getPath() + "_tempcheck.pdf"));
                        if (dc.isValido()) {
                            String cfuser = dbb.getCF(ut);
                            if (!dc.getCodicefiscale().toUpperCase().contains(cfuser.toUpperCase())) {
                                msg = "RA2"; //
                                trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                                check = false;
                            } else {
                                byte[] content = dc.getContenuto();
                                if (content == null) {
                                    msg = "RA1"; //
                                    trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- CF NON CONFORME");
                                    check = false;
                                } else {
                                    String esitoqr = verificaQR(tipodoc, ut, content);
                                    if (!esitoqr.equals("OK")) {
                                        msg = "RA1"; //
                                        trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                                        check = false;
                                    }
                                }
                            }
                        } else {
                            msg = "RA1"; //
                            trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + dc.getErrore());
                            check = false;
                        }
                    } else {
                        String esitoqr = verificaQR(tipodoc, ut, readFileToByteArray(nomefile));
                        if (!esitoqr.equals("OK")) {
                            msg = "RA1"; //
                            trackingAction(ut, "ERRORE NELL'UPLOAD - " + tipodoc + " -- " + esitoqr);
                            check = false;
                        }
                    }
                }
            }

            if (msg.equals("0") && check) {
                trackingAction(ut, "Caricamento documento: " + tipodoc);
                String fileb64 = preparefileforupload(nomefile);
                if (fileb64.equals("FILE ERROR")) {
                    response.sendRedirect("bando_index.jsp?esito=kocarest");
                } else {
                    Docuserconvenzioni docConv = new Docuserconvenzioni();
                    docConv.setCodbando(bandorif);
                    docConv.setUsername(ut);
                    docConv.setCodicedoc(tipodoc);
                    docConv.setPath(fileb64);
                    if (insertDocumentUserConvenzioni(docConv)) {
                        response.sendRedirect("bando_index.jsp?esito=ok");
                    } else {
                        response.sendRedirect("bando_index.jsp?esito=ko");
                    }
                }

            } else {
                response.sendRedirect("bando_index.jsp?esito=kocarest");
            }
        }
    }

    //modifica Faq Fra
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            String action = request.getParameter("action");
            if (action.equals("simpleRUP")) {
                simpleRUP(request, response);
            }
            if (request.getSession().getAttribute("username") == null) {
                redirect(request, response, "home.jsp");
            } else {
                String bandorif = request.getSession().getAttribute("bandorif").toString();
                String username = request.getSession().getAttribute("username").toString();
                if (!domandaAnnullata(bandorif, username)) {
                    if (action.equals("simple")) {
                        simple(request, response);
                    }
                    if (action.equals("simpleConvenzioni")) {
                        simpleConvenzioni(request, response);
                    }
                    if (action.equals("simpleConvenzioniSA")) {
                        simpleConvenzioniSA(request, response);
                    }
                } else {
                    redirect(request, response, "LoginOperations?type=out");
                }
            }
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
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
