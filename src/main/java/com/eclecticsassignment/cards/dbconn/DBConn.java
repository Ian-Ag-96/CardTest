package com.eclecticsassignment.cards.dbconn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DBConn {

    private static final String URL = "jdbc:postgresql://localhost:5432/card_test";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String testConn() {
    	String res = "Failure";
        try (Connection conn = DBConn.getConnection()) {
            if (conn != null) {
            	log.info("Conn URL: {}", conn.getMetaData().getURL());
            	log.info("Conn User: {}", conn.getMetaData().getUserName());
                System.out.println("Connected to the database successfully!");
                res = "Success";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
