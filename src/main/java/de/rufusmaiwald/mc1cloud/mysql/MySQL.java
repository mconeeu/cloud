/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mc1cloud.mysql;

import java.sql.*;

public class MySQL {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private String tablePrefix;
    
    private static Connection con;

    public MySQL(String host, int port, String database, String username, String password, String tablePrefix) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    public void connect() {
        if (!isConnected()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database, username, password);
                System.out.println("[MySQL.connect] MySQL connection to " + database + " established!");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[MySQL.connect] connection to " + database + " already established!");
        }
    }

    public void close() {
        if (isConnected()) {
            try {
                con.close();
                System.out.println("[MySQL.close] MySQL to " + database + " connection closed!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[MySQL.close] no MySQL connection to " + database + " available!");
        }
    }

    public void execute(String qry) {
        if (isConnected()) {
            try {
                PreparedStatement preparedstatement = con.prepareStatement(qry);
                preparedstatement.executeUpdate();
                System.out.println("[MySQL.execute] \""+qry+"\" executed @" + database + "!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[MySQL.execute] no MySQL connection to " + database + " available!");
        }
    }

    public ResultSet select(String qry) {
        if (isConnected()) {
            try {
                PreparedStatement preparedStatement = con.prepareStatement(qry);
                System.out.println("[MySQL.select] \""+qry+"\" executed @ " + database + "!");
                return preparedStatement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[MySQL.select] no MySQL connection to " + database + " available!");
        }
        return null;
    }

    private static boolean isConnected() {
        return con != null;
    }

    public void createCloudTables() {
        //templates table
        this.execute("CREATE TABLE IF NOT EXISTS `" + this.tablePrefix + "_templates` (`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, `name` varchar(100) NOT NULL UNIQUE KEY, `ram` int(8) NOT NULL, `min` int(5), `max` int(5), `emptyservers` int(3) NOT NULL, `startup` boolean NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        this.execute("CREATE TABLE IF NOT EXISTS `" + this.tablePrefix + "_static_servers` (`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, `name` varchar(100) NOT NULL, `ram` int(8) NOT NULL, `wrapper` varchar(100) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
    }

    public String getTablePrefix() {
        return tablePrefix;
    }
}
