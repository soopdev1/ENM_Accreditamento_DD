/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.action;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.forms.PdfAcroForm;
import static com.itextpdf.forms.PdfAcroForm.getAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import static com.itextpdf.kernel.colors.ColorConstants.BLACK;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.signatures.CertificateInfo.X500Name;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;
import static it.refill.action.ActionB.getPath;
import static it.refill.action.ActionB.trackingAction;
import static it.refill.action.Constant.bando;
import it.refill.db.Db_Bando;
import it.refill.entity.AllegatoB;
import it.refill.entity.AllegatoB1_field;
import it.refill.entity.AllegatoC2;
import it.refill.entity.SignedDoc;
import it.refill.entity.UserValoriReg;
import static it.refill.util.Utility.createDir;
import static it.refill.util.Utility.estraiEccezione;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.toRadians;
import static java.lang.System.setProperty;
import java.lang.reflect.Field;
import java.security.Principal;
import static java.security.Security.addProvider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import static java.security.cert.CertificateFactory.getInstance;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Calendar;
import static java.util.Calendar.getInstance;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.lang3.StringUtils.replace;
import org.apache.pdfbox.pdmodel.PDDocument;
import static org.apache.pdfbox.pdmodel.PDDocument.load;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import static org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.xmpbox.XMPMetadata;
import static org.apache.xmpbox.XMPMetadata.createXMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.joda.time.DateTime;

/**
 *
 * @author srotella
 */
public class Pdf_new {

    //ALLEGATO A - BASE
    private static File allegatoA(String username, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            ArrayList<UserValoriReg> listavalori = dbb.listaValoriUserbando(bando, username);
            HashMap<String, String> mappavalori = dbb.getAllegatoA(username);
            String contentb64 = dbb.getPathDocModello(bando, "DONLA");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);

            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".A.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();
                fields.get("privacy0").setValue("On");
//                fields.get("dataconsegna").setValue(dataconsegna.toString("dd/MM/yyyy"));
                try {
                    String comunecciaa = listavalori.stream().filter(re -> (re.getCampo().equals("comunecciaa"))).findAny().get().getValore().toUpperCase();
                    String proccciaa = listavalori.stream().filter(re -> (re.getCampo().equals("proccciaa"))).findAny().get().getValore().toUpperCase();
                    String regccciaa = listavalori.stream().filter(re -> (re.getCampo().equals("regccciaa"))).findAny().get().getValore().toUpperCase();
                    if (!comunecciaa.equals("") && !proccciaa.equals("") && !regccciaa.equals("")) {
                        String cciaa = comunecciaa + " - " + proccciaa + " - " + regccciaa;
                        fields.get("cciaa").setValue(cciaa);
                    }
                } catch (Exception e) {
                }

                fields.forEach((K, V) -> {
                    UserValoriReg va  = listavalori.stream().filter(re -> re.getCampo().equals(K)).findAny().orElse(null);
                    if (va  != null) {
//                        if (K.equals("societa") && fields.get("nodefinito") != null) {
//                            fields.get("nodefinito").setValue(va.getValore().toUpperCase());
//                        }
                        fields.get(K).setValue(va.getValore().toUpperCase());
                    } else {
                        if (mappavalori.get(K) != null) {
                            boolean checkbox = asList(V.getAppearanceStates()).size() > 0;
                            if (checkbox) {
                                if (mappavalori.get(K).toUpperCase().equals("SI")) {
                                    fields.get(K).setValue(V.getAppearanceStates()[0]);
                                }
                                if (K.equals("privacy1")) {
                                    if (mappavalori.get("privacy1").equals("NO")) {
                                        fields.get(K + "_1").setValue(V.getAppearanceStates()[0]);
                                    }
                                } else if (K.equals("privacy2")) {
                                    if (mappavalori.get("privacy2").equals("NO")) {
                                        fields.get(K + "_1").setValue(V.getAppearanceStates()[0]);
                                    }
                                }
                            } else {
                                fields.get(K).setValue(mappavalori.get(K).toUpperCase());
                            }
                        }
                    }
                    form.partialFormFlattening(K);
                });
                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO A DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }

        return null;
    }

    //ALLEGATO B - BASE
    private static File allegatoB(String username, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            ArrayList<UserValoriReg> listavalori = dbb.listaValoriUserbando(bando, username);
            HashMap<String, String> mappavaloriDOCA = dbb.getAllegatoA(username);
            List<AllegatoB> mappavaloriDOCB = dbb.getAllegatoB(username);
            String contentb64 = dbb.getPathDocModello(bando, "DONLB");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".B.pdf");

            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();
                fields.get("privacy0").setValue("On");
                try {
                    String comunecciaa = listavalori.stream().filter(re -> (re.getCampo().equals("comunecciaa"))).findAny().get().getValore().toUpperCase();
                    String proccciaa = listavalori.stream().filter(re -> (re.getCampo().equals("proccciaa"))).findAny().get().getValore().toUpperCase();
                    String regccciaa = listavalori.stream().filter(re -> (re.getCampo().equals("regccciaa"))).findAny().get().getValore().toUpperCase();
                    if (!comunecciaa.equals("") && !proccciaa.equals("") && !regccciaa.equals("")) {
                        String cciaa = comunecciaa + " - " + proccciaa + " - " + regccciaa;
                        fields.get("cciaa").setValue(cciaa);
                    }
                } catch (Exception e) {
                }

