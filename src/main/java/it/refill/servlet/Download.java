/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import it.refill.action.ActionB;
import static it.refill.action.ActionB.getAllegatoB1;
import static it.refill.action.ActionB.getConvenzioneROMA;
import static it.refill.action.ActionB.getPath;
import static it.refill.action.ActionB.getPathConvenzioni;
import static it.refill.action.ActionB.listaDocUserBandoDocenti;
import static it.refill.action.ActionB.listaDocuserbando;
import static it.refill.action.ActionB.preparefilefordownload;
import static it.refill.action.ActionB.trackingAction;
import static it.refill.action.Pdf_new.allegatoA_fase1;
import static it.refill.action.Pdf_new.allegatoB1;
import static it.refill.action.Pdf_new.allegatoB_fase1;
import static it.refill.action.Pdf_new.allegatoC;
import static it.refill.action.Pdf_new.allegatoC_mod1;
import static it.refill.action.Pdf_new.allegatoC_mod2;
import static it.refill.action.Pdf_new.allegatoD_fase1;
import it.refill.db.Db_Bando;
import it.refill.entity.AllegatoB1;
import it.refill.entity.DocumentiDocente;
import it.refill.entity.Docuserbandi;
import it.refill.entity.FileDownload;
import static it.refill.util.Utility.createDir;
import static it.refill.util.Utility.estraiEccezione;
import static it.refill.util.Utility.getRequestValue;
import static it.refill.util.Utility.parseIntR;
import static it.refill.util.Utility.redirect;
import static it.refill.util.Utility.zipListFiles;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import static java.lang.String.format;
import static java.nio.file.Files.probeContentType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import org.joda.time.DateTime;

/**
 *
 * @author raffaele
 */
@SuppressWarnings("serial")
public class Download extends HttpServlet {

