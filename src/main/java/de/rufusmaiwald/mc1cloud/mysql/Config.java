/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mc1cloud.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Config {

    private MySQL mysql;
    private String config;
    private boolean tableExists = false;

    private Map<String, String> keys = new HashMap<>();

    public Config(MySQL mysql, String config) {
        this.mysql = mysql;
        this.config = config;
    }

    public void createTable() {
        //Create Config table
        this.mysql.execute("CREATE TABLE IF NOT EXISTS `" + this.mysql.getTablePrefix() + "_" + this.config + "` (`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, `key` VARCHAR(100) UNIQUE KEY, `value` VARCHAR(1000)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        ResultSet rs = this.mysql.select("SELECT value FROM " + this.mysql.getTablePrefix() + "_" + this.config);
        try {
            if (rs.next()) {
                tableExists = true;
                System.out.println(this.config + " Config Table existiert bereits. Values werden heruntergeladen...");
            } else {
                System.out.println(this.config + " Config Table existiert noch nicht. Values werden hochgeladen...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Insert Config key and value
    public void insert(String key, String value){
        this.mysql.execute("INSERT IGNORE INTO " + this.mysql.getTablePrefix() + "_" + this.config + " (`key`, `value`) VALUES ('"+key+"', '"+value+"');");
    }

    //Store Config values locally
    public void store() {
        ResultSet rs = this.mysql.select("SELECT * FROM " + this.mysql.getTablePrefix() + "_" + this.config + ";");
        try {
            while (rs.next()) {
                this.keys.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Get stored Config value
    public String getConfigValue(String key) {
        return keys.get(key).replace("&", "§");
    }

    //Get stored Config value as int
    public int getIntConfigValue(String key) {
        return Integer.parseInt(keys.get(key));
    }

    //Get stored Config value as boolean
    public Boolean getBooleanConfigValue(String key) {
        String result = keys.get(key);

        if (Objects.equals(result, "0")) {
            return false;
        } else if (Objects.equals(result, "1")) {
            return true;
        } else {
            return null;
        }
    }

    //Get live Config value from database
    public String getLiveConfigValue(String key) {
        ResultSet rs = this.mysql.select("SELECT value FROM `" + this.mysql.getTablePrefix() + "_" + this.config + "` WHERE `key` = '" + key + "';");
        try{
            if (rs.next()) {
                String message;
                message = rs.getString("value").replace("&", "§");
                return message;
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Get live Config value from database as int
    public int getLiveIntConfigValue(String key) {
        ResultSet rs = this.mysql.select("SELECT * FROM `" + this.mysql.getTablePrefix() + "_" + this.config + "` WHERE `key` = '" + key + "';");
        try{
            if (rs.next()) {
                int zahl = 0;
                zahl = rs.getInt("value");
                return zahl;
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    //Get live Config value from database as boolean
    public Boolean getLiveBooleanConfigValue(String key) {
        ResultSet rs = this.mysql.select("SELECT * FROM `" + this.mysql.getTablePrefix() + "_" + this.config +"` WHERE `key` = '" + key + "';");
        try{
            if (rs.next()) {
                Boolean message;
                message = rs.getBoolean("value");
                return message;
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
