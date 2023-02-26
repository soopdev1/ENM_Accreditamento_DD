/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import static it.refill.action.ActionB.trackingAction;
import it.refill.action.Pdf_new;
import static it.refill.action.Pdf_new.extractSignatureInformation_P7M;
import static it.refill.action.Pdf_new.verificaQR;
import it.refill.db.Db_Bando;
import it.refill.entity.SignedDoc;
import static it.refill.util.Utility.estraiEccezione;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

/**
 *
 * @author Administrator
 */
public class checkfile {

    public static void main(String[] args) {
        try {

            String username = "SFATTO4303";
            String tipodoc = "DONLD";
            File nomefile = new File("C:\\Users\\Administrator\\Desktop\\da caricare\\SFATTO4303030220221633124.D_pdfA.pdf.p7m");

            Db_Bando dbb = new Db_Bando();
            String cfuser = dbb.getCF_user(username);
            SignedDoc dc = extractSignatureInformation_P7M(readFileToByteArray(nomefile));
            if (dc.isValido()) {
                System.out.println("test.checkfile.main(a) "+dc.getCodicefiscale());
                System.out.println("test.checkfile.main(b) "+cfuser.toUpperCase());
                if (!dc.getCodicefiscale().toUpperCase().contains(cfuser.toUpperCase())) {
                    System.out.println("ERRORE NELL'UPLOAD 1- " + tipodoc + " -- CF NON CONFORME");
                } else {
                    byte[] content = dc.getContenuto();
                    if (content == null) {
                        System.out.println("ERRORE NELL'UPLOAD 2- " + tipodoc + " -- CF NON CONFORME");
                    } else {
                        String esitoqr = verificaQR(tipodoc, username, content);
                        if (!esitoqr.equals("OK")) {
                            System.out.println("test.checkfile.main() " + esitoqr);
                        }
                    }
                }
            } else {
                trackingAction("ERROR SYSTEM", "test.checkfile.main() " + dc.getErrore());
            }
            dbb.closeDB();
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
    }
}