    protected void guidabando(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String b64 = getPath("pdf.guida");
        if (b64 != null) {
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", "Manuale Candidato.pdf");
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(b64));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void guidaConvenzioni(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String b64 = getPath("pdf.guidaconvenzione");
        if (b64 != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File Manuale Gestione Convenzioni");
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", "Manuale Gestione Convenzioni.pdf");
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(b64));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void avvisobando(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String b64 = getPath("pdf.avviso");
        if (b64 != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File Avviso");
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", "Avviso YISU 2122.pdf");
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(b64));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void viewFileConvenzioniRUP(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String codicedoc = request.getParameter("tipologia");
        String username = request.getParameter("userdoc");
        String b64 = getPathConvenzioni(username, codicedoc);
        if (b64 != null) {
            FileDownload fw = preparefilefordownload(b64);
            if (fw != null) {
                trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
                String mimeType = fw.getMimeType();
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);

                String headerKey = "Content-Disposition";
                String headerValue = format("attachment; filename=\"%s\"", fw.getName());
                response.setHeader(headerKey, headerValue);
                response.setContentLength(-1);
                try (OutputStream outStream = response.getOutputStream()) {
                    outStream.write(decodeBase64(fw.getContent()));
                }
            } else {
                redirect(request, response, "page_fnf.html");
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void viewFileConvenzioni(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getSession().getAttribute("username").toString();
        String codicedoc = getRequestValue(request, "codicedoc");
        String b64 = getPathConvenzioni(username, codicedoc);
        if (b64 != null) {
            FileDownload fw = preparefilefordownload(b64);
            if (fw != null) {
                trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
                String mimeType = fw.getMimeType();
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                String headerKey = "Content-Disposition";
                String headerValue = format("attachment; filename=\"%s\"", fw.getName());
                response.setHeader(headerKey, headerValue);
                response.setContentLength(-1);
                try (OutputStream outStream = response.getOutputStream()) {
                    outStream.write(decodeBase64(fw.getContent()));
                }
            } else {
                redirect(request, response, "page_fnf.html");
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    //download docmento del bando
    protected void docbando(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tipodoc = request.getParameter("tipodoc");
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String username = request.getSession().getAttribute("username").toString();

        Db_Bando dbb = new Db_Bando();
        String b64 = dbb.getPathDocUserBando(bandorif, username, tipodoc);
        dbb.closeDB();

        if (b64 != null) {
            FileDownload fw = preparefilefordownload(b64);
            if (fw != null) {
                trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
                String mimeType = fw.getMimeType();
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                String headerKey = "Content-Disposition";
                String headerValue = format("attachment; filename=\"%s\"", fw.getName());
                response.setHeader(headerKey, headerValue);
                response.setContentLength(-1);
                try (OutputStream outStream = response.getOutputStream()) {
                    outStream.write(decodeBase64(fw.getContent()));
                }
            } else {
                redirect(request, response, "page_fnf.html");
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void docbandoconsrup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tipodoc = request.getParameter("tipodoc");
        String tipologia = request.getParameter("tipologia");
        String username = request.getParameter("userdoc");
        String note = request.getParameter("note");
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String path = request.getParameter("path");
        String filePath = "";

        if (path == null || path.trim().equals("")) {
            Db_Bando dbb = new Db_Bando();
            filePath = dbb.getPathDocUserBando(bandorif, username, tipodoc, tipologia, note);
            dbb.closeDB();
        } else {
            filePath = path;
        }

        FileDownload fw = preparefilefordownload(filePath);

        if (fw != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
            String mimeType = fw.getMimeType();
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", fw.getName());
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(fw.getContent()));
//                outStream.flush();
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }

    }

    protected void docbandoconsConv(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = getRequestValue(request, "userdoc");
        if (username.equals("")) {
            username = request.getSession().getAttribute("username").toString();
        }
        String path = getRequestValue(request, "path");

        String filePath = "";
        if (path == null || path.trim().equals("")) {
            filePath = getConvenzioneROMA(username);
        } else {
            filePath = path;
        }

        FileDownload fw = preparefilefordownload(filePath);

        if (fw != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
            String mimeType = fw.getMimeType();
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", fw.getName());
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(fw.getContent()));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void docbandoconsrupB1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = getRequestValue(request, "username");
        String cod = getRequestValue(request, "cod");
        int indice = parseIntR(getRequestValue(request, "indice"));

        ArrayList<DocumentiDocente> dubd = ActionB.listaDocUserBandoDocenti(username, true);

        String content = null;
        if (cod.equals("ALB1")) {
            content = dubd.get(indice).getB1();
        } else if (cod.equals("CV")) {
            content = dubd.get(indice).getCv();
        } else if (cod.equals("DR")) {
            content = dubd.get(indice).getDr();
        }

        FileDownload fw = preparefilefordownload(content);

        if (fw != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
            String mimeType = fw.getMimeType();
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", fw.getName());
            response.setHeader(headerKey, headerValue);
            response.setContentLength(-1);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(fw.getContent()));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }

    }

    protected void modellodoc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession().getAttribute("username") == null) {
            redirect(request, response, "home.jsp");
        } else {
            String tipodoc = request.getParameter("tipodoc");
            String username = request.getSession().getAttribute("username").toString();
            String bandorif = request.getSession().getAttribute("bandorif").toString();
            Db_Bando dbb = new Db_Bando();
            String filePath = dbb.getPathDocModello(bandorif, tipodoc);
            // PER SVILUPPO
//            filePath = "C:\\bandoba0h8\\Allegato_C_Dichiarazione_disponibilita.pdf";
            // PER PRODUZIONE
//            filePath = "/mnt/bando/bandoba0h8/Allegato_C_Dichiarazione_disponibilita.pdf";
            File downloadFile = null;
            dbb.closeDB();
            switch (tipodoc) {
                //downloadFile = domanda(pathtemp, new File(filePath), bandorif, username);
                case "DONL":
                    break;
                case "DONLA": {
                    File f1 = allegatoA_fase1(username);
                    if (f1 != null) {
                        downloadFile = f1;
                    }
                    break;
                }
                case "DONLB": {
                    File f1 = allegatoB_fase1(username);
                    if (f1 != null) {
                        downloadFile = f1;
                    }
                    break;
                }
                case "DONLD": {
                    File f1 = allegatoD_fase1(username);
                    if (f1 != null) {
                        downloadFile = f1;
                    }
                    break;
                }
                default:
                    downloadFile = new File(filePath);
                    break;
            }

            if (downloadFile != null && downloadFile.exists()) {
                trackingAction(request.getSession().getAttribute("username").toString(), "Download File (con modello) " + downloadFile.getName());
                OutputStream outStream;
                try (FileInputStream inStream = new FileInputStream(downloadFile)) {
                    String mimeType = probeContentType(downloadFile.toPath());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream";
                    }
                    response.setContentType(mimeType);
                    String headerKey = "Content-Disposition";
                    String headerValue = format("attachment; filename=\"%s\"", downloadFile.getName());
                    response.setHeader(headerKey, headerValue);
                    response.setContentLength(-1);
                    outStream = response.getOutputStream();
                    byte[] buffer = new byte[4096 * 4096];
                    int bytesRead = -1;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                outStream.close();
            } else {
                redirect(request, response, "page_fnf.html");
            }
        }
    }

    protected void zipdomanda(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bandorif = request.getSession().getAttribute("bandorif").toString();
        String username = request.getParameter("userdoc");

        ArrayList<Docuserbandi> lid1 = listaDocuserbando(bandorif, username);
        ArrayList<DocumentiDocente> al = listaDocUserBandoDocenti(username, true);

        ArrayList<FileDownload> ff = new ArrayList<>();

        for (int i = 0; i < lid1.size(); i++) {
            if (!lid1.get(i).getPath().equals("-")) {
                ff.add(preparefilefordownload(lid1.get(i).getPath()));
            }
        }
        for (int i = 0; i < al.size(); i++) {
            ff.add(preparefilefordownload(al.get(i).getB1()));
            ff.add(preparefilefordownload(al.get(i).getCv()));
            ff.add(preparefilefordownload(al.get(i).getDr()));
        }

        Db_Bando dbb = new Db_Bando();
        String pathtemp = dbb.getPath("pathtemp");
        dbb.closeDB();
        createDir(pathtemp);
        String name = bandorif + username + new DateTime().toString("yyyyMMddHHmmssSSS") + ".zip";
        File zipout = new File(pathtemp + name);
        if (zipListFiles(ff, zipout)) {
            OutputStream outStream;
            try (FileInputStream inStream = new FileInputStream(zipout)) {
                String mimeType = probeContentType(zipout.toPath());
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                String headerKey = "Content-Disposition";
                String headerValue = format("attachment; filename=\"%s\"", zipout.getName());
                response.setHeader(headerKey, headerValue);
                response.setContentLength(-1);
                outStream = response.getOutputStream();
                byte[] buffer = new byte[4096 * 4096];
                int bytesRead = -1;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
            outStream.close();
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void excelcomParziale(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String data_da = request.getParameter("data_da");
            String data_a = request.getParameter("data_a");
            SimpleDateFormat sdf_in = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // creo l'oggetto
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
            Db_Bando db = new Db_Bando();
            String path = db.expexcel_ricerca(dt_da, dt_a);
            db.closeDB();
            File downloadFile = new File(path);
            if (downloadFile.exists()) {
                OutputStream outStream;
                try (FileInputStream inStream = new FileInputStream(downloadFile)) {
                    String mimeType = probeContentType(downloadFile.toPath());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream";
                    }
                    response.setContentType(mimeType);
                    String headerKey = "Content-Disposition";
                    String headerValue = format("attachment; filename=\"%s\"", downloadFile.getName());
                    response.setHeader(headerKey, headerValue);
                    response.setContentLength(-1);
                    outStream = response.getOutputStream();
                    byte[] buffer = new byte[4096 * 4096];
                    int bytesRead = -1;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                outStream.close();
            } else {
                redirect(request, response, "page_fnf.html");
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
    }

    protected void downconv(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getSession().getAttribute("username").toString();
        File downloadFile = null;
        String var = request.getParameter("ctrl");
        Db_Bando dbb = new Db_Bando();
        if (var.equals("1")) {
            File f1 = allegatoC(username);
            if (f1 != null) {
                downloadFile = f1;
            }
        }
        if (var.equals("2")) {
            File f1 = allegatoC_mod1(username);
            if (f1 != null) {
                downloadFile = f1;
            }
        }
        if (var.equals("3")) {

            File f1 = allegatoC_mod2(username);
            if (f1 != null) {
                downloadFile = f1;
            }
        }
        dbb.closeDB();

        if (downloadFile != null) {
            OutputStream outStream;
            try (FileInputStream inStream = new FileInputStream(downloadFile)) {
                String mimeType = probeContentType(downloadFile.toPath());
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                String headerKey = "Content-Disposition";
                String headerValue = format("attachment; filename=\"%s\"", downloadFile.getName());
                response.setHeader(headerKey, headerValue);
                response.setContentLength(-1);
                outStream = response.getOutputStream();
                byte[] buffer = new byte[4096 * 4096];
                int bytesRead = -1;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
            outStream.close();
        } else {
            redirect(request, response, "page_fnf.html");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {

        try {

            response.setContentType("text/html;charset=UTF-8");
            String action = request.getParameter("action");
            if (action.equals("guida")) {
                guidabando(request, response);
            }
            if (action.equals("guidaConvenzioni")) {
                guidaConvenzioni(request, response);
            }
            if (action.equals("avviso")) {
                avvisobando(request, response);
            }
            if (request.getSession().getAttribute("username") == null) {
                redirect(request, response, "home.jsp");
            } else {
                if (action.equals("docbando")) {
                    docbando(request, response);
                }
                if (action.equals("viewFileConvenzioniRUP")) {
                    viewFileConvenzioniRUP(request, response);
                }
                if (action.equals("docbandoconsrup")) {
                    docbandoconsrup(request, response);
                }
                if (action.equals("docbandoconsrupB1")) {
                    docbandoconsrupB1(request, response);
                }
                if (action.equals("modellodoc")) {
                    modellodoc(request, response);
                }
                if (action.equals("zipdomanda")) {
                    zipdomanda(request, response);
                }
                if (action.equals("docbandoDocente")) {
                    docbandoDocenti(request, response);
                }
                if (action.equals("docAllegatoB1")) {
                    docAllegatoB1(request, response);
                }
                if (action.equals("excelcomParziale")) {
                    excelcomParziale(request, response);
                }
                if (action.equals("viewFileConvenzioni")) {
                    viewFileConvenzioni(request, response);
                }
                if (action.equals("docbandoconsConv")) {
                    docbandoconsConv(request, response);
                }
                if (action.equals("downconv")) {
                    downconv(request, response);
                }
            }
        } catch (ServletException | IOException ex) {
            trackingAction("service", "Error:processRequestDownload: " + ex.getMessage());
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

    //download docmento del bando
    protected void docbandoDocenti(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tipodoc = request.getParameter("tipodoc");
        String indice = request.getParameter("indice");
        String username = request.getSession().getAttribute("username").toString();
        String pathing = "";
        ArrayList<AllegatoB1> al = getAllegatoB1(username, indice);
        for (int i = 0; i < al.size(); i++) {

            if (tipodoc.equals("cv")) {
                pathing = al.get(i).getAllegatocv();
            } else if (tipodoc.equals("b1")) {
                pathing = al.get(i).getAllegatob1();
            } else if (tipodoc.equals("ci")) {
                pathing = al.get(i).getAllegatodr();
            }
        }

        //new
        FileDownload fw = preparefilefordownload(pathing);

        if (fw != null) {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + fw.getName());
            String mimeType = fw.getMimeType();
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String headerKey = "Content-Disposition";
            String headerValue = format("attachment; filename=\"%s\"", fw.getName());
            response.setHeader(headerKey, headerValue);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(decodeBase64(fw.getContent()));
            }
        } else {
            redirect(request, response, "page_fnf.html");
        }

    }

    protected void docAllegatoB1(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //     Utility.printRequest(request);
        String username = request.getSession().getAttribute("username").toString();
        String iddocente = request.getParameter("iddocente");
//        String pathing = "";
//        String nomeDocente = request.getParameter("nomeDocente");
        File downloadFile = allegatoB1(username, iddocente);
        try {
            trackingAction(request.getSession().getAttribute("username").toString(), "Download File " + downloadFile.getName());
            if (downloadFile != null && downloadFile.exists()) {
                trackingAction(request.getSession().getAttribute("username").toString(), "Download File (con modello) " + downloadFile.getName());
                OutputStream outStream;
                try (FileInputStream inStream = new FileInputStream(downloadFile)) {
                    String mimeType = probeContentType(downloadFile.toPath());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream";
                    }
                    response.setContentType(mimeType);
                    String headerKey = "Content-Disposition";
                    String headerValue = format("attachment; filename=\"%s\"", downloadFile.getName());
                    response.setHeader(headerKey, headerValue);
                    outStream = response.getOutputStream();
                    byte[] buffer = new byte[4096 * 4096];
                    int bytesRead = -1;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                outStream.close();
            } else {
                redirect(request, response, "page_fnf.html");
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
    }

}
