
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.db;

import static it.refill.action.ActionB.query_unitamisura_rc;
import static it.refill.action.Constant.bando;
import static it.refill.action.Constant.patternITA;
import static it.refill.action.Constant.patternSql;
import static it.refill.action.Constant.test;
import static it.refill.action.Constant.timestampITA;
import static it.refill.action.Constant.timestampSQL;
import it.refill.entity.AllegatoB;
import it.refill.entity.AllegatoB1;
import it.refill.entity.AllegatoB1_field;
import it.refill.entity.AllegatoC2;
import it.refill.entity.Ateco;
import it.refill.entity.Comuni_rc;
import it.refill.entity.Docbandi;
import it.refill.entity.DocumentiDocente;
import it.refill.entity.Docuserbandi;
import it.refill.entity.Docuserconvenzioni;
import it.refill.entity.Domandecomplete;
import it.refill.entity.Faq;
import it.refill.entity.Items;
import it.refill.entity.Prov_rc;
import it.refill.entity.Registrazione;
import it.refill.entity.Reportistica;
import it.refill.entity.UserBando;
import it.refill.entity.UserValoriReg;
import it.refill.servlet.HttpSessionCollector;
import static it.refill.util.Utility.convMd5;
import static it.refill.util.Utility.cp_toUTF;
import static it.refill.util.Utility.estraiEccezione;
import static it.refill.util.Utility.formatDate;
import static it.refill.util.Utility.formatStringtoStringDate;
import static it.refill.util.Utility.parseIntR;
import static it.refill.util.Utility.randomP;
import static it.refill.util.Utility.replaeSpecialCharacter;
import static java.lang.Class.forName;
import static java.lang.String.valueOf;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.text.WordUtils.capitalize;
import static org.apache.commons.validator.routines.EmailValidator.getInstance;
import org.joda.time.DateTime;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author raffaele
 */
public class Db_Bando {

    private Connection c = null;

    private static final ResourceBundle conf = ResourceBundle.getBundle("conf.conf");

    public Db_Bando(boolean neet) {
        if (neet) {
            String driver = "com.mysql.cj.jdbc.Driver";
            String user = conf.getString("db.user");
            String password = conf.getString("db.pass");
            String host = conf.getString("db.host") + ":3306/enm_neet_prod";
//            if (test) {
//                host = conf.getString("db.host") + ":3306/enm_neet";
//            }
            try {
                forName(driver).newInstance();
                Properties p = new Properties();
                p.put("user", user);
                p.put("password", password);
                p.put("characterEncoding", "UTF-8");
                p.put("passwordCharacterEncoding", "UTF-8");
                p.put("useSSL", "false");
                p.put("connectTimeout", "1000");
                p.put("useUnicode", "true");

                this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
//                boolean ok = connesso(this.c);
//                System.out.println("HOST: " + host + " - CONNESSO " + ok + " - ISDBTEST: " + test);
            } catch (Exception ex) {
                System.err.println(estraiEccezione(ex));
                if (this.c != null) {
                    try {
                        this.c.close();
                    } catch (Exception ex1) {
                    }
                }
                this.c = null;
            }
        }
    }

    public Db_Bando() {

        String driver = "com.mysql.cj.jdbc.Driver";
        String user = "bando";
        String password = "bando";
        String host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_dd_prod";

        if (test) {
            host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_dd";
        }

        try {
            forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", user);
            p.put("password", password);
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");

            this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
//            boolean ok = connesso(this.c);
//            System.out.println("HOST: " + host + " - CONNESSO " + ok + " - ISDBTEST: " + test);
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
            if (this.c != null) {
                try {
                    this.c.close();
                } catch (Exception ex1) {
                }
            }
            this.c = null;
        }
    }

    public boolean connesso(Connection con) {
        try {
            return con != null && !con.isClosed();
        } catch (Exception ignored) {
        }
        return false;
    }

