package com.eclecticsassignment.cards.dbconn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DBConn {

    private static final String URL = "jdbc:postgresql://localhost:5432/card_test";
    private static final String DOCKER_URL = "jdbc:postgresql://host.docker.internal:5431/card_test";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";
    
    private DBConn() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static Connection getConnectionDocker() throws SQLException {
        return DriverManager.getConnection(DOCKER_URL, USER, PASSWORD);
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
        	log.info("Failed to connect using local URL. Using docker URL...");
        	try (Connection conn2 = DBConn.getConnectionDocker()) {
                if (conn2 != null) {
                	log.info("Conn URL: {}", conn2.getMetaData().getURL());
                	log.info("Conn User: {}", conn2.getMetaData().getUserName());
                    log.info("Connected to the database successfully!");
                    res = "Success";
                }
            } catch (SQLException e2) {
            	e2.printStackTrace();
            }
        }
        return res;
    }
}
