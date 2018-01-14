/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private MySQL mysql;
    private String config;
    private String sqlTable;

    private Map<String, String> keys = new HashMap<>();

    public Config(MySQL mysql, String config) {
        this.mysql = mysql;
        this.config = config;
        this.sqlTable = mysql.getTablePrefix() + "_" + config;
    }

    public void createTable() {
        //Create Config table
        this.mysql.update("CREATE TABLE IF NOT EXISTS `" + this.sqlTable + "` (`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, `key` VARCHAR(100) UNIQUE KEY, `value` VARCHAR(1000)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
    }

    //Insert Config key and value
    public void insert(String key, String value){
        this.mysql.update("INSERT IGNORE INTO " + this.sqlTable + " (`key`, `value`) VALUES ('"+key+"', '"+value+"');");
    }

    public void insert(String key, int value){
        this.mysql.update("INSERT IGNORE INTO " + this.sqlTable + " (`key`, `value`) VALUES ('"+key+"', '"+value+"');");
    }

    public void insert(String key, boolean value){
        this.mysql.update("INSERT IGNORE INTO " + this.sqlTable + " (`key`, `value`) VALUES ('"+key+"', '"+value+"');");
    }

    //Store Config values locally
    public void store() {
        ResultSet rs = this.mysql.select("SELECT * FROM " + this.sqlTable + ";");
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

        switch (result) {
            case "0":
                return false;
            case "1":
                return true;
            default:
                return null;
        }
    }

    //Get live Config value from database
    public String getLiveConfigValue(String key) {
        ResultSet rs = this.mysql.select("SELECT value FROM `" + this.sqlTable + "` WHERE `key` = '" + key + "';");
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
        ResultSet rs = this.mysql.select("SELECT * FROM `" + this.sqlTable + "` WHERE `key` = '" + key + "';");
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
        ResultSet rs = this.mysql.select("SELECT * FROM `" + this.sqlTable +"` WHERE `key` = '" + key + "';");
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

    public String getConfig() {
        return this.config;
    }

}
