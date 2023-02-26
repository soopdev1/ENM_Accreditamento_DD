/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.action;

import static com.google.common.base.Splitter.on;
import static it.refill.action.Pdf_new.nullaosta;
import it.refill.db.Db_Bando;
import it.refill.entity.AllegatoB;
import it.refill.entity.AllegatoB1;
import it.refill.entity.AllegatoB1_field;
import it.refill.entity.AllegatoC2;
import it.refill.entity.Comuni_rc;
import it.refill.entity.Docbandi;
import it.refill.entity.DocumentiDocente;
import it.refill.entity.Docuserbandi;
import it.refill.entity.Docuserconvenzioni;
import it.refill.entity.Domandecomplete;
import it.refill.entity.Faq;
import it.refill.entity.FileDownload;
import it.refill.entity.Items;
import it.refill.entity.Prov_rc;
import it.refill.entity.Registrazione;
import it.refill.entity.Reportistica;
import it.refill.entity.UserValoriReg;
import static it.refill.util.Utility.createDir;
import static it.refill.util.Utility.estraiEccezione;
import static it.refill.util.Utility.formatStringtoStringDate;
import static it.refill.util.Utility.parseIntR;
import static it.refill.util.Utility.zipListFiles;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.stripAccents;
import org.apache.tika.Tika;
import org.joda.time.DateTime;

/**
 *
 * @author raffaele
 */
public class ActionB {

    public static void trackingAction(String username, String azione) {
        Db_Bando dbb = new Db_Bando();
        dbb.insertTracking(username, azione);
        dbb.closeDB();
    }

    public static boolean verificaBando(String cod) {
        Db_Bando dbb = new Db_Bando();
        boolean bo = dbb.bandoAttivoRaf(cod);
        dbb.closeDB();
        return bo;
    }

    public static Domandecomplete isPresenteDomandaCompleta(String bando, String username) {
        Db_Bando dbb = new Db_Bando();
        Domandecomplete dc = dbb.isPresenteDomandaCompleta(bando, username);
        dbb.closeDB();
        return dc;
    }

    public static Domandecomplete isDomandaCompletaConsolidata(String bando, String username) {
        Db_Bando dbb = new Db_Bando();
        Domandecomplete dc = dbb.isDomandaCompletaConsolidata(bando, username);
        dbb.closeDB();
        return dc;
    }

