/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.otp;

import static it.refill.action.ActionB.trackingAction;
import static it.refill.util.Utility.estraiEccezione;
import static java.lang.Class.forName;
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author rcosco
 */
public class Db_OTP {

    private Connection conn = null;
    private static final ResourceBundle conf = ResourceBundle.getBundle("conf.conf");

    public Db_OTP() {

        String driver = "com.mysql.cj.jdbc.Driver";
        String user = conf.getString("db.user");
        String password = conf.getString("db.pass");
        String host = conf.getString("db.host") + ":3306/enm_otp";

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
            this.conn = getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(ex));
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (Exception ex1) {
                }
            }
            this.conn = null;
        }
    }

    public Db_OTP(Connection conn) {
        try {
            this.conn = conn;
        } catch (Exception ex) {
        }
    }

    public Connection getConnectionDB() {
        return conn;
    }

    public void closeDB() {
        try {
            if (conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(ex));
        }
    }

    public boolean cambiastato(String codProgetto, String user, int idsms, String statodest) {
        try {
            String update = "UPDATE ctrlotp SET stato = '" + statodest + "'"
                    + " WHERE stato='A' "
                    + "AND codprogetto = '" + codProgetto + "' "
                    + "AND user = '" + user + "' "
                    + "AND idsms = " + idsms;
            try ( PreparedStatement ps = this.conn.prepareStatement(update)) {
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(e));
        }
        return false;
    }

    public boolean insOtp(String codProgetto, String user, String codOtp, String numcell, int idsms) {
        try {
            cambiastato(codProgetto, user, idsms, "KO");
            String upd = "insert into ctrlotp (codprogetto,user,codotp,numcell,idsms) values (?,?,?,?,?)";
            try ( PreparedStatement ps = this.conn.prepareStatement(upd)) {
                ps.setString(1, codProgetto);
                ps.setString(2, user);
                ps.setString(3, codOtp);
                ps.setString(4, numcell);
                ps.setInt(5, idsms);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(e));
        }
        return false;
    }

    public String getSMS(String codprogetto, int codMsg) {
        try {
            String sql = "Select msg from sms where codprogetto = ? and idsms = ?";
            try ( PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, codprogetto);
                ps.setInt(2, codMsg);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("msg");
                    }
                }
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(e));
        }
        return null;
    }

    public boolean isOK(String codprogetto, String user, String otp, int idsms) {
        try {
            String sql = "Select codprogetto from ctrlotp where codprogetto = ? and user = ? and codotp = ? and stato = ? AND idsms = ?";
            try ( PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, codprogetto);
                ps.setString(2, user);
                ps.setString(3, otp);
                ps.setString(4, "A");
                ps.setInt(5, idsms);
                try ( ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(e));
        }
        return false;
    }

    public String getPath(String id) {
        try {
            String sql = "select url from path where id = ?";
            try ( PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("url");
                    }
                }
            }
        } catch (Exception e) {
            trackingAction("ERROR SYSTEM DBOTP", estraiEccezione(e));
        }
        return null;
    }
}
