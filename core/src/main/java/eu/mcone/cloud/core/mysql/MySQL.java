/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class MySQL {

    private HikariDataSource ds;
    private String tablePrefix;
	
	public MySQL(String host, int port, String database, String username, String password, String tablePrefix) {
	    this.tablePrefix = tablePrefix;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);
        System.out.println("[MySQL.class] Verbunden zur Datenbank");
	}

	public void close() {
	    this.ds.close();
    }

    public void update(String qry) {
        try {
            Connection con = this.ds.getConnection();

            PreparedStatement preparedstatement = con.prepareStatement(qry);
            preparedstatement.executeUpdate();

            preparedstatement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateWithGetID(String qry){
        int i = -1;
        try{
            Connection con = this.ds.getConnection();

            PreparedStatement preparedStatement = con.prepareStatement(qry, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                i = rs.getInt(1);
            }

            rs.close();
            preparedStatement.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return i;
    }

    public void select(final String qry, final Callback<ResultSet> cb){
        Executors.newSingleThreadExecutor().execute(() -> {
	        ResultSet result = null;
            Connection con = null;
            PreparedStatement preparedStatement = null;

            try {
                con = this.ds.getConnection();
                preparedStatement = con.prepareStatement(qry);
                result = preparedStatement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cb.run(result);
                try {
                    if (result != null) result.close();
                    if (preparedStatement != null) preparedStatement.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Object select(String qry, CallbackResult<ResultSet> cb){
	    Object o = null;
        try {
            final Connection con = this.ds.getConnection();

            PreparedStatement preparedStatement = con.prepareStatement(qry);
            ResultSet result = preparedStatement.executeQuery();

            o = cb.run(result);

            result.close();
            preparedStatement.close();
            con.close();

            return o;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return o;
    }

    public interface Callback<rs> {
        void run(rs rs);
    }

    public interface CallbackResult<rs> {
        Object run(rs rs);
    }

    public void createMasterTables() {
        update("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "_templates` " +
                "(" +
                    "`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "`name` varchar(100) NOT NULL UNIQUE KEY, " +
                    "`max_players` int(5) NOT NULL, " +
                    "`ram` int(8) NOT NULL, " +
                    "`min` int(5) NOT NULL, " +
                    "`max` int(5) NOT NULL, " +
                    "`version` varchar(10) NOT NULL, " +
                    "`emptyservers` int(3) NOT NULL, " +
                    "`startup` boolean NOT NULL" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        update("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "_static_servers` " +
                "(" +
                    "`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "`name` varchar(100) NOT NULL, `max` int(5) NOT NULL, " +
                    "`ram` int(8) NOT NULL, " +
                    "`version` varchar(10) NOT NULL, " +
                    "`wrapper` varchar(100)" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8;");
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

}