    public static ArrayList<Registrazione> listaCampiReg(String bando, String az) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<Registrazione> bo = new ArrayList<>();
        if (az == null) {
            bo = dbb.listaCampiReg(bando, "registrazione");
        }
        dbb.closeDB();
        return bo;
    }

    public static ArrayList<String[]> province() {
        Db_Bando db = new Db_Bando();
        ArrayList<String[]> desc = db.getAllProvince();
        db.closeDB();
        return desc;
    }

    public static ArrayList<String[]> comuni() {
        Db_Bando db = new Db_Bando();
        ArrayList<String[]> desc = db.comuni();
        db.closeDB();
        return desc;
    }

    public static ArrayList<Items> tipoDoc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> desc = db.getTipoDoc();
        db.closeDB();
        return desc;
    }

    public static String getDescrizioneBando(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.getDescrizioneBando(cod);
        dbb.closeDB();
        return capitalize(bo);
    }

    public static String getHTMLBando(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.getHTMLBando(cod);
        dbb.closeDB();
        return bo;
    }

    public static String getDescrizioneAltroAllUser(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.getDescrizioneAltroAll(cod);
        dbb.closeDB();
        return bo;
    }

    public static String descr_bandoaperto(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.descr_bandoaperto(cod);
        dbb.closeDB();
        return bo;
    }

    public static String descr_bandochiuso(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.descr_bandochiuso(cod);
        dbb.closeDB();
        return bo;
    }

    public static String getScadenzaBando(String cod) {
        Db_Bando dbb = new Db_Bando();
        String bo = dbb.getScadenzaBando(cod);
        dbb.closeDB();

        String pat1 = "yyyy-MM-dd";
        String pat2 = "dd/MM/yyyy";
        boolean ts = false;
        if (bo.length() > 10) {
            pat1 = "yyyy-MM-dd HH:mm:ss";
            pat2 = "dd/MM/yyyy  HH:mm:ss";
            ts = true;
        }

        return formatStringtoStringDate(bo, pat1, pat2, ts);

    }

    public static String generateUsername(String nome, String cognome) {
        nome = stripAccents(nome).replaceAll("[^a-zA-Z0-9]", "");
        cognome = stripAccents(cognome).replaceAll("[^a-zA-Z0-9]", "");
        String result;
        if (nome.length() > 1) {
            result = StringUtils.substring(nome, 0, 1).toUpperCase(); // First char
        } else {
            result = randomAlphabetic(1).toUpperCase();
        }
        if (cognome.length() > 5) {
            result += cognome.substring(0, 5).toUpperCase();
        } else {
            result += cognome.toUpperCase();
        }
        result += randomNumeric(4);
        return result;
    }

    public static ArrayList<Docbandi> listaDocRichiesti(String cod) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<Docbandi> liout = dbb.listaDocRichiestiBando(cod);
        dbb.closeDB();
        return liout;
    }

    public static ArrayList<String[]> listacodDocRichiesti(Db_Bando db, String bandorif, String username) {
        ArrayList<String[]> liout = new ArrayList<>();
        ArrayList<String[]> listart = db.listaCodDocRichiestiBando(bandorif);
        for (int i = 0; i < listart.size(); i++) {
            if (getSino(username).equals("SI")) {
                if (listart.get(i)[0].equals("DONLD")) {
                    liout.add(listart.get(i));
                }
            } else {
                if (listart.get(i)[0].equals("DOCR") || listart.get(i)[0].equals("DONLA")
                        || listart.get(i)[0].equals("DONLB") || listart.get(i)[0].equals("ALB1")) {
                    liout.add(listart.get(i));
                }
            }
        }
        return liout;
    }

    public static ArrayList<Docbandi> listaDocRichiesti(Db_Bando db, String bandorif, String username) {
        ArrayList<Docbandi> liout = new ArrayList<>();
        ArrayList<Docbandi> listart = db.listaDocRichiestiBando(bandorif);
        for (int i = 0; i < listart.size(); i++) {
            Docbandi dbb = listart.get(i);
            if (getSino(username).equals("SI")) {
                if (listart.get(i).getCodicedoc().equals("DONLD")) {
                    liout.add(dbb);
                }
            } else {
                if (listart.get(i).getCodicedoc().equals("DOCR") || listart.get(i).getCodicedoc().equals("DONLA")
                        || listart.get(i).getCodicedoc().equals("DONLB") || listart.get(i).getCodicedoc().equals("ALB1")) {
                    liout.add(dbb);
                }
            }
        }
        return liout;
    }

    public static ArrayList<Docbandi> listaDocRichiesti(String bandorif, String username) {
        ArrayList<Docbandi> liout = new ArrayList<>();
        Db_Bando db = new Db_Bando();
        ArrayList<Docbandi> listart = db.listaDocRichiestiBando(bandorif);
        db.closeDB();
        for (int i = 0; i < listart.size(); i++) {
            Docbandi dbb = listart.get(i);
            if (getSino(username).equals("SI")) {
                if (listart.get(i).getCodicedoc().equals("DOCR") || listart.get(i).getCodicedoc().equals("DONL")) {
                    liout.add(dbb);
                }
            } else {
                if (listart.get(i).getCodicedoc().equals("DOCR") || listart.get(i).getCodicedoc().equals("DONLA")
                        || listart.get(i).getCodicedoc().equals("DONLB") || listart.get(i).getCodicedoc().equals("ALB1")) {
                    liout.add(dbb);
                }
            }
        }
        return liout;
    }

    public static ArrayList<Docuserbandi> listaDocuserbando(String cod, String username) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<Docuserbandi> liout = dbb.listaDocUserBando(cod, username);
        dbb.closeDB();
        return liout;
    }

    public static ArrayList<Docuserbandi> listaDocuserbando(String cod, String username, String coddoc) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<Docuserbandi> liout = dbb.listaDocUserBando(cod, username, coddoc);
        dbb.closeDB();
        return liout;
    }

    public static Docuserbandi docUserBando(String cod, String username, String codicedoc) {
        Db_Bando dbb = new Db_Bando();
        Docuserbandi du = dbb.docUserBando(cod, username, codicedoc, "-", "-");
        dbb.closeDB();
        return du;
    }

    public static ArrayList<String[]> listaTipiAll(String bando) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<String[]> li = dbb.getListaAllBando(bando);
        dbb.closeDB();
        return li;
    }

    public static ArrayList<String[]> listaTipiAllRUP(String bando) {
        Db_Bando dbb = new Db_Bando();
        ArrayList<String[]> li = dbb.getListaAllBandoRUP(bando);
        dbb.closeDB();
        return li;
    }

    public static ArrayList<String[]> occupazione() {
        Db_Bando dbb = new Db_Bando();
        ArrayList<String[]> li = dbb.occupazione();
        dbb.closeDB();
        return li;
    }

    public static ArrayList<Reportistica> getListReports(String bandorif) {
        Db_Bando db = new Db_Bando();
        ArrayList<Reportistica> li = db.listareports(bandorif);
        db.closeDB();
        return li;
    }

    public static boolean domandaAnnullata(String bandorif, String username) {
        Db_Bando db = new Db_Bando();
        boolean is = db.isDomandaAnnullata(bandorif, username);
        db.closeDB();
        return is;
    }

    public static String[] comune(String bandorif, String username) {
        Db_Bando db = new Db_Bando();
        ArrayList<UserValoriReg> lista = db.listaValoriUserbando(bandorif, username);
        ArrayList<String[]> comuni = db.comuni();
        db.closeDB();
        for (int i = 0; i < lista.size(); i++) {
            UserValoriReg uvr = lista.get(i);
            if (uvr.getCampo().equals("comuni")) {
                for (int k = 0; k < comuni.size(); k++) {
                    if (comuni.get(k)[0].equals(uvr.getValore())) {
                        return comuni.get(k);
                    }
                }
            }
        }
        return null;
    }

    public static String getValoreCampo(ArrayList<UserValoriReg> lista, String nomecampo) {
        for (int i = 0; i < lista.size(); i++) {
            UserValoriReg uvr = lista.get(i);
            if (uvr.getCampo().equals(nomecampo)) {
                return uvr.getValore();
            }
        }
        return "";
    }

    public static int getDimMaxFiles() {
        Db_Bando db = new Db_Bando();
        int x = parseIntR(db.getPath("dim"));
        db.closeDB();
        return x;
    }

    public static String getDimMaxFilesEtichetta() {
        Db_Bando db = new Db_Bando();
        String x = (db.getPath("dimetichetta"));
        db.closeDB();
        return x;
    }

    public static boolean esisteusrnamedomande(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.esisteusrnamedomande(username);
        db.closeDB();
        return x;
    }

    public static boolean esisteAllegatoA(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.esisteAllegatoA(username);
        db.closeDB();
        return x;
    }

    public static boolean esisteAllegatoB(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.esisteAllegatoB(username);
        db.closeDB();
        return x;
    }

    public static boolean esisteAllegatoB1(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.esisteAllegatoB1(username);
        db.closeDB();
        return x;
    }

    public static boolean esisteAllegatoC2(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.esisteAllegatoC2(username);
        db.closeDB();
        return x;
    }

    public static boolean remdatiAllegatoA(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.remdatiAllegatoA(username);
        db.closeDB();
        return x;
    }

    public static boolean remdatiAllegatoB(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.remdatiAllegatoB(username);
        db.closeDB();
        return x;
    }

    public static boolean remdatiAllegatoC2(String username) {
        Db_Bando db = new Db_Bando();
        boolean x = db.remdatiAllegatoC2(username);
        db.closeDB();
        return x;
    }

    public static boolean invioattivo(String bando) {
        Db_Bando db = new Db_Bando();
        boolean x = db.invioattivo(bando);
        db.closeDB();
        return x;
    }

    public static String getPath(String id) {
        Db_Bando db = new Db_Bando();
        String path = db.getPath(id);
        db.closeDB();
        return path;
    }

    public static ArrayList<String[]> statieur() {
        Db_Bando db = new Db_Bando();
        ArrayList<String[]> desc = db.getAllStatiEur();
        db.closeDB();
        return desc;
    }

    public static ArrayList<String[]> Sesso() {
        Db_Bando db = new Db_Bando();
        ArrayList<String[]> desc = db.getSesso();
        db.closeDB();
        return desc;
    }

    public static ArrayList<String> SiNo() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("NO");
        desc.add("SI");
        return desc;
    }

    public static ArrayList<Domandecomplete> getDomandeComplete(String a, String b, String stato) {
        Db_Bando db = new Db_Bando();
        ArrayList<Domandecomplete> desc = db.listaconsegnatestato(a, b, stato);
        db.closeDB();
        return desc;
    }

    public static String getConvenzioneROMA(String username) {
        Db_Bando db = new Db_Bando();
        String path = db.getConvenzioneROMA(username);
        db.closeDB();
        return path;
    }

    public static String[] excelreport() {
        Db_Bando db = new Db_Bando();
        String[] desc = db.excelreport();
        db.closeDB();
        return desc;
    }

    public static String getSino(String username) {
        Db_Bando db = new Db_Bando();
        String var = db.getSiNo(username);
        db.closeDB();
        return var;
    }

    public static boolean insAllegatoA(
            //PUNTO 1
            String username, String enteistituzionepubblica, String associazione, String ordineprofessionale, String soggettoprivato, String formazione,
            String regione1, String iscrizione1, String servizi, String regione2, String iscrizione2, String ateco,
            //PUNTO 2
            String numaule,
            //AULA 1
            String indirizzo1, String citta1,
            String provincia1, String regioneaula1, String titolo1, String estremi1, String accreditamento1,
            String responsabile1, String mailresponsabile1, String telresponsabile1,
            String amministrativo1, String mailamministrativo1, String telamministrativo1,
            //AULA 2
            String indirizzo2, String citta2,
            String provincia2, String regioneaula2, String titolo2, String estremi2, String accreditamento2,
            String responsabile2, String mailresponsabile2, String telresponsabile2,
            String amministrativo2, String mailamministrativo2, String telamministrativo2,
            //AULA 3
            String indirizzo3, String citta3,
            String provincia3, String regioneaula3, String titolo3, String estremi3, String accreditamento3,
            String responsabile3, String mailresponsabile3, String telresponsabile3,
            String amministrativo3, String mailamministrativo3, String telamministrativo3,
            //AULA 4
            String indirizzo4, String citta4,
            String provincia4, String regioneaula4, String titolo4, String estremi4, String accreditamento4,
            String responsabile4, String mailresponsabile4, String telresponsabile4,
            String amministrativo4, String mailamministrativo4, String telamministrativo4,
            //AULA 5
            String indirizzo5, String citta5,
            String provincia5, String regioneaula5, String titolo5, String estremi5, String accreditamento5,
            String responsabile5, String mailresponsabile5, String telresponsabile5,
            String amministrativo5, String mailamministrativo5, String telamministrativo5,
            //ESPERIENZE
            String attivita, String destinatari, String finanziamento, String committente, String periodo,
            String attivita2, String destinatari2, String finanziamento2, String committente2, String periodo2,
            String attivita3, String destinatari3, String finanziamento3, String committente3, String periodo3,
            String attivita4, String destinatari4, String finanziamento4, String committente4, String periodo4,
            String attivita5, String destinatari5, String finanziamento5, String committente5, String periodo5,
            //PUNTO 3
            String noconsorzio, String consorzio, String nomeconsorzio,
            //PUNTO 4
            String pec,
            //PUNTO 5
            String numdocenti,
            //EXTRA
            String area, String area2, String area3, String area4, String area5,
            //PRIVACY
            String privacy1, String privacy2
    ) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insAllegatoA(
                username,
                //PUNTO 1
                enteistituzionepubblica, associazione, ordineprofessionale, soggettoprivato, formazione,
                regione1, iscrizione1, servizi, regione2, iscrizione2, ateco,
                //PUNTO 2
                numaule,
                //AULA 1
                indirizzo1, citta1,
                provincia1, regioneaula1, titolo1, estremi1, accreditamento1,
                responsabile1, mailresponsabile1, telresponsabile1,
                amministrativo1, mailamministrativo1, telamministrativo1,
                //AULA 2
                indirizzo2, citta2,
                provincia2, regioneaula2, titolo2, estremi2, accreditamento2,
                responsabile2, mailresponsabile2, telresponsabile2,
                amministrativo2, mailamministrativo2, telamministrativo2,
                //AULA 3
                indirizzo3, citta3,
                provincia3, regioneaula3, titolo3, estremi3, accreditamento3,
                responsabile3, mailresponsabile3, telresponsabile3,
                amministrativo3, mailamministrativo3, telamministrativo3,
                //AULA 4
                indirizzo4, citta4,
                provincia4, regioneaula4, titolo4, estremi4, accreditamento4,
                responsabile4, mailresponsabile4, telresponsabile4,
                amministrativo4, mailamministrativo4, telamministrativo4,
                //AULA 5
                indirizzo5, citta5,
                provincia5, regioneaula5, titolo5, estremi5, accreditamento5,
                responsabile5, mailresponsabile5, telresponsabile5,
                amministrativo5, mailamministrativo5, telamministrativo5,
                //ESPERIENZE
                attivita, destinatari, finanziamento, committente, periodo,
                attivita2, destinatari2, finanziamento2, committente2, periodo2,
                attivita3, destinatari3, finanziamento3, committente3, periodo3,
                attivita4, destinatari4, finanziamento4, committente4, periodo4,
                attivita5, destinatari5, finanziamento5, committente5, periodo5,
                //PUNTO 3
                noconsorzio, consorzio, nomeconsorzio,
                //PUNTO 4
                pec,
                //PUNTO 5
                numdocenti,
                //EXTRA
                area, area2, area3, area4, area5,
                //PRIVACY
                privacy1, privacy2);
        db.closeDB();
        return ctrl;
    }

    public static boolean insertAllegatoC2(AllegatoC2 dainserire) {
        String datetime = curtime();
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insertAllegatoC2(dainserire, datetime);
        db.closeDB();
        return ctrl;
    }

    public static boolean insAllegatoB(List<AllegatoB> dainserire) {
        String datetime = curtime();
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insAllegatoB(dainserire, datetime);
        db.closeDB();
        return ctrl;
    }

    public static boolean removeAllDocUserBando(String codbando, String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.removeAllDocUserBando(codbando, username);
        db.closeDB();
        return ctrl;
    }

    public static boolean removeSingleDocUserBando(String codbando, String username, String codicedoc, String tipologia) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.removeSingleDocUserBando(codbando, username, codicedoc, tipologia);
        db.closeDB();
        return ctrl;
    }

    public static boolean verificaDomandaCompleta(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.verificaDomandaCompleta(username);
        db.closeDB();
        return ctrl;
    }

    public static boolean insAllegatoB1(String idallegato_b1, String username,
            String allegatocv,
            String allegatodr,
            String allegatob1) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insAllegatoB1(idallegato_b1, username, allegatocv, allegatodr, allegatob1);
        db.closeDB();
        return ctrl;
    }

    public static boolean isPresenzaAllegatoB1(String idallegato_b1, String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.isPresenzaAllegatoB1(idallegato_b1, username);
        db.closeDB();
        return ctrl;
    }

    public static ArrayList<AllegatoB1> getAllegatoB1(String username, String id) {
        Db_Bando db = new Db_Bando();
        ArrayList<AllegatoB1> desc = db.getAllegatoB1(username, id);
        db.closeDB();
        return desc;
    }

    public static boolean delAllegatiDocenti(String username, String id) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.delAllegatiDocenti(username, id);
        db.closeDB();
        return ctrl;
    }

    public static boolean insertDocUserBando(Docuserbandi dub) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insertDocUserBando(dub);
        db.closeDB();
        return ctrl;
    }

    public static ArrayList<DocumentiDocente> listaDocUserBandoDocenti(String username, boolean withcontent) {
        Db_Bando db = new Db_Bando();
        ArrayList<DocumentiDocente> desc = db.listaDocUserBandoDocenti(username, withcontent);
        db.closeDB();
        return desc;
    }

    public static boolean delAllAllegatiDocenti(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.delAllAllegatiDocenti(username);
        db.closeDB();
        return ctrl;
    }

    public static boolean esisteAllegatoB1Field(String username, int iddocente) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.esisteAllegatoB1Field(username, iddocente);
        db.closeDB();
        return ctrl;
    }

    public static boolean insAllegatoB1Field(String iddocente, String username, String periodo, String durata, String incarico,
            String finanziamento, String attivita, String committente, String rif) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insAllegatoB1Field(iddocente, username, periodo, durata, incarico, finanziamento, attivita, committente, rif);
        db.closeDB();
        return ctrl;

    }

    public static boolean delAllegatoB1Field(String id, String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.delAllegatoB1Field(id, username);
        db.closeDB();
        return ctrl;
    }

    public static boolean delAllegatoB1Field(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.delAllegatoB1Field(username);
        db.closeDB();
        return ctrl;
    }

    public static HashMap<String, String> getAllegatoA(String username) {
        Db_Bando db = new Db_Bando();
        HashMap<String, String> a = db.getAllegatoA(username);
        db.closeDB();
        return a;
    }

    public static List<AllegatoB> getAllegatoB(String username) {
        Db_Bando db = new Db_Bando();
        List<AllegatoB> allegatoB = db.getAllegatoB(username);
        db.closeDB();
        return allegatoB;
    }

    public static AllegatoB getAllegatoB(String username, String id) {
        Db_Bando db = new Db_Bando();
        AllegatoB allegatoB = db.getAllegatoB(username, id);
        db.closeDB();
        return allegatoB;
    }

    public static boolean isDomandaPresente(String bandorif, String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.isDomandaPresente(bandorif, username);
        db.closeDB();
        return ctrl;
    }

    public static List<AllegatoB1_field> alb1(String user, String iddoc) {
        Db_Bando db = new Db_Bando();
        List<AllegatoB1_field> al = db.alb1(user, iddoc);
        db.closeDB();
        return al;
    }

    public static boolean presenzaAllB1Field(String user, String iddoc) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.presenzaAllB1Field(user, iddoc);
        db.closeDB();
        return ctrl;
    }

    public static int getDocentiAllegatoA(String username) {
        Db_Bando db = new Db_Bando();
        int x = db.getDocentiAllegatoA(username);
        db.closeDB();
        return x;
    }

    public static boolean isPresenteUsername(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.isPresenteUsername(username);
        db.closeDB();
        return ctrl;
    }

    public static boolean isPresenzaDocumento(String username, String tipologia) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.isPresenzaDocumento(username, tipologia);
        db.closeDB();
        return ctrl;
    }

    public static boolean setStatoDomandaAccRif(String username, String stato, String protocollo, String motivazione, String decreto, String datadecreto) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.setStatoDomandaAccRif(username, stato, protocollo, motivazione, decreto, datadecreto);
        db.closeDB();
        return ctrl;
    }

    public static boolean getStatoDomanda(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.getStatoDomanda(username);
        db.closeDB();
        return ctrl;
    }

    public static String getStatoDomandaInviata(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getStatoDomandaInviata(username);
        db.closeDB();
        return ctrl;
    }

    public static String getDataInvioDomanda(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getDataInvioDomanda(username);
        db.closeDB();
        return ctrl;
    }

    public static String getProtocolloDomandaInviata(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getProtocolloDomandaInviata(username);
        db.closeDB();
        return ctrl;
    }

    public static String getDecretoDomandaInviata(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getDecretoDomandaInviata(username);
        db.closeDB();
        return ctrl;
    }

    public static boolean insertDocumentUserConvenzioni(Docuserconvenzioni dub) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insertDocumentUserConvenzioni(dub);
        db.closeDB();
        return ctrl;
    }

    public static boolean verPresenzaConvenzioni(String username, String coddoc) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.verPresenzaConvenzioni(username, coddoc);
        db.closeDB();
        return ctrl;
    }

    public static String getPathConvenzioni(String username, String coddoc) {
        Db_Bando db = new Db_Bando();
        String path = db.getPathConvenzioni(username, coddoc);
        db.closeDB();
        return path;
    }

    public static boolean remDocConvenzioni(String username, String coddoc) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.remDocConvenzioni(username, coddoc);
        db.closeDB();
        return ctrl;
    }

    public static boolean verificaPresenzaConvenzioni(String username, String coddoc) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.verificaPresenzaConvenzioni(username, coddoc);
        db.closeDB();
        return ctrl;
    }

    public static int countDocumentConvenzioni(String username) {
        Db_Bando db = new Db_Bando();
        int var = db.countDocumentConvenzioni(username);
        db.closeDB();
        return var;
    }

    public static boolean setStatoInvioDocumenti(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.setStatoInvioDocumenti(username);
        db.closeDB();
        return ctrl;
    }

    public static boolean verificaInvioConvenzione(String username) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.verificaInvioConvenzione(username);
        db.closeDB();
        return ctrl;
    }

    public static String getDataInvioConvenzione(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getDataInvioConvenzione(username);
        db.closeDB();
        return ctrl;
    }

    public static ArrayList<Docuserconvenzioni> getDocumentiConvenzioni(String username) {
        Db_Bando db = new Db_Bando();
        ArrayList<Docuserconvenzioni> al = db.getDocumentiConvenzioni(username);
        db.closeDB();
        return al;
    }

    public static ArrayList<Prov_rc> query_province_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Prov_rc> al = db.query_province_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Prov_rc> query_regione_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Prov_rc> al = db.query_regione_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Comuni_rc> query_comuni_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Comuni_rc> al = db.query_comuni_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Comuni_rc> query_nazioniecomuni_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Comuni_rc> al1 = db.query_comuni_rc();
        ArrayList<Comuni_rc> al2 = db.query_nazioni_rc();
        db.closeDB();
        ArrayList<Comuni_rc> al = new ArrayList<>();
        al.addAll(al1);
        al.addAll(al2);
        return al;
    }

    public static ArrayList<Items> query_fontifin_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_fontifin_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Items> query_aree_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_aree_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Items> query_titolistudio_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_titolistudio_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Items> query_qualificazione_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_qualificazione_rc();
        db.closeDB();
        return al;
    }

    public static ArrayList<Items> query_unitamisura_rc() {
        ArrayList<Items> al = new ArrayList<>();
        al.add(new Items("gg", "GIORNI"));
        al.add(new Items("mm", "MESI"));
        al.add(new Items("aa", "ANNI"));
        return al;
    }

    public static ArrayList<Items> query_statodomanda_rc() {
        ArrayList<Items> al = new ArrayList<>();
        al.add(new Items("S", "NON PROCESSATA"));
        al.add(new Items("A", "APPROVATA"));
        al.add(new Items("R", "RIGETTATA"));
        al.add(new Items("A1", "CONVENZIONE SA"));
        al.add(new Items("A3", "IN ATTESA FIRMA ENM"));
        al.add(new Items("A2", "SA ATTIVO"));
        return al;
    }

    public static ArrayList<Items> query_inquadramento_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_inquadramento_rc();
        db.closeDB();
        return al;
    }

    public static List<Faq> elencoFAQ(String stato) {
        Db_Bando db = new Db_Bando();
        List<Faq> al = db.elencoFAQ(stato);
        db.closeDB();
        return al;
    }

    public static Faq getFAQ(String id) {
        Db_Bando db = new Db_Bando();
        Faq al = db.getFAQ(id);
        db.closeDB();
        return al;
    }

    public static ArrayList<Items> query_attivita_docenti_rc() {
        Db_Bando db = new Db_Bando();
        ArrayList<Items> al = db.query_attivita_docenti_rc();
        db.closeDB();
        return al;
    }

    public static boolean settaInvioEmailROMA(String username) {
        Db_Bando db = new Db_Bando();
        //////////////////////////////////////////////////////
        String date = db.curtime();
        boolean ctrl = db.settaInvioEmailROMA(username, date);
        db.closeDB();
        return ctrl;
    }

    public static String getInvioEmailROMA(String username) {
        Db_Bando db = new Db_Bando();
        String ctrl = db.getInvioEmailROMA(username);
        db.closeDB();
        return ctrl;
    }

    public static String curtime() {
        Db_Bando db = new Db_Bando();
        String ctrl = db.curtime();
        db.closeDB();
        return ctrl;
    }

    // ROMA
    public static boolean insertConvenzioneROMA(String username, String path) {
        Db_Bando db = new Db_Bando();
        boolean ctrl = db.insertConvenzioneROMA(username, path);
        db.closeDB();
        return ctrl;
    }