//                fields.get("dataconsegna").setValue(dataconsegna.toString("dd/MM/yyyy"));
                fields.forEach((K, V) -> {
                    UserValoriReg va  = listavalori.stream().filter(re -> re.getCampo().equals(K)).findAny().orElse(null);
                    if (va  != null) {
                        fields.get(K).setValue(va.getValore().toUpperCase());
                    } else {
                        if (mappavaloriDOCA.get(K) != null) {
                            boolean checkbox = asList(V.getAppearanceStates()).size() > 0;
                            if (checkbox) {
                                if (mappavaloriDOCA.get(K).toUpperCase().equals("SI")) {
                                    fields.get(K).setValue(V.getAppearanceStates()[0]);
                                }
                                if (K.equals("privacy1")) {
                                    if (mappavaloriDOCA.get("privacy1").equals("NO")) {
                                        fields.get(K + "_1").setValue(V.getAppearanceStates()[0]);
                                    }
                                } else if (K.equals("privacy2")) {
                                    if (mappavaloriDOCA.get("privacy2").equals("NO")) {
                                        fields.get(K + "_1").setValue(V.getAppearanceStates()[0]);
                                    }
                                }
                            } else {
                                fields.get(K).setValue(mappavaloriDOCA.get(K).toUpperCase());
                            }
                        } else {
                            if (K.startsWith("docente")) {

                                AtomicInteger indicedocente = new AtomicInteger(1);
                                mappavaloriDOCB.forEach(alB -> {
                                    if (K.equals("docentenome" + indicedocente.get())) {
                                        fields.get(K).setValue(alB.getNome().toUpperCase());
                                    } else if (K.equals("docentecognome" + indicedocente.get())) {
                                        fields.get(K).setValue(alB.getCognome().toUpperCase());
                                    } else if (K.equals("docentecf" + indicedocente.get())) {
                                        fields.get(K).setValue(alB.getCF().toUpperCase());
                                    } else if (K.equals("docentefascia" + indicedocente.get())) {
                                        fields.get(K).setValue(alB.getFascia().toUpperCase());
                                    } else if (K.equals("docenteinquadramento" + indicedocente.get())) {
                                        fields.get(K).setValue(alB.getInquadramento().toUpperCase());
                                    }

                                    indicedocente.addAndGet(1);
                                });
                            }
                        }
                    }
                    form.partialFormFlattening(K);
                });
                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO B DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    //ALLEGATO B1 - BASE
    private static File allegatoB1(String username, String iddocente, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            HashMap<String, String> mappavaloriB = dbb.getAllegato_B(username, iddocente);
            List<AllegatoB1_field> B1 = dbb.alb1(username, iddocente);
            String contentb64 = dbb.getPathDocModello(bando, "ALB1");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".B1.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {

                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();

                if (fields.get("iddoc") != null) {
                    fields.get("iddoc").setValue(iddocente);
                }

                fields.forEach((K, V) -> {
                    if (mappavaloriB.get(K) != null) {
                        fields.get(K).setValue(mappavaloriB.get(K).toUpperCase());
                    } else {
                        AtomicInteger indicedocente = new AtomicInteger(1);
                        B1.forEach(alB -> {
                            if (K.equals("committente" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getCommittente().toUpperCase());
                            } else if (K.equals("periodo" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getPeriodo().toUpperCase());
                            } else if (K.equals("du" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getDurata().toUpperCase());
                            } else if (K.equals("um" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getUm().toUpperCase());
                            } else if (K.equals("incarico" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getIncarico().toUpperCase());
                            } else if (K.equals("attivita" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getAttivita().toUpperCase());
                            } else if (K.equals("finanziamento" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getFinanziamento().toUpperCase());
                            } else if (K.equals("rif" + indicedocente.get())) {
                                fields.get(K).setValue(alB.getRif());
                            }
                            indicedocente.addAndGet(1);
                        });
                    }
                    form.partialFormFlattening(K);
                });
                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO B1 DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);

            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    //ALLEGATO C - BASE
    private static File allegatoC(String username, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            ArrayList<UserValoriReg> listavalori = dbb.listaValoriUserbandoCONV(bando, username);
            String contentb64 = dbb.getPathDocModello(bando + "_A", "CONV");
            String pathtemp = dbb.getPath("pathtemp");
            String dataavviso = dbb.getPath("conv.data.avviso.del");
            String dataavvisopub = dbb.getPath("conv.data.avviso.pub");
            String dataavvisopubsito = dbb.getPath("conv.data.avviso.pubsito");

            String dd_numero = dbb.getPath("conv.dd.numero");
            String dd_data = dbb.getPath("conv.dd.data");
            String datafine = dbb.getPath("conv.data.finerendicontazione");
            String decreto[] = dbb.getDecreto(username);
            dbb.closeDB();

            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".C.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();
                fields.forEach((K, V) -> {
                    UserValoriReg va  = listavalori.stream().filter(re -> re.getCampo().equals(K)).findAny().orElse(null);
                    if (va  != null) {
                        setFieldsValue(form, fields, K, va.getValore().toUpperCase());
                        form.partialFormFlattening(K);
                    }
                });

                //  ALTRI CAMPI
                setFieldsValue(form, fields, "conv.data.avviso.del", dataavviso);
                setFieldsValue(form, fields, "conv.data.avviso.pub", dataavvisopub);
                setFieldsValue(form, fields, "conv.data.avviso.pubsito", dataavvisopubsito);
                setFieldsValue(form, fields, "conv.dd.numero", dd_numero);
                setFieldsValue(form, fields, "conv.dd.data", dd_data);
                setFieldsValue(form, fields, "conv.data.finerendicontazione", datafine);
                setFieldsValue(form, fields, "decreto", decreto[0]);
                setFieldsValue(form, fields, "datadecreto", decreto[1]);
                setFieldsValue(form, fields, "nomecognome", listavalori.stream().filter(va1 -> (va1.getCampo().equals("nome"))).findAny().orElse(null).getValore() + " " + listavalori.stream().filter(va1 -> (va1.getCampo().equals("cognome"))).findAny().orElse(null).getValore());

                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO C DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }

        return null;
    }

    //ALLEGATO C1 - BASE
    private static File allegatoC_1(String username, DateTime dataconsegna) {
        try {
            Db_Bando dbb = new Db_Bando();
            String contentb64 = dbb.getPathDocModello(bando + "_A", "MOD1");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".A.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO 1 DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    //ALLEGATO C2 - BASE
    private static File allegatoC_2(String username, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            ArrayList<UserValoriReg> listavalori = dbb.listaValoriUserbando(bando, username);
            AllegatoC2 allegato_c2 = dbb.getAllegatoC2(username);
            String contentb64 = dbb.getPathDocModello(bando + "_A", "MOD2");
            String pathtemp = dbb.getPath("pathtemp");
            String dataavviso = dbb.getPath("conv.data.avviso.del");
            dbb.closeDB();

            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".C2.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();

                Class<? extends Object> c = allegato_c2.getClass();
                Field[] campi = c.getDeclaredFields();

                fields.forEach((K, V) -> {
                    UserValoriReg va  = listavalori.stream().filter(re -> re.getCampo().equals(K)).findAny().orElse(null);
                    if (va  != null) {
                        setFieldsValue(form, fields, K, va.getValore().toUpperCase());
                    } else {
                        for (Field field : campi) {
                            try {
                                String name = field.getName();
                                field.setAccessible(true);
                                if (K.equals(name)) {
                                    boolean checkbox = asList(V.getAppearanceStates()).size() > 0;
                                    if (checkbox) {
                                        if (field.get(allegato_c2).toString().toUpperCase().equals("SI")) {
                                            setFieldsValue(form, fields, K, V.getAppearanceStates()[0]);
                                        }
                                    } else {
                                        setFieldsValue(form, fields, K, field.get(allegato_c2).toString().toUpperCase());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    form.partialFormFlattening(K);
                });

                //  ALTRI CAMPI
                setFieldsValue(form, fields, "conv.data.avviso.del", dataavviso);
                setFieldsValue(form, fields, "nomecognome", listavalori.stream().filter(va1 -> (va1.getCampo().equals("nome"))).findAny().orElse(null).getValore() + " " + listavalori.stream().filter(va1 -> (va1.getCampo().equals("cognome"))).findAny().orElse(null).getValore());

                if (allegato_c2.getSoggetto1_nome().equals("") || allegato_c2.getSoggetto1_cognome().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto1_nomecognome", allegato_c2.getSoggetto1_nome().toUpperCase() + " " + allegato_c2.getSoggetto1_cognome().toUpperCase());
                }

                if (allegato_c2.getSoggetto2_nome().equals("") || allegato_c2.getSoggetto2_cognome().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto2_nomecognome", allegato_c2.getSoggetto2_nome().toUpperCase() + " " + allegato_c2.getSoggetto2_cognome().toUpperCase());
                }

                if (allegato_c2.getSoggetto1_luogonascita().equals("") || allegato_c2.getSoggetto1_datanascita().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto1_luogodata", allegato_c2.getSoggetto1_luogonascita().toUpperCase() + " - " + allegato_c2.getSoggetto1_datanascita());
                }

                if (allegato_c2.getSoggetto2_luogonascita().equals("") || allegato_c2.getSoggetto2_datanascita().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto2_luogodata", allegato_c2.getSoggetto2_luogonascita().toUpperCase() + " - " + allegato_c2.getSoggetto2_datanascita());
                }

                if (allegato_c2.getSoggetto1_indirizzoresidenza().equals("") || allegato_c2.getSoggetto1_cittaresidenza().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto1_residenza", allegato_c2.getSoggetto1_indirizzoresidenza().toUpperCase() + " " + allegato_c2.getSoggetto1_cittaresidenza().toUpperCase());
                }
                if (allegato_c2.getSoggetto2_indirizzoresidenza().equals("") || allegato_c2.getSoggetto2_cittaresidenza().equals("")) {
                } else {
                    setFieldsValue(form, fields, "soggetto2_residenza", allegato_c2.getSoggetto2_indirizzoresidenza().toUpperCase() + " " + allegato_c2.getSoggetto2_cittaresidenza().toUpperCase());
                }

                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO 2 DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }

        return null;
    }

    private static File allegatoD(String username, DateTime dataconsegna, boolean flatten) {
        try {
            Db_Bando dbb = new Db_Bando();
            ArrayList<UserValoriReg> listavalori = dbb.listaValoriUserbando(bando, username);
            String contentb64 = dbb.getPathDocModello(bando, "DONLD");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);

            Db_Bando dbd = new Db_Bando();
            String dataavviso = dbd.getPath("conv.data.avviso.del");
            dbd.closeDB();

            Db_Bando dbn = new Db_Bando(true);
            String dd_numero = dbn.getPath("conv.dd.numero");
            String dd_data = dbn.getPath("conv.dd.data");
            String protocollo = dbn.getProtocolloNEET(username);
            String decreto[] = dbn.getDecretoNEET(username);
            dbn.closeDB();

            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".D.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);

                Map<String, PdfFormField> fields = form.getFormFields();
                setFieldsValue(form, fields, "conv.data.avviso.del", dataavviso);
                setFieldsValue(form, fields, "conv.dd.numero", dd_numero);
                setFieldsValue(form, fields, "conv.dd.data", dd_data);
                setFieldsValue(form, fields, "protocollo", protocollo);
                setFieldsValue(form, fields, "decreto", decreto[0]);
                setFieldsValue(form, fields, "datadecreto", decreto[1]);
                fields.forEach((K, V) -> {
                    UserValoriReg va  = listavalori.stream().filter(re -> re.getCampo().equals(K)).findAny().orElse(null);
                    if (va  != null) {
                        setFieldsValue(form, fields, K, va.getValore().toUpperCase());
                    }
                    form.partialFormFlattening(K);
                });
                if (flatten) {
                    form.flattenFields();
                }
                BarcodeQRCode barcode = new BarcodeQRCode(username + " / ALLEGATO D DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));
                printbarcode(barcode, pdfDoc);
            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    //NULLAOSTA DA ALLEGARE ALLA CONVENZIONE
    public static File nullaosta(String username, String protocollo, String nomesa, DateTime dataconsegna) {
        try {
            Db_Bando dbb = new Db_Bando();
            String contentb64 = dbb.getPathDocModello(bando + "_A", "NUOS");
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);
            File pdfOut = new File(pathtemp + username + dataconsegna.toString("ddMMyyyyHHmmSSS") + ".NO.pdf");
            try ( InputStream is = new ByteArrayInputStream(decodeBase64(contentb64));  PdfReader reader = new PdfReader(is);  PdfWriter writer = new PdfWriter(pdfOut);  PdfDocument pdfDoc = new PdfDocument(reader, writer)) {
                PdfAcroForm form = getAcroForm(pdfDoc, true);
                form.setGenerateAppearance(true);
                Map<String, PdfFormField> fields = form.getFormFields();
                //STATICI

                setFieldsValue(form, fields, "protocollo", protocollo);
                setFieldsValue(form, fields, "enm", nomesa);
                setFieldsValue(form, fields, "data", dataconsegna.toString("dd/MM/yyyy"));

//                fields.get().setValue();
//                fields.get().setValue();
//                fields.get("strategie").setValue();
//                fields.get("soggetti").setValue("DONNE E DISOCCUPATI");
//                fields.get("ambito").setValue("NAZIONALE");
//                fields.get("output").setValue("PERCORSO FORMATIVO (FORMAZIONE DONNE E DISOCCUPATI)");
//                fields.get("finalita").setValue("CORREGGERE");
                form.flattenFields();

                BarcodeQRCode barcode = new BarcodeQRCode(username + " / NULLA OSTA DD/ " + dataconsegna.toString("ddMMyyyyHHmmSSS"));

                printbarcode(barcode, pdfDoc);

            }
            if (checkPDF(pdfOut)) {
                return pdfOut;
            }
        } catch (Exception e) {
            e.printStackTrace();
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    //BARCODE
    public static void printbarcode(BarcodeQRCode barcode, PdfDocument pdfDoc) {
        try {
            Rectangle rect = barcode.getBarcodeSize();
            PdfFormXObject formXObject = new PdfFormXObject(new Rectangle(rect.getWidth(), rect.getHeight() + 10));
            PdfCanvas pdfCanvas = new PdfCanvas(formXObject, pdfDoc);
            barcode.placeBarcode(pdfCanvas, BLACK);
            Image bCodeImage = new Image(formXObject);
            bCodeImage.setRotationAngle(toRadians(90));
            bCodeImage.setFixedPosition(25, 5);
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                new Canvas(pdfDoc.getPage(i), pdfDoc.getDefaultPageSize()).add(bCodeImage);
            }

        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
    }

    //FIRMA CON OTP
////    private static File sign_otp(File f1, String username) {
////        try {
////            Db_Bando dbb = new Db_Bando();
////            String nomecogn = dbb.nome_cognome_user(username);
////            dbb.closeDB();
////            File pdfOut = new File(f1.getPath() + ".O.pdf");
////            PdfDocument pdfDoc = new PdfDocument(new PdfReader(f1), new PdfWriter(pdfOut));
////            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
////            form.setGenerateAppearance(true);
////            Map<String, PdfFormField> fields = form.getFormFields();
////            if (fields.get("Firma") != null) {
////                fields.get("Firma").setValue(nomecogn);
////                fields.get("Firma").setVisibility(PdfFormField.VISIBLE);
////                form.partialFormFlattening("Firma");
////            }
////            if (fields.get("otp") != null) {
////                fields.get("otp").setValue("FIRMATO TRAMITE OTP IN DATA " + new DateTime().toString("dd/MM/yyyy HH:mm:ss"));
////                fields.get("otp").setVisibility(PdfFormField.VISIBLE);
////                form.partialFormFlattening("otp");
////            }
////            form.flattenFields();
////
////            pdfDoc.close();
////
////            if (checkPDF(pdfOut)) {
////                return pdfOut;
////            }
////        } catch (Exception e) {
////            trackingAction("ERROR SYSTEM", estraiEccezione(e));
////        }
////        return null;
////    }
    // PDF-A
    private static File convertPDFA(File pdf_ing, String nomepdf) {
        if (pdf_ing == null) {
            return null;
        }
        try {
            byte[] byteICC = decodeBase64(getPath("pdf.icc"));
            File pdfOutA = new File(replace(pdf_ing.getPath(), ".pdf", "_pdfA.pdf"));
            try ( FileInputStream in = new FileInputStream(pdf_ing)) {
                setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
                try ( PDDocument doc = load(pdf_ing)) {
                    int numPageTOT = 0;
                    Iterator<PDPage> it1 = doc.getPages().iterator();
                    while (it1.hasNext()) {
                        numPageTOT++;
                        it1.next();
                    }
                    PDPage page = new PDPage();
                    doc.setVersion(1.7f);
                    try ( PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                        PDDocument docSource = load(in);
                        PDFRenderer pdfRenderer = new PDFRenderer(docSource);
                        for (int i = 0; i < numPageTOT; i++) {
                            BufferedImage imagePage = pdfRenderer.renderImageWithDPI(i, 200);
                            PDImageXObject pdfXOImage = createFromImage(doc, imagePage);
                            contents.drawImage(pdfXOImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                        }
                    }
                    XMPMetadata xmp = createXMPMetadata();
                    PDDocumentCatalog catalogue = doc.getDocumentCatalog();
                    Calendar cal = getInstance();
                    try {
                        DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
                        dc.addCreator("YISU");
                        dc.addDate(cal);
                        PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
                        id.setPart(3);  //value => 2|3
                        id.setConformance("A"); // value => A|B|U
                        XmpSerializer serializer = new XmpSerializer();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        serializer.serialize(xmp, baos, true);
                        PDMetadata metadata = new PDMetadata(doc);
                        metadata.importXMPMetadata(baos.toByteArray());
                        catalogue.setMetadata(metadata);
                    } catch (BadFieldValueException e) {
                        throw new IllegalArgumentException(e);
                    }
                    InputStream colorProfile = new ByteArrayInputStream(byteICC);
                    PDOutputIntent intent = new PDOutputIntent(doc, colorProfile);
                    intent.setInfo("sRGB IEC61966-2.1");
                    intent.setOutputCondition("sRGB IEC61966-2.1");
                    intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
                    intent.setRegistryName("http://www.color.org");
                    catalogue.addOutputIntent(intent);
                    catalogue.setLanguage("it-IT");
                    PDViewerPreferences pdViewer = new PDViewerPreferences(page.getCOSObject());
                    pdViewer.setDisplayDocTitle(true);
                    catalogue.setViewerPreferences(pdViewer);
                    PDMarkInfo mark = new PDMarkInfo();
                    PDStructureTreeRoot treeRoot = new PDStructureTreeRoot();
                    catalogue.setMarkInfo(mark);
                    catalogue.setStructureTreeRoot(treeRoot);
                    catalogue.getMarkInfo().setMarked(true);
                    PDDocumentInformation info = doc.getDocumentInformation();
                    info.setCreationDate(cal);
                    info.setModificationDate(cal);
                    info.setAuthor("YISU");
                    info.setProducer("YISU");
                    info.setCreator("YISU");
                    info.setTitle(nomepdf);
                    info.setSubject("PDF/A");
                    doc.save(pdfOutA);
                }
            }

            return pdfOutA;
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    public static File allegatoA_fase1(String username) {
        File out1 = allegatoA(username, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato A");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    public static File allegatoB_fase1(String username) {
        File out1 = allegatoB(username, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato B");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    public static File allegatoD_fase1(String username) {
        File out1 = allegatoD(username, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato D");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    public static File allegatoB1(String username, String iddocente) {
        File out1 = allegatoB1(username, iddocente, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato B1");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    public static File allegatoC_mod2(String username) {
        File out1 = allegatoC_2(username, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato C2");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    public static File allegatoC_mod1(String username) {
        return allegatoC_1(username, new DateTime());
//        return convertPDFA(allegatoC_1(username, new DateTime()), "Allegato C1");
    }

    public static File allegatoC(String username) {
        File out1 = allegatoC(username, new DateTime(), true);
        if (out1 != null) {
            File out2 = convertPDFA(out1, "Allegato C");
            if (out2 != null) {
                return out2;
            }
        }
        return null;
    }

    //UTIL
    public static SignedDoc extractSignatureInformation_P7M(byte[] p7m_bytes) {
        SignedDoc doc = new SignedDoc();
        CMSSignedData cms;
        try {
            cms = new CMSSignedData(p7m_bytes);
        } catch (CMSException e) {
            doc.setErrore("ERRORE NEL FILE - " + e.getMessage());
            return doc;
        }
        if (cms.getSignedContent() == null) {
            doc.setErrore("ERRORE NEL FILE - CONTENUTO ERRATO");
            return doc;
        }
        try {
            Store<X509CertificateHolder> store = cms.getCertificates();
            Collection<X509CertificateHolder> allCerts = store.getMatches(null);
            if (!allCerts.isEmpty()) {
                X509CertificateHolder x509h = allCerts.iterator().next();
                CertificateFactory certFactory = getInstance("X.509");
                try ( InputStream in = new ByteArrayInputStream(x509h.getEncoded())) {
                    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
                    Principal principal = cert.getSubjectDN();
                    try {
                        cert.checkValidity();
                        doc.setValido(true);
                    } catch (Exception e) {
                        doc.setValido(false);
                        doc.setErrore(e.getMessage());
                    }
                    if (doc.isValido()) {
//                        String cf = substringBefore(substringAfter(principal.getName(), "SERIALNUMBER="), ", GIVENNAME");
                        doc.setCodicefiscale(principal.getName().toUpperCase());
                        doc.setContenuto((byte[]) cms.getSignedContent().getContent());
                    } else {
                        if (new DateTime(cert.getNotAfter().getTime()).isBeforeNow()) {
                            doc.setErrore("CERTIFICATO SCADUTO IN DATA " + new DateTime(cert.getNotAfter().getTime()).toString("dd/MM/yyyy HH:mm:ss"));
                        }
                    }
                }
            } else {
                doc.setErrore("ERRORE NEL FILE - CERTIFICATI NON TROVATI");
                return doc;
            }
        } catch (CertificateException | IOException ex) {
            doc.setValido(false);
            doc.setErrore("ERRORE NEL FILE - " + ex.getMessage());
        }
        return doc;
    }

    public static String verificaQRNOPDFA(String codicedoc, String username, byte[] content) {
        String out = "KO";
        if (codicedoc.equals("DONLA")
                || codicedoc.equals("DONLB")
                || codicedoc.equals("CONV")
                || codicedoc.equals("MOD1")
                || codicedoc.equals("MOD2")) {
            try {
                setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
                if (content != null) {
                    try ( InputStream is1 = new ByteArrayInputStream(content);  PDDocument doc = load(is1)) {
                        PDPage page = doc.getPage(0);
                        page.setCropBox(new PDRectangle(20, 0, 50, 50));
                        PDFRenderer pr = new PDFRenderer(doc);
                        BufferedImage bi = pr.renderImageWithDPI(0, 300);
                        int[] pixels = bi.getRGB(0, 0,
                                bi.getWidth(), bi.getHeight(),
                                null, 0, bi.getWidth());
                        RGBLuminanceSource source = new RGBLuminanceSource(bi.getWidth(),
                                bi.getHeight(),
                                pixels);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = new QRCodeReader().decode(bitmap);
                        String qr = result.getText().toUpperCase();
                        if (qr.contains(username.toUpperCase())) {
                            switch (codicedoc) {
                                case "DONLA":
                                    if (qr.contains("ALLEGATO A")) {
                                        out = "OK";
                                    } else {
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    }
                                    break;
                                case "DONLB":
                                    if (qr.contains("ALLEGATO B")) {
                                        out = "OK";
                                    } else {
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    }
                                    break;
                                case "CONV":
                                    if (qr.contains("ALLEGATO C")) {
                                        out = "OK";
                                    } else {
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    }
                                    break;
                                case "MOD1":
                                    if (qr.contains("ALLEGATO 1")) {
                                        out = "OK";
                                    } else {
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    }
                                    break;
                                case "MOD2":
                                    if (qr.contains("ALLEGATO 2")) {
                                        out = "OK";
                                    } else {
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    }
                                    break;
                                default:
                                    out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                    break;
                            }
                        } else {
                            out = "ERRORE - USERNAME NON CORRISPONDE";
                        }

                    }
                }
            } catch (Exception e) {
                if (e.getMessage() == null) {
                    out = "ERRORE - QR CODE ILLEGGIBILE";
                } else {
                    e.printStackTrace();
                    out = "ERRORE NEL FILE - " + e.getMessage();
                }
                trackingAction("ERROR SYSTEM", estraiEccezione(e));
            }
        } else { // non richiesto
            out = "OK";
        }
        return out;

    }

    public static SignedDoc extractSignatureInformation_PDF(byte[] pdf_bytes, File out) {
        SignedDoc doc = new SignedDoc();
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            addProvider(provider);
            try ( InputStream is = new ByteArrayInputStream(pdf_bytes);  PdfReader read = new PdfReader(is);  PdfDocument pdfDoc = new PdfDocument(read, new PdfWriter(out))) {
                AtomicInteger error = new AtomicInteger(0);
                SignatureUtil signatureUtil = new SignatureUtil(pdfDoc);
                List<String> li = signatureUtil.getSignatureNames();
                if (li.isEmpty()) {
                    doc.setErrore("ERRORE NEL FILE - NESSUNA FIRMA");
                } else {
                    li.forEach(name -> {
                        if (error.get() == 0) {
                            PdfPKCS7 signature1 = signatureUtil.readSignatureData(name);
                            if (signature1 != null) {
                                X509Certificate cert = signature1.getSigningCertificate();
                                try {
                                    boolean es = signature1.verifySignatureIntegrityAndAuthenticity();
                                    if (es) {
                                        Principal principal = cert.getSubjectDN();
                                        doc.setCodicefiscale(principal.getName().toUpperCase());
                                        doc.setContenuto(pdf_bytes);
                                        doc.setValido(true);
                                    } else {
                                        doc.setErrore("ERRORE NEL FILE - CERTIFICATO NON VALIDO");
                                        error.addAndGet(1);
                                        doc.setValido(false);
                                    }
                                } catch (Exception e) {
                                    doc.setValido(false);
                                    doc.setErrore("ERRORE NEL FILE - " + e.getMessage());
                                    error.addAndGet(1);
                                }
                            } else {
                                doc.setValido(false);
                                doc.setErrore("ERRORE NEL FILE - FIRMA NON VALDA");
                                error.addAndGet(1);
                            }
                        }
                    });
                }
            }
            try {
                out.delete();
            } catch (Exception e) {

            }
            return doc;
        } catch (Exception e) {
            doc.setErrore("ERRORE NEL FILE - " + e.getMessage());
            return doc;
        }
    }

    public static String verificaPDFA(String codicedoc, String username, byte[] content) {
        return "OK";
//        String out = "KO";
//        if (codicedoc.equals("DONLA")
//                || codicedoc.equals("DONLB")
//                || codicedoc.equals("CONV")
//                || codicedoc.equals("MOD2")) {
//            try {
//                setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
//                if (content != null) {
//
////                    try (InputStream is1 = new FileInputStream("C:\\Users\\rcosco\\Downloads\\MCN\\r.pdf"); PDDocument doc = load(is1)) {
//                    try (InputStream is1 = new ByteArrayInputStream(content); PDDocument doc = load(is1)) {
//
//                        PDDocumentInformation info = doc.getDocumentInformation();
//                        if (info.getSubject() != null) {
//                            if (info.getSubject().equals("PDF/A")) {
//                                out = "OK";
//                            } else {
//                                out = "ERRORE NEL FILE - NO PDF/A";
//                            }
//                        } else {
//                            out = "ERRORE NEL FILE - NO PDF/A";
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                out = "ERRORE NEL FILE - " + e.getMessage();
//                trackingAction("ERROR SYSTEM", estraiEccezione(e));
//            }
//        } else { // non richiesto
//            out = "OK";
//        }
//        return out;
    }

    public static String verificaQR(String codicedoc, String username, byte[] content) {
        String pdfa = verificaPDFA(codicedoc, username, content);
        if (!pdfa.equals("OK")) {
            return pdfa;
        } else {
            String out = "KO";
            if (codicedoc.equals("DONLA")
                    || codicedoc.equals("DONLB")
                    || codicedoc.equals("DONLD")
                    || codicedoc.equals("CONV")
                    || codicedoc.equals("MOD1")
                    || codicedoc.equals("MOD2")) {
                try {
                    setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
                    if (content != null) {
                        try ( InputStream is1 = new ByteArrayInputStream(content);  PDDocument doc = load(is1)) {
                            PDPage page = doc.getPage(0);
                            page.setCropBox(new PDRectangle(20, 0, 50, 50));
                            PDFRenderer pr = new PDFRenderer(doc);
                            BufferedImage bi = pr.renderImageWithDPI(0, 300);
                            int[] pixels = bi.getRGB(0, 0,
                                    bi.getWidth(), bi.getHeight(),
                                    null, 0, bi.getWidth());
                            RGBLuminanceSource source = new RGBLuminanceSource(bi.getWidth(),
                                    bi.getHeight(),
                                    pixels);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result result = new QRCodeReader().decode(bitmap);
                            String qr = result.getText().toUpperCase();
                            if (qr.contains(username.toUpperCase())) {
                                switch (codicedoc) {
                                    case "DONLA":
                                        if (qr.contains("ALLEGATO A DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    case "DONLB":
                                        if (qr.contains("ALLEGATO B DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    case "CONV":
                                        if (qr.contains("ALLEGATO C DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    case "MOD1":
                                        if (qr.contains("ALLEGATO 1 DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    case "MOD2":
                                        if (qr.contains("ALLEGATO 2 DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    case "DONLD":
                                        if (qr.contains("ALLEGATO D DD")) {
                                            out = "OK";
                                        } else {
                                            out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        }
                                        break;
                                    default:
                                        out = "ERRORE - DOCUMENTO NON CORRISPONDE A QUANTO RICHIESTO";
                                        break;
                                }
                            } else {
                                out = "ERRORE - USERNAME NON CORRISPONDE";
                            }

                        }
                    }
                } catch (Exception e) {
                    if (e.getMessage() == null) {
                        out = "ERRORE - QR CODE ILLEGGIBILE";
                    } else {
                        e.printStackTrace();
                        out = "ERRORE NEL FILE - " + e.getMessage();
                    }
                    trackingAction("ERROR SYSTEM", estraiEccezione(e));
                }
            } else {
                out = "OK";
            }
            return out;
        }
    }

    public static boolean checkPDF(File pdffile) {
        if (pdffile.exists()) {
            try {
                int pag;
                try ( InputStream is = new FileInputStream(pdffile);  PdfReader pdfReader = new PdfReader(is);  PdfDocument pd = new PdfDocument(pdfReader)) {
                    pag = pd.getNumberOfPages();
                }
                return pag > 0;
            } catch (IOException e) {
                trackingAction("ERROR SYSTEM", estraiEccezione(e));
            }
        }
        return false;
    }

    public static void setFieldsValue(PdfAcroForm form, Map<String, PdfFormField> fields_list, String field_name, String field_value) {
        try {
            if (fields_list.get(field_name) != null) {
                fields_list.get(field_name).setValue(field_value);
                form.partialFormFlattening(field_name);
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM", estraiEccezione(e));
        }
    }
}