    public void closeDB() {
        try {
            if (this.c != null) {
                this.c.close();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
    }

    public Db_Bando(Connection conn) {
        try {
            this.c = conn;
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
    }

    public Connection getConnection() {
        return c;
    }

    public ArrayList<Docuserbandi> listaDocUserBandoNocontent(String cod, String username) {
        ArrayList<Docuserbandi> val = new ArrayList<>();
        try {
            String sql = "SELECT codicedoc,datacar,timestamp,tipodoc,note FROM docuserbandi WHERE codbando = ? AND username = ? AND stato = ? ORDER BY datacar";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Docuserbandi db = new Docuserbandi(cod, username, rs.getString("codicedoc"), "1",
                                "-", rs.getString("datacar"), rs.getString("timestamp"), rs.getString("tipodoc"), rs.getString("note"));
                        val.add(db);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public String curdate() {
        String out = new DateTime().toString("yyyy-MM-dd");
        try {
            String sql = "SELECT curdate()";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public boolean bandoAttivoRaf(String cod) {
        boolean out = false;
        try {
            String sql = "SELECT * FROM elencobandi WHERE codbando = ? AND ATTIVO = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, "SI");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String dataapertura = rs.getString("dataapertura");
                        String datafinereg = rs.getString("datafineregistrazione");
                        DateTime datafine = formatDate(datafinereg, timestampSQL);
                        DateTime dataap = formatDate(dataapertura, timestampSQL);
                        DateTime datenow = formatDate(curtime(), timestampSQL);
                        if (datafine.isAfter(datenow) && dataap.isBefore(datenow)) {
                            out = true;
                        } else {
                            out = datafine.equals(datenow);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public String curtime() {
        String out = "";
        try {
            String sql = "SELECT now()";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Registrazione> listaCampiReg(String cod, String table) {
        ArrayList<Registrazione> li = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + table + " WHERE codbando = ? and visibile = ? order by ordinamento,etichetta";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, "SI");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Registrazione reg = new Registrazione(rs.getString(1), rs.getString(2).toUpperCase(), rs.getString(3),
                                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getInt(8), rs.getString(9), rs.getString(10));
                        li.add(reg);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return li;
    }

    public ArrayList<String[]> getAllProvince() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM province order by descrizione";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] v1 = {rs.getString(1), rs.getString(2) + " (" + rs.getString(3) + ")"};
                    val.add(v1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<String[]> occupazione() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM occupazione";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] v1 = {rs.getString(1), rs.getString(2)};
                    val.add(v1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Items> getTipoDoc() {
        ArrayList<Items> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM doc_validi ORDER BY descrizione";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    val.add(new Items(rs.getString(1), rs.getString(2).toUpperCase()));
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public String getHTMLBando(String cod) {
        String out = "Descrizione non disponibile.";
        try {
            String sql = "SELECT html FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String getDescrizioneBando(String cod) {
        String out = "Descrizione non disponibile.";
        try {
            String sql = "SELECT descrizione FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = cp_toUTF(rs.getString(1));
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String getDescrizioneAltroAll(String cod) {
        String out = "Descrizione non disponibile.";
        try {
            String sql = "SELECT descrizione FROM tipiall WHERE id = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String descr_bandoaperto(String cod) {
        String out = "";
        try {
            String sql = "SELECT datachiusura FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String dat = rs.getString(1).split("\\.")[0];
                        DateTimeFormatter fmt = forPattern("yyyy-MM-dd HH:mm:ss");
                        DateTime dtout = fmt.parseDateTime(dat);
                        out = "L'accreditamento a questo bando è attivo fino alle ore " + dtout.toString("HH:mm:ss") + " del " + dtout.toString("dd/MM/yyyy") + ".";
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String descr_bandochiuso(String cod) {
        String out = "";
        try {
            String sql = "SELECT dataapertura,datachiusura FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String dat_aper = rs.getString(1).split("\\.")[0];
                        String dat_chiu = rs.getString(2).split("\\.")[0];
                        DateTimeFormatter fmt = forPattern("yyyy-MM-dd HH:mm:ss");
                        DateTime dt1 = fmt.parseDateTime(dat_aper);
                        DateTime dt2 = fmt.parseDateTime(dat_chiu);
                        out = "L'accreditamento è disponibile dalle ore " + dt1.toString("HH:mm") + " del "
                                + dt1.toString("dd/MM/yyyy") + " alle ore " + dt2.toString("HH:mm:ss") + " del " + dt2.toString("dd/MM/yyyy") + ".";
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String getScadenzaBando(String cod) {
        String out = "Descrizione non disponibile.";
        try {
            String sql = "SELECT datachiusura FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public String getInizioBando(String cod) {
        String out = "Descrizione non disponibile.";
        try {
            String sql = "SELECT dataapertura FROM elencobandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean isPresenteUsername(String username) {
        boolean out = false;
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean notaIsPresente(String bandorif, String username, String nota) {
        boolean out = false;
        try {
            String sql = "SELECT note FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, "ALTR");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (nota.trim().equals(rs.getString(1).trim())) {
                            out = true;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean isDomandaPresente(String bandorif, String username) {
        boolean out = false;
        try {
            String sql = "SELECT id FROM domandecomplete WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean insertUserReg(UserBando ub) {
        try {
            String ins = "INSERT INTO users (username,codiceBando,datareg,password,tipo,cell,mail) VALUES (?,?,?,?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, ub.getUsername());
                ps.setString(2, ub.getCodiceBando());
                ps.setString(3, ub.getDatareg());
                ps.setString(4, ub.getPassword());
                ps.setString(5, ub.getTipo());
                ps.setString(6, ub.getCell());
                ps.setString(7, ub.getMail());
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public int insertUserRegistration(ArrayList<UserValoriReg> liout) {
        int x = 0;
        for (int i = 0; i < liout.size(); i++) {
            String def = "";
            try {
                UserValoriReg uvr = liout.get(i);
                String ins = "INSERT INTO usersvalori (codbando,username,campo,valore,datareg) VALUES (?,?,?,?,?)";
                try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                    ps.setString(1, uvr.getCodbando());
                    ps.setString(2, uvr.getUsername());
                    ps.setString(3, uvr.getCampo());
                    ps.setString(4, uvr.getValore());
                    ps.setString(5, uvr.getDatareg());
                    def = ps.toString();
                    ps.execute();
                }
                x++;
            } catch (SQLException ex) {
                insertTracking("ERROR SYSTEM", estraiEccezione(ex) + " -PS: " + def);
                return 0;
            }
        }
        return x;
    }

    public boolean esisteAllegatoA(String username) {
        boolean out = false;
        try {
            String sql = "Select username from allegato_a where username='" + username + "' ";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                out = rs.next();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean esisteAllegatoB(String username) {
        boolean out = false;
        try {
            String sql = "Select username from allegato_b where username='" + username + "' ";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                out = rs.next();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean esisteAllegatoC2(String username) {
        boolean out = false;
        try {
            String sql = "Select username from allegato_c2 where username='" + username + "' ";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                out = rs.next();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean esisteAllegatoB1(String username) {
        boolean out = false;
        try {
            String sql = "Select count(idallegato_b1) from allegato_b1 where username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) > 0) {
                        out = (rs.getInt(1) == getDocentiAllegatoA(username));
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean esisteAllegatoB1Field(String username, int iddocente) {
        boolean out = false;
        try {
            String sql = "Select * from allegato_b1_field where username = ? and iddocente=?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setInt(2, iddocente);
                try ( ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean remdatiAllegatoA(String username) {
        String query = "delete from allegato_a where username = ?";
        try {
            PreparedStatement ps = this.c.prepareStatement(query);
            ps.setString(1, username);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            return false;
        }
    }

    public boolean remdatiAllegatoB(String username) {
        try {
            String query = "delete from allegato_b where username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            return false;
        }
    }

    public boolean remdatiAllegatoC2(String username) {
        try {
            String query = "delete from allegato_c2 where username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            return false;
        }
    }

    public boolean esisteusrnamedomande(String username) {
        boolean out = false;
        try {
            String sql = "Select username from bando_dd_mcn where username='" + username + "' ";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                out = rs.next();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean deletebando(String username) {
        try {
            String sql = "DELETE FROM bando_dd_mcn where username='" + username + "' ";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public String changeUserPassword(String user) {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, user);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (getInstance().isValid(rs.getString("mail"))) {
                            String pass = randomP();
                            String passmd5 = convMd5(pass);
                            String upd = "UPDATE users SET password = ? WHERE username = ?";
                            try ( PreparedStatement ps1 = this.c.prepareStatement(upd)) {
                                ps1.setString(1, passmd5);
                                ps1.setString(2, user);
                                boolean x = ps1.executeUpdate() > 0;
                                if (x) {
                                    return pass;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    public UserBando getUser(String user) {
        UserBando us = null;
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, user);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String bando = rs.getString("codiceBando");
                        String tipo = rs.getString("tipo");
                        if (tipo.equals("1")) {
                            us = new UserBando(user, bando, rs.getString("stato"), rs.getString("datareg"), rs.getString("timestamp"),
                                    rs.getString("password"), rs.getString("cell"), rs.getString("mail"), rs.getString("tipo"), getSiNo(user));
                        } else {
                            boolean att = bandoAttivoRaf(bando);
                            if (att) {
                                us = new UserBando(user, bando, rs.getString("stato"), rs.getString("datareg"), rs.getString("timestamp"),
                                        rs.getString("password"), rs.getString("cell"), rs.getString("mail"), rs.getString("tipo"), getSiNo(user));
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return us;
    }

    public UserBando getUser(String user, String psw) {
        UserBando us = null;
        try {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, psw);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String bando = rs.getString("codiceBando");
                        String tipo = rs.getString("tipo");
                        if (tipo.equals("1")) {
                            us = new UserBando(user, bando, rs.getString("stato"), rs.getString("datareg"), rs.getString("timestamp"),
                                    psw, rs.getString("cell"), rs.getString("mail"), rs.getString("tipo"), getSiNo(user));
                        } else {
                            boolean att = bandoAttivoRaf(bando);
                            if (att) {
                                us = new UserBando(user, bando, rs.getString("stato"), rs.getString("datareg"), rs.getString("timestamp"),
                                        psw, rs.getString("cell"), rs.getString("mail"), rs.getString("tipo"), getSiNo(user));
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return us;
    }

    public String getSiNo(String username) {
        String sino = "";
        try {
            String query = "select valore from usersvalori where campo='accreditato' and username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sino = rs.getString("valore");
                    }
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return sino;
    }

    public boolean esisteUserMail(String user, String mail) {
        boolean out = false;
        try {
            String sql = "SELECT * FROM users WHERE username = ? AND mail = ? AND stato = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, mail);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public boolean cambiaValoreUser(String username, String field, String value) {
        boolean es = false;
        try {
            String upd = "UPDATE users SET " + field + " = ? WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(upd)) {
                ps.setString(1, value);
                ps.setString(2, username);
                es = ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return es;
    }

    public ArrayList<String[]> listaCodDocRichiestiBando(String cod) {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT codicedoc,titolo FROM docbandi WHERE codbando = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String[] out = {rs.getString(1), rs.getString(2)};
                        val.add(out);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Docbandi> listaDocRichiestiBando(String cod) {
        ArrayList<Docbandi> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM docbandi WHERE codbando = ?  order by ordinamento,codicedoc";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Docbandi db = new Docbandi(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getString(6), rs.getInt(7), rs.getString(8),
                                rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13));
                        val.add(db);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Docuserbandi> listaDocUserBando(String cod, String username) {
        ArrayList<Docuserbandi> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM docuserbandi WHERE codbando = ? AND username = ? AND stato = ? ORDER BY datacar";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Docuserbandi db = new Docuserbandi(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9));
                        val.add(db);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Docuserbandi> listaDocUserBando(String cod, String username, String coddoc) {
        ArrayList<Docuserbandi> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM docuserbandi WHERE codbando = ? AND username = ? AND stato = ? AND codicedoc = ? order by datacar";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                ps.setString(3, "1");
                ps.setString(4, coddoc);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Docuserbandi db = new Docuserbandi(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9));
                        val.add(db);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public Docuserbandi docUserBando(String cod, String username, String codicedoc, String tipologia, String note) {
        Docuserbandi dub = null;
        try {
            String sql = "SELECT * FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND stato = ? AND tipodoc = ? AND note = ? order by datacar";
            PreparedStatement ps = this.c.prepareStatement(sql);
            ps.setString(1, cod);
            ps.setString(2, username);
            ps.setString(3, codicedoc);
            ps.setString(4, "1");
            ps.setString(5, tipologia);
            ps.setString(6, note);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dub = new Docuserbandi(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9));
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return dub;
    }

    public String nome_cognome_user(String username) {
        StringBuilder out = new StringBuilder("");
        try {
            String sql = "SELECT valore FROM usersvalori WHERE username= '" + username + "' AND (campo = 'nome' OR campo = 'cognome') ORDER BY campo";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    out.append(rs.getString(1).toLowerCase()).append(" ");
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return capitalize(out.toString().trim());
    }

    public ArrayList<UserValoriReg> listaValoriUserbandoCONV(String cod, String username) {
        ArrayList<UserValoriReg> li = new ArrayList<>();
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> tipo_doc = getTipoDoc();

            String sql = "SELECT * FROM usersvalori WHERE codbando = ? AND username = ? order by campo";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String campo = rs.getString(3);
                        String valore = rs.getString(4).toUpperCase().trim();
                        String valore1 = rs.getString(4).toUpperCase().trim();
                        switch (campo) {
                            case "comunecciaa":
                            case "sedecomune":
                                //comune
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                }
                                break;
                            case "regccciaa":
                            case "sederegione":
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                }
                                break;
                            case "tipdoc1":
                                if (!valore.equals("")) {
                                    Items it1 = tipo_doc.stream().filter(c1 -> c1.getCod().equals(valore)).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                break;
                            case "proccciaa":
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getProvincia();
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        UserValoriReg reg = new UserValoriReg(
                                rs.getString(1), rs.getString(2), campo,
                                valore1, rs.getString(5), rs.getString(6));
                        li.add(reg);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return li;
    }

    public ArrayList<UserValoriReg> listaValoriUserbando(String cod, String username) {
        ArrayList<UserValoriReg> li = new ArrayList<>();
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> tipo_doc = getTipoDoc();

            String sql = "SELECT * FROM usersvalori WHERE codbando = ? AND username = ? order by campo";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String campo = rs.getString(3);
                        String valore = rs.getString(4).toUpperCase().trim();
                        String valore1 = rs.getString(4).toUpperCase().trim();
                        switch (campo) {
                            case "comunecciaa":
                            case "sedecomune":
                                //comune
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                }
                                break;
                            case "regccciaa":
                            case "sederegione":
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                }
                                break;
                            case "tipdoc1":
                                if (!valore.equals("")) {
                                    Items it1 = tipo_doc.stream().filter(c1 -> c1.getCod().equals(valore)).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                break;
                            case "sedeprov":
                            case "proccciaa":
                                if (!valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getProvincia();
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        UserValoriReg reg = new UserValoriReg(
                                rs.getString(1), rs.getString(2), campo,
                                valore1, rs.getString(5), rs.getString(6));
                        li.add(reg);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return li;
    }

    public boolean insertDocUserBando(Docuserbandi dub) {
        try {
            if (dub.getTipodoc() == null) {
                String sql = "select COUNT(*) FROM docuserbandi WHERE username = '" + dub.getUsername() + "' AND codicedoc = 'RALL'";
                try ( Statement st1 = this.c.createStatement();  ResultSet rs1 = st1.executeQuery(sql)) {
                    if (rs1.next()) {
                        int valore = rs1.getInt(1) + 1;
                        dub.setTipodoc(String.valueOf(valore));
                    } else {
                        dub.setTipodoc("0");
                    }
                }
            }
            String ins = "INSERT INTO docuserbandi (codbando,username,codicedoc,stato,path,datacar,tipodoc,note) VALUES (?,?,?,?,?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, dub.getCodbando());
                ps.setString(2, dub.getUsername());
                ps.setString(3, dub.getCodicedoc());
                ps.setString(4, dub.getStato());
                ps.setString(5, dub.getPath());
                ps.setString(6, dub.getDatacar());
                ps.setString(7, dub.getTipodoc());
                ps.setString(8, replaeSpecialCharacter(dub.getNote()));
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public String getPath(String id) {
        String path = "-";
        try {
            String sql = "SELECT url FROM path WHERE id = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public String getPathDocUserBando(String bandorif, String username, String codicedoc) {
        String path = "-";
        try {
            String sql = "SELECT path FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND tipodoc = ? AND stato = ? ORDER BY timestamp DESC LIMIT 1";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, codicedoc);
                ps.setString(4, "-");
                ps.setString(5, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public String getPathDocUserBando(String username, String codicedoc) {
        String path = "-";
        try {
            String sql = "SELECT path FROM docuserbandi WHERE username = ? AND codicedoc = ? ORDER BY timestamp DESC LIMIT 1";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, codicedoc);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public String getPathDocUserBando(String bandorif, String username, String codicedoc, String tipologia, String note) {
        String path = "-";
        try {
            String sql = "SELECT path FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND tipodoc = ? AND stato = ? AND note = ? ORDER BY timestamp DESC LIMIT 1";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, codicedoc);
                ps.setString(4, tipologia);
                ps.setString(5, "1");
                ps.setString(6, note);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public String getPathDocModello(String bandorif, String codicedoc) {
        String path = "-";
        try {
            String sql = "SELECT download FROM docbandi WHERE codbando =  ? AND codicedoc = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, codicedoc);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public boolean insertDomandaDaCanc(String bandorif, String username, String datacanc, String codiceconferma) {
        try {
            String ins = "INSERT INTO annulladomande (codbando,username,datacanc,codiceconferma) VALUES (?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, datacanc);
                ps.setString(4, codiceconferma);
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public String[] controllaDomandadaEliminare(String codconf) {
        String[] v = new String[3];
        boolean ok = false;
        try {
            String sql = "SELECT * FROM annulladomande WHERE codiceconferma =  ? AND stato = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, codconf);
                ps.setString(2, "0");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        v[0] = rs.getString(1);
                        v[1] = rs.getString(2);
                        v[2] = rs.getString(3);
                        ok = true;
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        if (ok) {
            return v;
        }
        return null;
    }

    public boolean cambiaStatoUser(String username, String stato) {
        try {
            String upd = "UPDATE users SET stato = ? WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(upd)) {
                ps.setString(1, stato);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeAllDocUserBando(String codbando, String username) {
        try {
            String canc = "DELETE FROM docuserbandi WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeSingleDocUserBando(String codbando, String username, String codicedoc, String tipologia) {
        try {
            String canc = "DELETE FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND tipodoc = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.setString(3, codicedoc);
                ps.setString(4, tipologia);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeSingleDocUserBandoAltri(String codbando, String username, String codicedoc, String note) {
        try {
            String canc = "DELETE FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND note = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.setString(3, codicedoc);
                ps.setString(4, note);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeSingleDocUserBandoAltri(String codbando, String username, String codicedoc, String tipologia, String note) {
        try {
            String canc = "DELETE FROM docuserbandi WHERE codbando = ? AND username = ? AND codicedoc = ? AND tipodoc = ? AND note = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.setString(3, codicedoc);
                ps.setString(4, tipologia);
                ps.setString(5, note);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeAllValoriUserBando(String codbando, String username) {
        try {
            String canc = "DELETE FROM usersvalori WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeAllValoriUserBandoDOC1(String codbando, String username) {
        try {
            String canc = "DELETE FROM usersvalori WHERE codbando = ? AND username = ? AND campo IN (SELECT campo FROM domandaonline WHERE codbando = ? AND visibile = ?)";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.setString(3, codbando);
                ps.setString(4, "SI");
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean cambiaStatoRichiestaAnnulla(String id, String stato) {
        try {
            String upd = "UPDATE annulladomande SET stato = ? WHERE id = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(upd)) {
                ps.setString(1, stato);
                ps.setString(2, id);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public ArrayList<String[]> listCodFiscBando(String codbando) {
        ArrayList<String[]> liout = new ArrayList<>();
        ArrayList<String[]> liout2 = new ArrayList<>();
        try {
            String sql = "SELECT username,campo,valore FROM usersvalori WHERE codbando = ? and (campo = ? or campo = ?)";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, codbando);
                ps.setString(2, "cf");
                ps.setString(3, "linea");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String[] v1 = {rs.getString(1), rs.getString(2), rs.getString(3)};
                        liout.add(v1);
                    }
                    for (int i = 0; i < liout.size(); i++) {
                        String usr = liout.get(i)[0];
                        String out[] = new String[2];
                        for (int x = 0; x < liout.size(); x++) {
                            if (usr.equals(liout.get(x)[0])) {
                                if (liout.get(x)[1].equals("cf")) {
                                    out[0] = liout.get(x)[2];
                                } else if (liout.get(x)[1].equals("linea")) {
                                    out[1] = liout.get(x)[2];
                                }
                            }
                        }
                        liout2.add(out);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return liout2;
    }

    public boolean inviaDomanda(Domandecomplete doc) {
        try {
            String ins = "INSERT INTO domandecomplete (id,codbando,username,datainvio) VALUES (?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, doc.getId());
                ps.setString(2, doc.getCodbando());
                ps.setString(3, doc.getUsername());
                ps.setString(4, doc.getDatainvio());
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public Domandecomplete isPresenteDomandaCompleta(String bandorif, String username) {
        Domandecomplete dc = null;
        try {
            String sql = "SELECT * FROM domandecomplete WHERE codbando = ? and username = ? AND stato = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dc = new Domandecomplete(rs.getString(1), rs.getString(2), rs.getString(3),
                                rs.getString(4), rs.getString(5), rs.getString(6));
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return dc;
    }

    public Domandecomplete isDomandaCompletaConsolidata(String bandorif, String username) {
        Domandecomplete dc = null;
        try {
            String sql = "SELECT * FROM domandecomplete WHERE codbando = ? and username = ? AND stato = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dc = new Domandecomplete(rs.getString(1), rs.getString(7));
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return dc;
    }

    public boolean annullaDomandaCompleta(String bandorif, String username) {
        try {
            String upd = "UPDATE domandecomplete set stato = ? WHERE codbando = ? and username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(upd)) {
                ps.setString(1, "0");
                ps.setString(2, bandorif);
                ps.setString(3, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean insertTracking(String idUser, String azione) {
        try {
            String ins = "INSERT INTO tracking (idUser,azione) VALUES (?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, idUser);
                ps.setString(2, azione);
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
        }
        return false;
    }

    public ArrayList<String[]> getListaAllBando(String bando) {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM tipiall WHERE bandorif = ? order by descrizione";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bando);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String[] v1 = {rs.getString(1), rs.getString(2), rs.getString(3)};
                        val.add(v1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<String[]> getListaAllBandoRUP(String bando) {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM tipiall_rup WHERE bandorif = ? order by descrizione";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bando);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String[] v1 = {rs.getString(1), rs.getString(2), rs.getString(3)};
                        val.add(v1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Domandecomplete> listaconsegnatestato(String lineaintervento) {
        ArrayList<Domandecomplete> li = new ArrayList<>();
        try {
            String sql = "SELECT * FROM bando_dd_mcn where lineaintervento ='" + lineaintervento + "' and coddomanda<>'-'";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String decre = "";
                    if (rs.getString("decreto").equals("-") || rs.getString("datadecreto").equals("-")) {

                    } else {
                        decre = rs.getString("decreto") + " DEL " + rs.getString("datadecreto");
                    }

                    li.add(new Domandecomplete(rs.getString("coddomanda"), rs.getString("username"), rs.getString("nome"),
                            rs.getString("cognome"), rs.getString("cf"), rs.getString("dataconsegna"), rs.getString("comune"), rs.getString("protocollo"),
                            rs.getString("dataprotocollo"), rs.getString("stato_domanda"), rs.getString("protocollo"), decre));
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return li;
    }

    public ArrayList<Reportistica> listareports(String bandorif) {
        ArrayList<Reportistica> lc = new ArrayList<>();
        try {
            String sql = "SELECT * FROM reportistica WHERE bando = ? ORDER BY ordine";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bandorif);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Reportistica re = new Reportistica(rs.getString(1), rs.getString(2), rs.getString(3),
                                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
                                formatStringtoStringDate(rs.getString(9), timestampSQL, timestampITA, true),
                                rs.getString(10), rs.getString(11), rs.getString(12));
                        lc.add(re);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return lc;
    }

    public boolean removeAllcampiDomanda(String codbando, String username) {
        try {
            String canc = "DELETE FROM bando_dd_mcn WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean removeAllDomandaValue(String codbando, String username) {
        try {
            String canc = "UPDATE bandod3 SET stato= 'A' WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(canc)) {
                ps.setString(1, codbando);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean isDomandaAnnullata(String bandorif, String username) {
        boolean es = false;
        try {
            String sel = "SELECT id FROM annulladomande WHERE codbando = ? and username = ? AND stato = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sel)) {
                ps.setString(1, bandorif);
                ps.setString(2, username);
                ps.setString(3, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    es = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return es;
    }

    public String getPathFaq(String id) {
        String path = "--";
        try {
            String sql = "SELECT pathpdf FROM faq WHERE id=?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        path = rs.getString(1);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return path;
    }

    public String generaProt(String username) {
        String prot = "0";
        try {
            String ins = "INSERT INTO protocollidomande (username) VALUES (?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, username);
                ps.execute();
            }
            String select = "SELECT LPAD(id, 10, '0') FROM protocollidomande WHERE username = ? ORDER BY timestamp DESC LIMIT 1";
            try ( PreparedStatement ps1 = this.c.prepareStatement(select)) {
                ps1.setString(1, username);
                ResultSet rs = ps1.executeQuery();
                if (rs.next()) {
                    prot = rs.getString(1);
                }
                rs.close();
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return prot;
    }

    public ArrayList<String[]> comuni() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni order by nome,provincia";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] o = {leftPad(rs.getString(1), 5, "0"), rs.getString(2), rs.getString(4), rs.getString(5)};
                    val.add(o);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public boolean invioattivo(String bando) {
        boolean es = false;
        try {
            String sql = "SELECT codbando FROM elencobandi WHERE codbando = ? AND now()<datainizioclick OR now()>datafineclick AND attivo = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, bando);
                ps.setString(2, "SI");
                try ( ResultSet rs = ps.executeQuery()) {
                    es = rs.next();
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return es;
    }

    public ArrayList<String[]> getAllStatiEur() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM nazeur ORDER BY ordine";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] v1 = {rs.getString(2), rs.getString(2)};
                    val.add(v1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<String[]> getSesso() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM sesso order by sesso desc";
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] v1 = {rs.getString(1), rs.getString(1)};
                    val.add(v1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public ArrayList<Domandecomplete> listaconsegnatestato(String data_da, String data_a, String stato) {
        ArrayList<Domandecomplete> li = new ArrayList<>();
        try {
            String sql = "SELECT * FROM bando_dd_mcn where coddomanda <> '-'";
            if (!data_da.trim().equals("") && data_a.equals("")) {
                sql = sql + " and dataconsegna like '" + data_da + "%'";
            } else if (data_da.trim().equals("") && !data_a.equals("")) {
                sql = sql + " and dataconsegna like '" + data_a + "%'";
            } else if (!data_da.trim().equals("") && !data_a.equals("")) {
                sql = sql + " and dataconsegna > '" + data_da + " 00:00:00' and dataconsegna <'" + data_a + " 23:59:59'";
            }
            if (!stato.equals("")) {

                if (stato.equals("S") || stato.equals("A") || stato.equals("R")) {
                    sql = sql + " and stato_domanda = '" + stato + "'";
                } else {
                    sql = sql + " and stato_domanda = 'A'";
                }

            }

            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String decre = "";
                    if (rs.getString("decreto").equals("-") || rs.getString("datadecreto").equals("-")) {

                    } else {
                        decre = rs.getString("decreto") + " DEL " + rs.getString("datadecreto");
                    }
                    boolean convenzionedainviare = countDocumentConvenzioni(rs.getString("username")) == 3;
                    boolean convenzioneinviataROMA = getInvioEmailROMA(rs.getString("username")).equals("1");
                    boolean convenzionecaricatacontrofirmata = !getConvenzioneROMA(rs.getString("username")).trim().equals("");
                    Domandecomplete dc1 = new Domandecomplete(rs.getString("coddomanda"), rs.getString("username"), rs.getString("nome"),
                            rs.getString("cognome"), rs.getString("cf"), rs.getString("dataconsegna"),
                            rs.getString("societa"), rs.getString("pivacf"), rs.getString("pec"),
                            rs.getString("stato_domanda"), rs.getString("protocollo"), decre);

                    if (convenzionedainviare) {
                        if (convenzioneinviataROMA) {
                            if (convenzionecaricatacontrofirmata) {
                                dc1.setStatoDomanda("A2");
                            } else {
                                dc1.setStatoDomanda("A3");
                            }
                        } else {
                            dc1.setStatoDomanda("A1");
                        }
                    }

                    dc1.setConvenzionedainviare(convenzionedainviare);
                    dc1.setConvenzioneinviataROMA(convenzioneinviataROMA);
                    dc1.setConvenzionecaricatacontrofirmata(convenzionecaricatacontrofirmata);

                    if (stato.equals("") || stato.equals(dc1.getStatoDomanda())) {
                        li.add(dc1);
                    }

//                    if (stato.equals("") || stato.equals("S") || stato.equals("A") || stato.equals("R")) {
//                        li.add(dc1);
//                    } else {
//                        if (stato.equals("A1")) {
//                            if (convenzionedainviare) {
//                                if (convenzioneinviataROMA) {
//                                    if (!convenzionecaricatacontrofirmata) {
//                                        dc1.setStatoDomanda("A1");
//                                        li.add(dc1);
//                                    }
//                                } else {
//                                    dc1.setStatoDomanda("A1");
//                                    li.add(dc1);
//                                }
//                            }
//                        } else if (stato.equals("A2")) {
//                            if (convenzionedainviare) {
//                                if (convenzioneinviataROMA) {
//                                    if (convenzionecaricatacontrofirmata) {
//                                        dc1.setStatoDomanda("A2");
//                                        li.add(dc1);
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return li;
    }

    public boolean insAllegatoA(
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

        String insert = "INSERT INTO allegato_a VALUES ("
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" //16
                + ")";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(insert)) {
                ps.setString(1, username);
                ps.setString(2, enteistituzionepubblica);
                ps.setString(3, associazione);
                ps.setString(4, ordineprofessionale);
                ps.setString(5, soggettoprivato);
                ps.setString(6, formazione);
                ps.setString(7, regione1);
                ps.setString(8, iscrizione1);
                ps.setString(9, servizi);
                ps.setString(10, regione2);
                ps.setString(11, iscrizione2);
                ps.setString(12, ateco);
                ps.setString(13, numaule);

                ps.setString(14, indirizzo1);
                ps.setString(15, citta1);
                ps.setString(16, provincia1);
                ps.setString(17, regioneaula1);
                ps.setString(18, estremi1);
                ps.setString(19, accreditamento1);
                ps.setString(20, responsabile1);
                ps.setString(21, mailresponsabile1);
                ps.setString(22, telresponsabile1);
                ps.setString(23, amministrativo1);
                ps.setString(24, mailamministrativo1);
                ps.setString(25, telamministrativo1);

                ps.setString(26, indirizzo2);
                ps.setString(27, citta2);
                ps.setString(28, provincia2);
                ps.setString(29, regioneaula2);
                ps.setString(30, estremi2);
                ps.setString(31, accreditamento2);
                ps.setString(32, responsabile2);
                ps.setString(33, mailresponsabile2);
                ps.setString(34, telresponsabile2);
                ps.setString(35, amministrativo2);
                ps.setString(36, mailamministrativo2);
                ps.setString(37, telamministrativo2);

                ps.setString(38, indirizzo3);
                ps.setString(39, citta3);
                ps.setString(40, provincia3);
                ps.setString(41, regioneaula3);
                ps.setString(42, estremi3);
                ps.setString(43, accreditamento3);
                ps.setString(44, responsabile3);
                ps.setString(45, mailresponsabile3);
                ps.setString(46, telresponsabile3);
                ps.setString(47, amministrativo3);
                ps.setString(48, mailamministrativo3);
                ps.setString(49, telamministrativo3);

                ps.setString(50, indirizzo4);
                ps.setString(51, citta4);
                ps.setString(52, provincia4);
                ps.setString(53, regioneaula4);
                ps.setString(54, estremi4);
                ps.setString(55, accreditamento4);
                ps.setString(56, responsabile4);
                ps.setString(57, mailresponsabile4);
                ps.setString(58, telresponsabile4);
                ps.setString(59, amministrativo4);
                ps.setString(60, mailamministrativo4);
                ps.setString(61, telamministrativo4);

                ps.setString(62, indirizzo5);
                ps.setString(63, citta5);
                ps.setString(64, provincia5);
                ps.setString(65, regioneaula5);
                ps.setString(66, estremi5);
                ps.setString(67, accreditamento5);
                ps.setString(68, responsabile5);
                ps.setString(69, mailresponsabile5);
                ps.setString(70, telresponsabile5);
                ps.setString(71, amministrativo5);
                ps.setString(72, mailamministrativo5);
                ps.setString(73, telamministrativo5);
                ////////////////////////////////////
                ps.setString(74, attivita);
                ps.setString(75, destinatari);
                ps.setString(76, finanziamento);
                ps.setString(77, committente);
                ps.setString(78, periodo);
                ps.setString(79, attivita2);
                ps.setString(80, destinatari2);
                ps.setString(81, finanziamento2);
                ps.setString(82, committente2);
                ps.setString(83, periodo2);
                ps.setString(84, attivita3);
                ps.setString(85, destinatari3);
                ps.setString(86, finanziamento3);
                ps.setString(87, committente3);
                ps.setString(88, periodo3);
                ps.setString(89, attivita4);
                ps.setString(90, destinatari4);
                ps.setString(91, finanziamento4);
                ps.setString(92, committente4);
                ps.setString(93, periodo4);
                ps.setString(94, attivita5);
                ps.setString(95, destinatari5);
                ps.setString(96, finanziamento5);
                ps.setString(97, committente5);
                ps.setString(98, periodo5);
                ps.setString(99, noconsorzio);
                ps.setString(100, consorzio);
                ps.setString(101, nomeconsorzio);
                ps.setString(102, pec);
                ps.setString(103, numdocenti);

                //EXTRA
                ps.setString(104, area);
                ps.setString(105, area2);
                ps.setString(106, area3);
                ps.setString(107, area4);
                ps.setString(108, area5);

                //EXTRA
                ps.setString(109, titolo1);
                ps.setString(110, titolo2);
                ps.setString(111, titolo3);
                ps.setString(112, titolo4);
                ps.setString(113, titolo5);

                //PRIVACY
                ps.setString(114, privacy1);
                ps.setString(115, privacy2);

                ps.setString(116, curdate());
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean insAllegatoB(List<AllegatoB> dainserire, String datetime) {
        AtomicInteger error = new AtomicInteger(0);
        dainserire.forEach(allegatob -> {
            try {
                if (error.get() == 0) {
                    String insert = "INSERT INTO allegato_b VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    try ( PreparedStatement ps = this.c.prepareStatement(insert)) {
                        ps.setString(1, allegatob.getUsername());
                        ps.setString(2, valueOf(allegatob.getId()));
                        ps.setString(3, allegatob.getNome());
                        ps.setString(4, allegatob.getCognome());
                        ps.setString(5, allegatob.getCF());
                        ps.setString(6, allegatob.getComune());
                        ps.setString(7, allegatob.getDatanascita());
                        ps.setString(8, allegatob.getSesso());
                        ps.setString(9, allegatob.getRegione());
                        ps.setString(10, allegatob.getMail());
                        ps.setString(11, allegatob.getPec());
                        ps.setString(12, allegatob.getTel());
                        ps.setString(13, allegatob.getTitolistudio());
                        ps.setString(14, allegatob.getQualifiche());
                        ps.setString(15, allegatob.getFascia());
                        ps.setString(16, allegatob.getInquadramento());
                        ps.setString(17, datetime);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                insertTracking("ERROR SYSTEM", estraiEccezione(e));
                error.addAndGet(1);
            }
        });
        if (error.get() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean insertAllegatoC2(AllegatoC2 dainserire, String datetime) {
        try {
            String insert = "INSERT INTO allegato_c2 VALUES ("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," //20
                    + "?,?,?,?,?,?,?,?" //8
                    + ")";
            try ( PreparedStatement ps = this.c.prepareStatement(insert)) {
                ps.setString(1, dainserire.getUsername());
                ps.setString(2, dainserire.getProvincianascita());
                ps.setString(3, dainserire.getIndirizzoresidenza());
                ps.setString(4, dainserire.getCapresidenza());
                ps.setString(5, dainserire.getCittaresidenza());
                ps.setString(6, dainserire.getBanca_nome());
                ps.setString(7, dainserire.getBanca_agenzia());
                ps.setString(8, dainserire.getBanca_intestatario());
                ps.setString(9, dainserire.getIban());
                ps.setString(10, dainserire.getUnicosi());

                ps.setString(11, dainserire.getUnicono());
                ps.setString(12, dainserire.getSoggetto1_nome());
                ps.setString(13, dainserire.getSoggetto1_cognome());
                ps.setString(14, dainserire.getSoggetto1_cf());
                ps.setString(15, dainserire.getSoggetto1_luogonascita());
                ps.setString(16, dainserire.getSoggetto1_datanascita());
                ps.setString(17, dainserire.getSoggetto1_sesso());
                ps.setString(18, dainserire.getSoggetto1_indirizzoresidenza());
                ps.setString(19, dainserire.getSoggetto1_cittaresidenza());
                ps.setString(20, dainserire.getSoggetto2_nome());

                ps.setString(21, dainserire.getSoggetto2_cognome());
                ps.setString(22, dainserire.getSoggetto2_cf());
                ps.setString(23, dainserire.getSoggetto2_luogonascita());
                ps.setString(24, dainserire.getSoggetto2_datanascita());
                ps.setString(25, dainserire.getSoggetto2_sesso());
                ps.setString(26, dainserire.getSoggetto2_indirizzoresidenza());
                ps.setString(27, dainserire.getSoggetto2_cittaresidenza());
                ps.setString(28, datetime);
                ps.executeUpdate();

            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;

    }

    public boolean verificaDomandaCompleta(String username) {
        boolean es = false;
        try {
            String query = "select username from domandecomplete where username=?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    es = rs.next();
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public boolean insAllegatoB1(String idallegato_b1, String username, String allegatocv,
            String allegatodr, String allegatob1) {
        String query = "insert into allegato_b1 (idallegato_b1,username,allegatocv,allegatodr,allegatob1) values (?,?,?,?,?)";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, idallegato_b1);
                ps.setString(2, username);
                ps.setString(3, allegatocv);
                ps.setString(4, allegatodr);
                ps.setString(5, allegatob1);
                ps.execute();
            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean isPresenzaAllegatoB1(String idallegato_b1, String username) {
        boolean es = false;
        String query = "select * from allegato_b1 where idallegato_b1=? and username=?";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, idallegato_b1);
                ps.setString(2, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    es = rs.next();
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public ArrayList<AllegatoB1> getAllegatoB1(String username, String id) {
        ArrayList<AllegatoB1> al = new ArrayList<>();
        try {
            String query = "select * from allegato_b1 where username=? and idallegato_b1=?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        AllegatoB1 b1 = new AllegatoB1();
                        b1.setId(rs.getString("idallegato_b1"));
                        b1.setAllegatob1(rs.getString("allegatob1"));
                        b1.setAllegatocv(rs.getString("allegatocv"));
                        b1.setAllegatodr(rs.getString("allegatodr"));
                        al.add(b1);
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return al;
    }

    public boolean delAllegatiDocenti(String username, String id) {
        boolean es = false;
        try {
            String query = "DELETE FROM allegato_b1 WHERE username = ? AND idallegato_b1 = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, id);
                int x = ps.executeUpdate();
                if (x > 0) {
                    String query1 = "SELECT username FROM allegato_b1 WHERE username = ?";
                    try ( PreparedStatement ps1 = this.c.prepareStatement(query1)) {
                        ps1.setString(1, username);
                        if (!ps1.executeQuery().next()) {
                            String del = "DELETE FROM docuserbandi WHERE username = ? AND codicedoc = ?";
                            try ( PreparedStatement ps2 = this.c.prepareStatement(del)) {
                                ps2.setString(1, username);
                                ps2.setString(2, "ALB1");
                                ps2.execute();
                            }
                            es = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean delAllAllegatiDocenti(String username) {
        boolean es = false;
        try {
            String query = "delete from allegato_b1 where username=?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                es = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public ArrayList<DocumentiDocente> listaDocUserBandoDocenti(String username, boolean withcontent) {
        ArrayList<DocumentiDocente> val = new ArrayList<>();
        try {
            String sql = "SELECT * FROM allegato_b1 WHERE username = ?";
            if (!withcontent) {
                sql = "SELECT idallegato_b1,data FROM allegato_b1 WHERE username = ?";
            }
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DocumentiDocente dd = new DocumentiDocente();
                        dd.setCoddoc(rs.getString("idallegato_b1"));
                        if (withcontent) {
                            dd.setCv(rs.getString("allegatocv"));
                            dd.setDr(rs.getString("allegatodr"));
                            dd.setB1(rs.getString("allegatob1"));
                        }
                        dd.setData(rs.getString("data"));
                        String sql2 = "SELECT nome,cognome FROM allegato_b WHERE username = ? AND id = ?";
                        try (final PreparedStatement ps2 = this.c.prepareStatement(sql2)) {
                            ps2.setString(1, username);
                            ps2.setString(2, rs.getString("idallegato_b1"));
                            try ( ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    dd.setNomecognome(rs2.getString("nome") + " " + rs2.getString("cognome"));
                                    val.add(dd);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public boolean insAllegatoB1Field(String iddocente, String username, String periodo, String durata, String incarico,
            String finanziamento, String attivita, String committente, String rif) {

        try {
            String query = "INSERT INTO allegato_b1_field (iddocente,username,periodo,durata,incarico,finanziamento,attivita,committente,rif) VALUES (?,?,?,?,?,?,?,?,?)";
            int i;
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, iddocente);
                ps.setString(2, username);
                ps.setString(3, periodo);
                ps.setString(4, durata);
                ps.setString(5, incarico);
                ps.setString(6, finanziamento);
                ps.setString(7, attivita);
                ps.setString(8, committente);
                ps.setString(9, rif);
                i = ps.executeUpdate();
            }
            return i > 0;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean delAllegatoB1Field(String id, String username) {
        String query = "delete from allegato_b1_field where iddocente=? and username=?";
        try {
            int i;
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, id);
                ps.setString(2, username);
                i = ps.executeUpdate();
            }
            return i > 0;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean delAllegatoB1Field(String username) {
        String query = "delete from allegato_b1_field where username=?";
        try {
            int i;
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                i = ps.executeUpdate();
            }
            return i > 0;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public HashMap<String, String> getAllegato_B(String username, String iddocente) {
        HashMap<String, String> map = new HashMap<>();
        String query = "select * from allegato_b where username = ? AND id = ?";
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> titolistudio = query_titolistudio_rc();
            ArrayList<Items> qualifiche = query_qualificazione_rc();
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, iddocente);
                try ( ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsmd.getColumnName(i);
                        map.put(name, "");
                    }
                    if (rs.next()) {
                        SortedSet<String> indici = new TreeSet<>(map.keySet());
                        indici.forEach(ind -> {
                            try {
                                String valore = rs.getString(ind).toUpperCase().trim();
                                String valore1 = rs.getString(ind).toUpperCase().trim();
                                if (ind.startsWith("regione") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                } else if (ind.startsWith("comune") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getId() == parseIntR(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                } else if (ind.startsWith("titolistudio") && !valore.equals("")) {
                                    Items it1 = titolistudio.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                } else if (ind.startsWith("qualifiche") && !valore.equals("")) {
                                    Items it1 = qualifiche.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                map.replace(ind, valore1);
                            } catch (Exception e) {
                                insertTracking("ERROR SYSTEM", estraiEccezione(e));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return map;
    }

    public HashMap<String, String> getAllegato_B1(String username, String iddocente) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> mapdest = new HashMap<>();
        String query = "select * from allegato_b1_field where username = ? and iddocente = ?";
        try {
            ArrayList<Items> unitamisura = query_unitamisura_rc();
            ArrayList<Items> attivita = query_attivita_docenti_rc();
            ArrayList<Items> inquadr = query_inquadramento_rc();
            ArrayList<Items> fonti = query_fontifin_rc();
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, iddocente);
                try ( ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsmd.getColumnName(i);
                        if (name.equals("periodo")) {
                            map.put(name, "");
                            mapdest.put(name + iddocente, "");
                            map.put("um", "");
                            map.put("du", "");
                            mapdest.put("um" + iddocente, "");
                            mapdest.put("du" + iddocente, "");
                        } else {
                            map.put(name, "");
                            mapdest.put(name + iddocente, "");
                        }

                    }
                    if (rs.next()) {
                        SortedSet<String> indici = new TreeSet<>(map.keySet());
                        indici.forEach(ind -> {
                            try {
                                switch (ind) {
                                    case "um": {
                                        String valore = rs.getString("durata").split("-")[1].trim();
                                        Items it1 = unitamisura.stream().filter(c1 -> (c1.getCod().equals(valore))).findAny().orElse(new Items(valore, valore));
                                        String valore1 = it1.getDescrizione();
                                        map.replace(ind, valore1);
                                        mapdest.replace(ind + iddocente, valore1);
                                        break;
                                    }
                                    case "du": {
                                        String valore1 = rs.getString("durata").split("-")[0].trim();
                                        map.replace(ind, valore1);
                                        mapdest.replace(ind + iddocente, valore1);
                                        break;
                                    }
                                    default: {
                                        String valore = rs.getString(ind).toUpperCase().trim();
                                        String valore1 = rs.getString(ind).toUpperCase().trim();
                                        if (ind.startsWith("incarico") && !valore.equals("")) {
                                            Items it1 = inquadr.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                            valore1 = it1.getDescrizione();
                                        } else if (ind.startsWith("attivita") && !valore.equals("")) {
                                            Items it1 = attivita.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                            valore1 = it1.getDescrizione();
                                        } else if (ind.startsWith("finanziamento") && !valore.equals("")) {
                                            Items it1 = fonti.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                            valore1 = it1.getDescrizione();
                                        }
                                        map.replace(ind, valore1);
                                        mapdest.replace(ind + iddocente, valore1);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                insertTracking("ERROR SYSTEM", estraiEccezione(e));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return mapdest;
    }

    public AllegatoC2 getAllegatoC2(String username) {
        AllegatoC2 out = null;
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            String query = "SELECT * FROM allegato_c2 WHERE username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {

                        String soggetto1_luogonascita = "";
                        String soggetto2_luogonascita = "";
                        String luogonascita1 = rs.getString("soggetto1_luogonascita");
                        String luogonascita2 = rs.getString("soggetto2_luogonascita");

                        if (!luogonascita1.equals("")) {
                            Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(luogonascita1))).findAny().orElse(null);
                            if (c0 != null) {
                                soggetto1_luogonascita = c0.getNome();
                            }
                        }
                        if (!luogonascita2.equals("")) {
                            Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(luogonascita2))).findAny().orElse(null);
                            if (c0 != null) {
                                soggetto2_luogonascita = c0.getNome();
                            }
                        }

                        out = new AllegatoC2(username, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                                rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10),
                                rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), soggetto1_luogonascita,
                                rs.getString(16), rs.getString(17), rs.getString(18), rs.getString(19), rs.getString(20),
                                rs.getString(21), rs.getString(22), soggetto2_luogonascita, rs.getString(24), rs.getString(25),
                                rs.getString(26), rs.getString(27)
                        );
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public HashMap<String, String> getAllegatoA(String username) {
        HashMap<String, String> map = new HashMap<>();
        String query = "SELECT * FROM allegato_a WHERE username = ?";
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> aree = query_aree_rc();
            ArrayList<Items> fonti = query_fontifin_rc();
            ArrayList<Items> disponibilita = query_disponibilita_rc();

            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsmd.getColumnName(i);
                        map.put(name, "");
                    }
                    if (rs.next()) {
                        SortedSet<String> indici = new TreeSet<>(map.keySet());
                        indici.forEach(ind -> {
                            try {
                                String valore = rs.getString(ind).toUpperCase().trim();
                                String valore1 = rs.getString(ind).toUpperCase().trim();
                                if (ind.startsWith("citta") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                } else if (ind.startsWith("regione") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                } else if (ind.startsWith("provincia") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getProvincia();
                                    }
                                } else if (ind.startsWith("titolo") && !valore.equals("")) {
                                    Items it1 = disponibilita.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                } else if (ind.startsWith("finanziamento") && !valore.equals("")) {
                                    Items it1 = fonti.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                } else if (ind.startsWith("area") && !valore.equals("")) {
                                    Items it1 = aree.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                map.replace(ind, valore1);
                            } catch (Exception e) {
                                insertTracking("ERROR SYSTEM", estraiEccezione(e));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return map;
    }

    public List<AllegatoB> getAllegatoB(String username) {

        List<AllegatoB> out = new ArrayList<>();

        ArrayList<Items> inquadr = query_inquadramento_rc();
        String query = "select * from allegato_b where username = ? ORDER BY id";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {

                        String inquadramento = rs.getString("inquadramento").trim();
                        String inquadramento1 = rs.getString("inquadramento").trim();

                        if (!inquadramento.equals("")) {
                            Items it1 = inquadr.stream().filter(c1 -> (c1.getCodice() == parseIntR(inquadramento))).findAny().orElse(new Items(inquadramento, inquadramento));
                            inquadramento1 = it1.getDescrizione().toUpperCase();
                        }

                        out.add(
                                new AllegatoB(
                                        rs.getString("username"), rs.getInt("id"), rs.getString("nome"), rs.getString("cognome"),
                                        rs.getString("cf"), rs.getString("comune"), rs.getString("datanascita"), rs.getString("sesso"),
                                        rs.getString("regione"), rs.getString("mail"), rs.getString("pec"), rs.getString("tel"),
                                        rs.getString("titolistudio"), rs.getString("qualifiche"), rs.getString("fascia"), inquadramento1
                                )
                        );
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public AllegatoB getAllegatoB(String username, String id) {
        AllegatoB out = null;
        try {
            String query = "select * from allegato_b where username = ? AND id = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new AllegatoB(
                                rs.getString("username"), rs.getInt("id"), rs.getString("nome"), rs.getString("cognome"),
                                rs.getString("cf"), rs.getString("comune"), rs.getString("datanascita"), rs.getString("sesso"),
                                rs.getString("regione"), rs.getString("mail"), rs.getString("pec"), rs.getString("tel"),
                                rs.getString("titolistudio"), rs.getString("qualifiche"), rs.getString("fascia"), rs.getString("inquadramento")
                        );
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public List<AllegatoB1_field> alb1(String user, String iddoc) {
        String query = "select * from allegato_b1_field where username = ? and iddocente = ?";
        List<AllegatoB1_field> al = new ArrayList<>();
        try {
            ArrayList<Items> unitamisura = query_unitamisura_rc();
            ArrayList<Items> attivita = query_attivita_docenti_rc();
            ArrayList<Items> inquadr = query_inquadramento_rc();
            ArrayList<Items> fonti = query_fontifin_rc();

            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, user);
                ps.setString(2, iddoc);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AllegatoB1_field alb1 = new AllegatoB1_field();

                        String att = rs.getString("attivita");
                        Items it_attivita = attivita.stream().filter(c1 -> (c1.getCodice() == parseIntR(att))).findAny().orElse(new Items(att, att));
                        alb1.setAttivita(it_attivita.getDescrizione());

                        String inc = rs.getString("incarico");
                        Items it_incarico = inquadr.stream().filter(c1 -> (c1.getCodice() == parseIntR(inc))).findAny().orElse(new Items(inc, inc));
                        alb1.setIncarico(it_incarico.getDescrizione());

                        String fin = rs.getString("finanziamento");
                        Items it_finanziamento = fonti.stream().filter(c1 -> (c1.getCodice() == parseIntR(fin))).findAny().orElse(new Items(fin, fin));
                        alb1.setFinanziamento(it_finanziamento.getDescrizione());

                        alb1.setCommittente(rs.getString("committente"));
                        alb1.setDurata(rs.getString("durata").split("-")[0].trim());

                        String um = rs.getString("durata").split("-")[1].trim();
                        Items it_unitamisura = unitamisura.stream().filter(c1 -> (c1.getCod().equals(um))).findAny().orElse(new Items(um, um));
                        alb1.setUm(it_unitamisura.getDescrizione());

                        alb1.setIddocente(rs.getString("iddocente"));
                        alb1.setPeriodo(rs.getString("periodo"));
                        alb1.setRif(rs.getString("rif"));
                        alb1.setUsername(rs.getString("username"));
                        al.add(alb1);
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return al;
    }

    public boolean presenzaAllB1Field(String user, String iddoc) {
        boolean out = false;
        try {
            String query = "select * from allegato_b1_field where username=? and iddocente=?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, user);
                ps.setString(2, iddoc);
                try ( ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public int getDocentiAllegatoA(String username) {
        int out = -1;
        try {
            String query = "select numdocenti from allegato_a where username=?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = rs.getInt("numdocenti");
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public ArrayList<String> getUsername() {
        String query = "select username from usersvalori where username in (select username from domandecomplete) and username not in (select username from bando_dd_mcn) group by username";
        ArrayList<String> al = new ArrayList<>();
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    al.add(rs.getString("username"));
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return al;
    }

    public ArrayList<String[]> getDescDocumenti() {
        ArrayList<String[]> val = new ArrayList<>();
        try {
            String query = "SELECT id,descrizione FROM doc_validi";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] v1 = {rs.getString(1), rs.getString(2)};
                    val.add(v1);
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return val;
    }

    public String expexcel_ricerca(String data_da, String data_a) {
        String generatedString = randomAlphabetic(7);
        ArrayList<String[]> documenti = getDescDocumenti();
//        try {
//            String sql = "SELECT * FROM bando_dd_mcn where coddomanda<>'-'";
//            if (!data_da.trim().equals("") && data_a.equals("")) {
//                sql = sql + " and dataconsegna like '" + data_da + "%'";
//            } else if (!data_da.trim().equals("") && !data_a.equals("")) {
//                sql = sql + " and dataconsegna>'" + data_da + " 00:00:00' and dataconsegna<'" + data_a + " 23:59:59'";
//            }
//            PreparedStatement ps = this.c.prepareStatement(sql + " limit 1000");
//            ResultSet rs = ps.executeQuery();
//            XSSFWorkbook workbook = new XSSFWorkbook();
//            File xl = new File("/mnt/bando/bandoba0h8/temp/" + generatedString + "bando_dd_mcn.xlsx");
//            if (rs.next()) {
//                XSSFSheet sheet = workbook.createSheet("DOMANDE CONSEGNATE");
//                XSSFFont font = workbook.createFont();
//                font.setFontName(HSSFFont.FONT_ARIAL);
//                font.setFontHeightInPoints((short) 12);
//                font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
//                XSSFCellStyle style1 = (XSSFCellStyle) workbook.createCellStyle();
//                style1.setFillBackgroundColor(new XSSFColor());
//                style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(192, 192, 192)));
//                style1.setFillPattern(CellStyle.SOLID_FOREGROUND);
//                style1.setBorderTop(XSSFCellStyle.BORDER_THIN);
//                style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
//                style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
//                style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
//                style1.setFont(font);
//                XSSFFont font2 = workbook.createFont();
//                font2.setFontName(HSSFFont.FONT_ARIAL);
//                font2.setFontHeightInPoints((short) 12);
//                XSSFCellStyle style2 = (XSSFCellStyle) workbook.createCellStyle();
//                style2.setBorderTop(XSSFCellStyle.BORDER_THIN);
//                style2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
//                style2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
//                style2.setBorderRight(XSSFCellStyle.BORDER_THIN);
//                style2.setFont(font2);
//                int cntriga = 1;
//                Row rowP = sheet.createRow((short) cntriga);
//                Cell cl = rowP.createCell(1);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Codice domanda");
//                cl = rowP.createCell(2);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Username");
//                cl = rowP.createCell(3);
//                cl.setCellStyle(style1);
//                cl.setCellValue("SA già accreditato");
//                cl = rowP.createCell(4);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Cognome");
//                cl = rowP.createCell(5);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Nome");
//                cl = rowP.createCell(6);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Nato a");
//                cl = rowP.createCell(7);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Data nascita");
//                cl = rowP.createCell(8);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Carica societaria");
//                cl = rowP.createCell(9);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Società");
//                cl = rowP.createCell(10);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Comune sede");
//                cl = rowP.createCell(11);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Provincia sede");
//                cl = rowP.createCell(12);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Indirizzo sede");
//                cl = rowP.createCell(13);
//                cl.setCellStyle(style1);
//                cl.setCellValue("CAP sede");
//                cl = rowP.createCell(14);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Codice fiscale");
//                cl = rowP.createCell(15);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Partita iva/Codice fiscale SA");
//                cl = rowP.createCell(16);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Comune CCIAA");
//                cl = rowP.createCell(17);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Provincia CCIAA");
//                cl = rowP.createCell(18);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Numero REA");
//                cl = rowP.createCell(19);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Matricola Inps");
//                cl = rowP.createCell(20);
//                cl.setCellStyle(style1);
//                cl.setCellValue("PEC");
//                cl = rowP.createCell(21);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Email");
//                cl = rowP.createCell(22);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Idoneità");
//                cl = rowP.createCell(23);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Cellulare");
//                cl = rowP.createCell(24);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Tipo documento");
//                cl = rowP.createCell(25);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Numero documento");
//                cl = rowP.createCell(26);
//                cl.setCellStyle(style1);
//                cl.setCellValue("Data consegna");
//                rs.beforeFirst();
//                while (rs.next()) {
//                    cntriga++;
//                    rowP = sheet.createRow((short) cntriga);
//                    cl = rowP.createCell(1);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("coddomanda"));
//                    cl = rowP.createCell(2);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("username"));
//                    cl = rowP.createCell(3);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("accreditato"));
//                    cl = rowP.createCell(4);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("cognome").toUpperCase());
//                    cl = rowP.createCell(5);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("nome").toUpperCase());
//                    cl = rowP.createCell(6);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("nato_a").toUpperCase());
//                    cl = rowP.createCell(7);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("data").toUpperCase());
//                    cl = rowP.createCell(8);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("carica").toUpperCase());
//                    cl = rowP.createCell(9);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("societa").toUpperCase());
//                    cl = rowP.createCell(10);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("sedecomune").toUpperCase());
//                    cl = rowP.createCell(11);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("sedeprovincia").toUpperCase());
//                    cl = rowP.createCell(12);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("sedeindirizzo").toUpperCase());
//                    cl = rowP.createCell(13);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("sedecap").toUpperCase());
//                    cl = rowP.createCell(14);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("cf").toUpperCase());
//                    cl = rowP.createCell(15);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("pivacf").toUpperCase());
//                    cl = rowP.createCell(16);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("cciaacomune").toUpperCase());
//                    cl = rowP.createCell(17);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("cciaaprovincia").toUpperCase());
//                    cl = rowP.createCell(18);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("rea").toUpperCase());
//                    cl = rowP.createCell(19);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("matricolainps").toUpperCase());
//                    cl = rowP.createCell(20);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("pec").toUpperCase());
//                    cl = rowP.createCell(21);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("mail").toUpperCase());
//                    cl = rowP.createCell(22);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("idoneo").toUpperCase());
//                    cl = rowP.createCell(23);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("cellulare").toUpperCase());
//
//                    cl = rowP.createCell(24);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(formatAL(rs.getString("tipdoc").toUpperCase(), documenti, 1));
//                    cl = rowP.createCell(25);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("docric"));
//                    cl = rowP.createCell(26);
//                    cl.setCellStyle(style2);
//                    cl.setCellValue(rs.getString("dataconsegna").toUpperCase());
//                }
//                for (int c = 1; c < 60; c++) {
//                    sheet.autoSizeColumn(c);
//                }
//            }
//            FileOutputStream out = new FileOutputStream(xl);
//            workbook.write(out);
//            out.close();
//            //String base64 = new String(Base64.encodeBase64(FileUtils.readFileToByteArray(xl)));
//            return "/mnt/bando/bandoba0h8/temp/" + generatedString + "bando_dd_mcn.xlsx";
//
//        } catch (SQLException | IOException ex) {
//            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
//        }
        return null;
    }

    public Domandecomplete domandeComplete(String cod, String username) {
        Domandecomplete dc = new Domandecomplete();
        try {
            String sql = "SELECT * FROM usersvalori WHERE codbando = ? AND username = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cod);
                ps.setString(2, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String campo = rs.getString("campo");
                        dc.setCodbando(bando);
                        dc.setUsername(username);
                        if (campo.equals("accreditato")) {
                            dc.setAccreditato(rs.getString("valore"));
                        }
                        if (campo.equals("caricasoc")) {
                            dc.setCarica(rs.getString("valore"));
                        }
                        if (campo.equals("cell")) {
                            dc.setCellulare(rs.getString("valore"));
                        }
                        if (campo.equals("cf")) {
                            dc.setCf(rs.getString("valore"));
                        }
                        if (campo.equals("cognome")) {
                            dc.setCognome(rs.getString("valore"));
                        }
                        if (campo.equals("comunecciaa")) {
                            dc.setCciaacomune(rs.getString("valore"));
                        }
                        if (campo.equals("data")) {
                            dc.setData(rs.getString("valore"));
                        }
                        if (campo.equals("docric1")) {
                            dc.setDocric(rs.getString("valore"));
                        }
                        if (campo.equals("email")) {
                            dc.setMail(rs.getString("valore"));
                        }
                        if (campo.equals("idoneo")) {
                            dc.setIdoneo(rs.getString("valore"));
                        }
                        if (campo.equals("matricolainps")) {
                            dc.setMatricolainps(rs.getString("valore"));
                        }
                        if (campo.equals("nato")) {
                            dc.setNato_a(rs.getString("valore"));
                        }
                        if (campo.equals("nome")) {
                            dc.setNome(rs.getString("valore"));
                        }
                        if (campo.equals("pec")) {
                            dc.setPec(rs.getString("valore"));
                        }
                        if (campo.equals("piva")) {
                            dc.setPivacf(rs.getString("valore"));
                        }
                        if (campo.equals("proccciaa")) {
                            dc.setCciaaprovincia(rs.getString("valore"));
                        }
                        if (campo.equals("rea")) {
                            dc.setRea(rs.getString("valore"));
                        }
                        if (campo.equals("sedecap")) {
                            dc.setSedecap(rs.getString("valore"));
                        }
                        if (campo.equals("sedecomune")) {
                            dc.setSedecomune(rs.getString("valore"));
                        }
                        if (campo.equals("sedeindirizzo")) {
                            dc.setSedeindirizzo(rs.getString("valore"));
                        }
                        if (campo.equals("sedeprov")) {
                            dc.setSedeprovincia(rs.getString("valore"));
                        }
                        if (campo.equals("societa")) {
                            dc.setSocieta(rs.getString("valore"));
                        }
                        if (campo.equals("tipdoc1")) {
                            dc.setTipdoc(rs.getString("valore"));
                        }
                        String valoriDomandaCompleta[] = getCodiceDomanda(username);
                        dc.setCoddomanda(valoriDomandaCompleta[0]);
                        dc.setDataconsegna(valoriDomandaCompleta[1]);
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return dc;
    }

    public String[] getCodiceDomanda(String username) {
        String query = "select id,timestamp from domandecomplete where username=?";
        String valori[] = new String[2];
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        valori[0] = rs.getString("id");
                        valori[1] = rs.getString("timestamp");
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return valori;
    }

    public boolean isPresenzaDocumento(String username, String tipologia) {
        boolean out = false;
        try {
            String query = "select username from docuserbandi where username='" + username + "' and codicedoc='" + tipologia + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery(query)) {
                out = rs.next();
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    public boolean setStatoDomandaAccRif(String username, String stato, String protocollo, String motivazione, String decreto, String datadecreto) {
        boolean es = false;
        try {
            String query = "UPDATE bando_dd_mcn SET stato_domanda = ?, protocollo = ?, rigetto = ?, decreto = ?, datadecreto = ? WHERE username = ? AND stato_domanda = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, stato);
                ps.setString(2, protocollo);
                ps.setString(3, motivazione);
                ps.setString(4, decreto);
                ps.setString(5, datadecreto);

                ps.setString(6, username);
                ps.setString(7, "S");
                System.out.println("it.refill.db.Db_Bando.setStatoDomandaAccRif() " + ps.toString());
                es = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public String getDecretoDomandaInviata(String username) {
        String out = "";
        try {
            String query = "SELECT decreto,datadecreto FROM bando_dd_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString(1).equals("-") || rs.getString(2).equals("-")) {

                    } else {
                        out = rs.getString(1) + " DEL " + rs.getString(2);
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out = "";
        }
        return out;
    }

    public String[] getDecreto(String username) {
        String[] out = {"", ""};
        try {
            String query = "SELECT decreto,datadecreto FROM bando_dd_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out[0] = rs.getString(1);
                    out[1] = rs.getString(2);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out[0] = "";
            out[1] = "";
        }
        return out;
    }

    public String[] getDecretoNEET(String username) {
        String[] out = {"", ""};
        try {
            String query = "SELECT decreto,datadecreto FROM bando_neet_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out[0] = rs.getString(1);
                    out[1] = rs.getString(2);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out[0] = "";
            out[1] = "";
        }
        return out;
    }

    public String getProtocolloNEET(String username) {
        String out = "";
        try {
            String query = "SELECT protocollo FROM bando_neet_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out = "";
        }
        return out;
    }

    public String getStatoDomandaInviata(String username) {
        String out = "";
        try {
            String query = "SELECT stato_domanda FROM bando_dd_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out = "";
        }
        return out;
    }

    public String getCodiceDomandaInviata(String username) {
        String out = "";
        try {
            String query = "SELECT coddomanda FROM bando_dd_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out = "";
        }
        return out;
    }

    public String getProtocolloDomandaInviata(String username) {
        String out = "";
        try {
            String query = "SELECT protocollo FROM bando_dd_mcn WHERE username ='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
            out = "";
        }
        return out;
    }

    public String getDataInvioDomanda(String username) {
        String datainvio = "";
        try {
            String query = "select dataconsegna from bando_dd_mcn where username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    datainvio = formatStringtoStringDate(rs.getString(1), timestampSQL, patternITA, false);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return datainvio;
    }

    public boolean getStatoDomanda(String username) {
        boolean es = false;
        try {
            String query = "select username from bando_dd_mcn where stato_domanda='A' and username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                es = rs.next();
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public boolean insertDocumentUserConvenzioni(Docuserconvenzioni dub) {
        try {
            String ins = "INSERT INTO docuserconvenzioni (codbando,username,codicedoc,path) VALUES (?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, dub.getCodbando());
                ps.setString(2, dub.getUsername());
                ps.setString(3, dub.getCodicedoc());
                ps.setString(4, dub.getPath());
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public boolean verPresenzaConvenzioni(String username, String coddoc) {
        boolean es = false;
        try {
            String query = "select username from docuserconvenzioni where username = '" + username + "' and codicedoc = '" + coddoc + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                es = rs.next();
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public String getPathConvenzioni(String username, String coddoc) {
        String var = "";
        try {
            String query = "select path from docuserconvenzioni where username = '" + username + "' and codicedoc = '" + coddoc + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getString("path");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public boolean remDocConvenzioni(String username, String coddoc) {
        try {
            String query = "delete from docuserconvenzioni where username = '" + username + "' and codicedoc = '" + coddoc + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean verificaPresenzaConvenzioni(String username, String coddoc) {
        boolean es = false;
        try {
            String query = "select username from docuserconvenzioni where username = '" + username + "' and codicedoc = '" + coddoc + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                es = rs.next();
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public int countDocumentConvenzioni(String username) {
        int var = 0;
        try {
            String query = "select count(*) from docuserconvenzioni where username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public boolean setStatoInvioDocumenti(String username) {
        boolean es = false;
        try {
            String query = "update docuserconvenzioni set stato='1' where username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                es = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public boolean verificaInvioConvenzione(String username) {
        boolean es = false;
        try {
            String query = "select username from docuserconvenzioni where username = '" + username + "' and stato='1'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                es = rs.next();
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public String getDataInvioConvenzione(String username) {
        String es = "";
        try {
            String query = "select tipodoc from docuserconvenzioni where username = '" + username + "' AND sendmail='1'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    es = formatStringtoStringDate(rs.getString("tipodoc"), timestampSQL, patternITA, false);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public ArrayList<Docuserconvenzioni> getDocumentiConvenzioni(String username) {
        ArrayList<Docuserconvenzioni> al = new ArrayList<>();
        try {
            String query = "select * from docuserconvenzioni where username = '" + username + "' and stato='1'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Docuserconvenzioni d = new Docuserconvenzioni();
                    d.setCodicedoc(rs.getString("codicedoc"));
                    d.setPath(rs.getString("path"));
                    d.setNote("-");
                    d.setUsername(rs.getString("username"));
                    d.setDatacar(rs.getString("timestamp"));
                    al.add(d);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return al;
    }

    public boolean settaInvioEmailROMA(String username, String datainvio) {
        boolean es = false;
        try {
            String query = "update docuserconvenzioni set sendmail='1', tipodoc = '" + datainvio + "' where username='" + username + "'";
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                es = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return es;
    }

    public String getInvioEmailROMA(String username) {
        String out = "0";
        try {
            String query = "select username,sendmail from docuserconvenzioni where username='" + username + "' and codicedoc='CONV'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString("sendmail");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

    // DOCUMENTAZIONE PROVENIENTE DA ROMA
    public boolean insertConvenzioneROMA(String username, String path) {
        try {
            String ins = "INSERT INTO convenzioniroma (codbando,username,codicedoc,path) VALUES (?,?,?,?)";
            try ( PreparedStatement ps = this.c.prepareStatement(ins)) {
                ps.setString(1, bando);
                ps.setString(2, username);
                ps.setString(3, "CONVROMA");
                ps.setString(4, path);
                ps.execute();
            }
            return true;
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return false;
    }

    public String getConvenzioneROMA(String username) {
        String pathRoma = "";
        try {
            String query = "select path from convenzioniroma where username = '" + username + "' order by timestamp desc limit 1";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pathRoma = rs.getString("path");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return pathRoma;
    }

    public String getDatafirmaConvenzioneROMA(String username) {
        String data = "";
        try {
            String query = "select timestamp from convenzioniroma where username = '" + username + "' order by timestamp desc limit 1";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = formatStringtoStringDate(rs.getString(1), timestampSQL, patternITA, false);
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return data;
    }

    public String getCellUtente(String username) {
        String cell = "";
        String query = "select cell from users where username=?";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cell = rs.getString("cell").trim();
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return cell;
    }

    public String getEmailUtente(String username) {
        String email = "";
        String query = "select mail from users where username=?";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        email = rs.getString("mail");
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return email;
    }

    public String[] getValoriPerEmail(String username) {
        String var[] = new String[7];
        String query = "select username,coddomanda,protocollo,mail,societa,decreto,datadecreto from bando_dd_mcn where username=?";
        try {
            try ( PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        var[0] = rs.getString("username");
                        var[1] = rs.getString("coddomanda");
                        var[2] = rs.getString("protocollo");
                        var[3] = rs.getString("mail");
                        var[4] = rs.getString("societa");
                        var[5] = rs.getString("decreto");
                        var[6] = rs.getString("datadecreto");
                    }
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public String getCF_user(String user) {
        String var = "";
        try {
            String query = "SELECT valore FROM usersvalori WHERE username = '" + user + "' AND campo = 'cfuser'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getString("valore");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public String getCF(String user) {
        String var = "";
        try {
            String query = "SELECT valore FROM usersvalori WHERE username = '" + user + "' AND campo = 'cf'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getString("valore");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public String getRagioneSociale(String user) {
        String var = "";
        try {
            String query = "select valore from usersvalori where username='" + user + "' and campo='societa'";
            try ( PreparedStatement ps = this.c.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getString("valore");
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return var;
    }

    public ArrayList<Ateco> query_ateco(String com) {
        ArrayList<Ateco> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM ateco_cr WHERE codice like ? OR descrizione LIKE ? ORDER BY descrizione";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, "%" + com + "%");
                ps1.setString(2, "%" + com + "%");
                try ( ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {
                        Ateco c1 = new Ateco(rs1.getString(1), rs1.getString(2));
                        if (rs1.getString(2).trim().equalsIgnoreCase(com.trim())) {
                            out.add(0, c1);
                        } else {
                            out.add(c1);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Comuni_rc> query_comuninazioni_rc(String com) {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni_rc WHERE nome like ? ORDER BY nome";
            PreparedStatement ps2;
            ResultSet rs2;
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, "%" + com + "%");
                try ( ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {
                        Comuni_rc c1 = new Comuni_rc(rs1.getInt(1), rs1.getString(2).trim(), rs1.getString(3).trim(), rs1.getString(4).trim(), rs1.getString(5).trim(), rs1.getString(6).trim(), rs1.getString(7).trim());
                        if (c1.getNome().equalsIgnoreCase(com.trim())) {
                            out.add(0, c1);
                        } else {
                            out.add(c1);
                        }
                    }
                    String sql2 = "SELECT * FROM nazioni_rc WHERE nome like ? ORDER BY nome";
                    ps2 = this.c.prepareStatement(sql2, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
                    ps2.setString(1, "%" + com + "%");
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        Comuni_rc c1 = new Comuni_rc(999999 + rs2.getInt(1), rs2.getString(3), rs2.getString(2), "00", "EE", "00", "EE");
                        if (c1.getNome().equalsIgnoreCase(com.trim())) {
                            out.add(0, c1);
                        } else {
                            out.add(c1);
                        }
                    }
                }
            }
            rs2.close();
            ps2.close();
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;

    }

    public ArrayList<Comuni_rc> query_comuni_rc(String com) {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni_rc WHERE nome like ? ORDER BY nome";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, "%" + com + "%");
                try ( ResultSet rs1 = ps1.executeQuery()) {
                    while (rs1.next()) {
                        Comuni_rc c1 = new Comuni_rc(rs1.getInt(1), rs1.getString(2).trim(), rs1.getString(3).trim(), rs1.getString(4).trim(), rs1.getString(5).trim(), rs1.getString(6).trim(), rs1.getString(7).trim());
                        if (c1.getNome().equalsIgnoreCase(com.trim())) {
                            out.add(0, c1);
                        } else {
                            out.add(c1);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Prov_rc> query_province_rc() {
        ArrayList<Prov_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT codiceprovincia,provincia FROM comuni_rc GROUP BY codiceprovincia";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Prov_rc(rs1.getString(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Prov_rc> query_regione_rc() {
        ArrayList<Prov_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT codiceregione,regione FROM comuni_rc GROUP BY regione";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Prov_rc(rs1.getString(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Comuni_rc> query_nazioni_rc() {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM nazioni_rc order by nome";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Comuni_rc(999999 + rs1.getInt(1), rs1.getString(3), rs1.getString(2), "00", "EE", "00", "EE"));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Comuni_rc> query_comuni_rc() {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Comuni_rc(rs1.getInt(1), rs1.getString(2),
                            rs1.getString(3),
                            rs1.getString(4), rs1.getString(5), rs1.getString(6), rs1.getString(7)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_fontifin_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM fontifin_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_aree_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM attivita_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_titolistudio_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM titolistudio_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_qualificazione_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM qualificazione_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_inquadramento_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM inquadramento_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_disponibilita_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM disponibilita_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public ArrayList<Items> query_attivita_docenti_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM attivita_docenti_rc";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
        return out;
    }

    public void deletesessionid(String sid) {
        try {
            String sql = "DELETE FROM sessionid WHERE sessionid = ?";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, sid);
                ps1.execute();
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }
    }

    public void newsessionid(String user, String sid, String time) {
        try {
            String sql = "SELECT sessionid FROM sessionid WHERE user = ? ORDER BY time DESC LIMIT 1";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, user);
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.next()) {
                    HttpSessionCollector.destroy(rs1.getString("sessionid"));
                }
                rs1.close();
                String insert = "INSERT INTO sessionid VALUES (?,?,?)";
                try ( PreparedStatement ps2 = this.c.prepareStatement(insert, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                    ps2.setString(1, sid);
                    ps2.setString(2, user);
                    ps2.setString(3, time);
                    ps2.execute();
                }
            }
        } catch (Exception ex) {
            if (!ex.getMessage().toLowerCase().contains("duplicate entry")) {
                insertTracking("ERROR SYSTEM", estraiEccezione(ex));
            }
        }
    }

    public Faq getFAQ(String idfaq) {
        Faq out = null;
        try {
            String sql = "SELECT * FROM faq WHERE idfaq = " + idfaq + "";
            try (
                     PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) {
                    out = new Faq(
                            rs1.getInt("idfaq"),
                            rs1.getInt("tipo"),
                            formatStringtoStringDate(rs1.getString("date_answer"), patternSql, patternITA, false),
                            formatStringtoStringDate(rs1.getString("date_ask"), patternSql, patternITA, false),
                            rs1.getString("domanda"),
                            getResultSetString(rs1, "domanda_mod"),
                            getResultSetString(rs1, "risposta"),
                            rs1.getString("timestamp"),
                            rs1.getString("usernamesoggetto"),
                            getResultSetString(rs1, "usernamerisposta")
                    );
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }

        return out;
    }

    public List<Faq> elencoFAQ(String stato) {
        List<Faq> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM faq WHERE tipo <> 3 ORDER BY tipo,date_ask";

            if (stato != null) {
                sql = "SELECT * FROM faq WHERE tipo = " + stato + " ORDER BY date_ask";
            }

            try (
                     PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Faq(
                            rs1.getInt("idfaq"),
                            rs1.getInt("tipo"),
                            formatStringtoStringDate(rs1.getString("date_answer"), patternSql, patternITA, false),
                            formatStringtoStringDate(rs1.getString("date_ask"), patternSql, patternITA, false),
                            rs1.getString("domanda"),
                            getResultSetString(rs1, "domanda_mod"),
                            getResultSetString(rs1, "risposta"),
                            rs1.getString("timestamp"),
                            rs1.getString("usernamesoggetto"),
                            getResultSetString(rs1, "usernamerisposta")
                    )
                    );
                }
            }
        } catch (Exception ex) {
            insertTracking("ERROR SYSTEM", estraiEccezione(ex));
        }

        return out;
    }

    private static String getResultSetString(ResultSet resultset, String colName) {
        try {
            String v = resultset.getString(colName);
            if (resultset.wasNull()) {
                return "";
            }
            if (v != null) {
                return v.trim();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public boolean delete_faq(String idfaq) {
        try {
            String update = "UPDATE faq SET tipo = ? WHERE idfaq = ?";
            try ( PreparedStatement ps1 = this.c.prepareStatement(update, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setInt(1, 3);
                ps1.setString(2, idfaq);
                ps1.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;
    }

    public boolean edit_faq(String idfaq, String domanda_mod, String risposta, String usernamerisposta) {
        try {
            String date_answer = curdate();
            String update = "UPDATE faq SET date_answer = ?, domanda_mod = ?, risposta = ?, tipo = ?, usernamerisposta = ? WHERE idfaq = ?";
            try ( PreparedStatement ps1 = this.c.prepareStatement(update, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                ps1.setString(1, date_answer);
                ps1.setString(2, domanda_mod);
                ps1.setString(3, risposta);
                ps1.setInt(4, 2);
                ps1.setString(5, usernamerisposta);
                ps1.setString(6, idfaq);
                ps1.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;

    }

    public boolean addFaq(String domanda, String risposta, String usernamesoggetto, String tipo) {
        try {
            String date_ask = curdate();
            if (tipo.equals("1")) {
                String insert = "INSERT INTO faq (date_answer,date_ask,domanda,domanda_mod,risposta,usernamesoggetto,tipo,usernamerisposta) "
                        + "VALUES (?,?,?,?,?,?,?,?)";
                try ( PreparedStatement ps1 = this.c.prepareStatement(insert, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                    ps1.setString(1, date_ask);
                    ps1.setString(2, date_ask);
                    ps1.setString(3, domanda);
                    ps1.setString(4, domanda);
                    ps1.setString(5, risposta);
                    ps1.setString(6, usernamesoggetto);
                    ps1.setString(7, "2");
                    ps1.setString(8, usernamesoggetto);
                    ps1.execute();
                    return true;
                }
            } else {
                String insert = "INSERT INTO faq (date_ask,domanda,domanda_mod,usernamesoggetto,tipo) "
                        + "VALUES (?,?,?,?,?)";
                try ( PreparedStatement ps1 = this.c.prepareStatement(insert, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE)) {
                    ps1.setString(1, date_ask);
                    ps1.setString(2, domanda);
                    ps1.setString(3, domanda);
                    ps1.setString(4, usernamesoggetto);
                    ps1.setString(5, "1");
                    ps1.execute();
                    return true;
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return false;

    }

    public String[] excelreport() {
        try {
            String sql = "SELECT aggiornamento,content FROM excelreport ORDER BY aggiornamento DESC LIMIT 1";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery();) {
                if (rs1.next()) {
                    String[] out = {formatStringtoStringDate(rs1.getString("aggiornamento"), timestampSQL, timestampITA, false), rs1.getString("content")};
                    return out;
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return null;
    }

    public List<String> elencoaccreditati() {
        List<String> out = new LinkedList<>();
        try {
            String sql = "SELECT DISTINCT(UPPER(valore)) FROM usersvalori WHERE(campo='cf' OR campo='piva') AND username IN (SELECT DISTINCT(username) FROM bando_dd_mcn WHERE stato_domanda <> 'R')";
            try ( PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);  ResultSet rs1 = ps1.executeQuery();) {
                while (rs1.next()) {
                    out.add(rs1.getString(1));
                }
            }
        } catch (Exception e) {
            insertTracking("ERROR SYSTEM", estraiEccezione(e));
        }
        return out;
    }

}