//    public static String getConvenzioneROMA(String username) {
//        Db_Bando db = new Db_Bando();
//        String path = db.getConvenzioneROMA(username);
//        db.closeDB();
//        return path;
//    }
    public static String getDatafirmaConvenzioneROMA(String username) {
        Db_Bando db = new Db_Bando();
        String path = db.getDatafirmaConvenzioneROMA(username);
        db.closeDB();
        return path;
    }

    public static String[] getValoriPerEmail(String coddomanda) {
        Db_Bando db = new Db_Bando();
        String var[] = db.getValoriPerEmail(coddomanda);
        db.closeDB();
        return var;
    }

    public static String getRagioneSociale(String user) {
        Db_Bando db = new Db_Bando();
        String ragsoc = db.getRagioneSociale(user);
        db.closeDB();
        return ragsoc;
    }

    public static void deletesessionid(String sessionid) {
        Db_Bando db = new Db_Bando();
        db.deletesessionid(sessionid);
        db.closeDB();
    }

    public static void newsessionid(String sessionid, String user, String time) {
        Db_Bando db = new Db_Bando();
        db.newsessionid(user, sessionid, time);
        db.closeDB();
    }

    public static String preparefileforupload(File file) {
        try {
            String mimeType = new Tika().detect(file);
            String content = encodeBase64String(readFileToByteArray(file));
            return file.getName() + "###" + mimeType + "###" + content;
        } catch (Exception ex) {
            trackingAction("SYSTEM", estraiEccezione(ex));
            return "FILE ERROR";
        }
    }

    public static boolean checkfileuploadmultiplo(String[] file) {
        List<String> sp2 = asList(file);
        if (sp2.size() == 3) {
            List<String> sp_A = on("###").splitToList(sp2.get(0));
            List<String> sp_B = on("###").splitToList(sp2.get(1));
            List<String> sp_C = on("###").splitToList(sp2.get(2));

            if (getExtension(sp_A.get(0)).toLowerCase().contains("pdf")
                    || getExtension(sp_A.get(0)).toLowerCase().contains("p7m")) {
                if (getExtension(sp_B.get(0)).toLowerCase().contains("pdf")
                        || getExtension(sp_B.get(0)).toLowerCase().contains("p7m")) {
                    if (getExtension(sp_C.get(0)).toLowerCase().contains("pdf")
                            || getExtension(sp_C.get(0)).toLowerCase().contains("p7m")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static FileDownload preparefilefordownload(String path) {
        List<String> spl = on("###").splitToList(path);
        if (spl.size() == 3) {
            return new FileDownload(spl.get(0), spl.get(1), spl.get(2));
        }
        return null;
    }

    public static String encodeB64(File file) {
        try {
            return encodeBase64String(readFileToByteArray(file));
        } catch (IOException ex) {
            return "FILE ERROR";
        }
    }

    public static File creaZip_Convenzione(String username, String protocollo, String ragsoc) {
        try {

            ArrayList<FileDownload> ff = new ArrayList<>();
            Db_Bando db = new Db_Bando();
            ArrayList<Docuserconvenzioni> aldocumenti = db.getDocumentiConvenzioni(username);
            db.closeDB();
            File no = nullaosta(username, protocollo, ragsoc, new DateTime());

            for (int i = 0; i < aldocumenti.size(); i++) {
                if (!aldocumenti.get(i).getPath().equals("-")) {
                    if (aldocumenti.get(i).getCodicedoc().contains("CONV")) {
                        ff.add(preparefilefordownload(aldocumenti.get(i).getPath()));
                    }
                }
            }
            if (no != null) {
                ff.add(preparefilefordownload(preparefileforupload(no)));
            }

            Db_Bando dbb = new Db_Bando();
            String pathtemp = dbb.getPath("pathtemp");
            dbb.closeDB();
            createDir(pathtemp);
            String name = "CONV_" + username + new DateTime().toString("yyyyMMddHHmmssSSS") + ".zip";
            File zipout = new File(pathtemp + name);
            if (zipListFiles(ff, zipout)) {
                return zipout;
            }
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
        return null;
    }

    public static List<String> elencoaccreditati() {
        Db_Bando dbb = new Db_Bando();
        List<String> list1 = dbb.elencoaccreditati();
        dbb.closeDB();
        return list1;

    }

    public static List<FileDownload> lista_file_Convenzione(String username, String protocollo, String ragsoc) {
        List<FileDownload> out = new ArrayList<>();
        try {
            Db_Bando db = new Db_Bando();
            ArrayList<Docuserconvenzioni> aldocumenti = db.getDocumentiConvenzioni(username);
            db.closeDB();
            Docuserconvenzioni conv = aldocumenti.stream().filter(d1 -> d1.getCodicedoc().contains("CONV")).findAny().orElse(null);
            if (conv != null) {
                FileDownload nullaosta = preparefilefordownload(preparefileforupload(nullaosta(username, protocollo, ragsoc, new DateTime())));
                FileDownload convenzione = preparefilefordownload(conv.getPath());
                out.add(convenzione);
                out.add(nullaosta);
            }
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

}
