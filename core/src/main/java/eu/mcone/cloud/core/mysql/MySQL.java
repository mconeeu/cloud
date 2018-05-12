/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.mysql;

import com.zaxxer.hikari.HikariDataSource;
import eu.mcone.cloud.core.console.ConsoleColor;
import eu.mcone.cloud.core.console.Logger;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

    private HikariDataSource ds;
    @Getter
    private String tablePrefix;
	
	public MySQL(String host, int port, String database, String username, String password, String tablePrefix) {
        ds = new HikariDataSource();
        ds.setMaximumPoolSize(2);
        ds.setDriverClassName("org.mariadb.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mariadb://"+host+":"+port+"/"+database+"?autoReconnect=true");
        ds.addDataSourceProperty("user", username);
        ds.addDataSourceProperty("password", password);

        //config.addDataSourceProperty("cachePrepStmts", "true");
        //config.addDataSourceProperty("prepStmtCacheSize", "250");
        //config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.tablePrefix = tablePrefix;
        Logger.log(getClass(), ConsoleColor.GREEN+"Verbunden zu Datenbank "+ds.getJdbcUrl());
	}

    public void close() {
        this.ds.close();
    }

    public void update(String qry, Object... parameters) {
        try {
            Connection con = this.ds.getConnection();

            PreparedStatement preparedstatement = con.prepareStatement(qry);
            for (int i = 0; i < parameters.length; i++) {
                preparedstatement.setObject(i+1, parameters[i]);
            }
            preparedstatement.executeUpdate();

            preparedstatement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateWithGetId(String qry, Object... parameters){
        int id = -1;
        try{
            Connection con = this.ds.getConnection();

            PreparedStatement preparedStatement = con.prepareStatement(qry, PreparedStatement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i+1, parameters[i]);
            }
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            preparedStatement.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return id;
    }

    public void select(String qry, Callback<ResultSet> cb){
        try {
            final Connection con = this.ds.getConnection();

            PreparedStatement preparedStatement = con.prepareStatement(qry);
            ResultSet result = preparedStatement.executeQuery();

            cb.run(result);

            result.close();
            preparedStatement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectAsync(final String qry, final Callback<ResultSet> cb){
        new Thread(() -> {
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
        }).start();
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

    public Connection getConnection() {
        Connection result = null;
        try {
            result = ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}

