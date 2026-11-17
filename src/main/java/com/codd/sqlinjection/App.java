package com.codd.sqlinjection;

import java.sql.*;
import java.util.Scanner;

public class App {
    
    private static final String URL = "jdbc:h2:mem:database;DB_CLOSE_DELAY=-1";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    
    public static void main(String[] args) {
        try (
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Scanner scanner = new Scanner(System.in);
        ) {
            initDatabase(connection);
            
            while (true) {
                System.out.println("  [1] Recherche normale");
                System.out.println("  [2] Fuite de données");
                System.out.println("  [0] Quitter");

                String choice = scanner.nextLine();
                String payload;

                switch (choice) {
                    case "1": payload = "admin"; break;
                    case "2": payload = "' OR '1'='1"; break;
                    case "0": return;
                    default:
                        System.out.println("Choix invalide.");
                        continue;
                }

                System.out.println("\n>>> Entrée testée : [" + payload + "]");

                System.out.println("\n--- 1. VERSION SÉCURISÉE (PreparedStatement) ---");
                selectWithPreparedStatement(connection, payload);

                System.out.println("\n--- 2. VERSION VULNÉRABLE (Concaténation) ---");
                selectWithConcatenation(connection, payload);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void initDatabase(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (id INT PRIMARY KEY, username VARCHAR(50), password VARCHAR(50), role VARCHAR(20))");
            stmt.execute("INSERT INTO users VALUES (1, 'admin', 'SuperSecret123', 'ADMIN')");
            stmt.execute("INSERT INTO users VALUES (2, 'alice', 'alice2026', 'USER')");
        }
    }
    
    private static void selectWithConcatenation(Connection connection, String username) {
        String query = "select * from users where username = '" + username + "';";

        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            
            printResults(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void selectWithPreparedStatement(Connection connection, String username) {
        String query = "select * from users where username = ?;";
        
        try (
            PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            
            printResults(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void printResults(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println(rs.getString(1) 
                + (rs.getString(2) != null ? " | " + rs.getString(2) : "")
                + (rs.getString(3) != null ? " | " + rs.getString(3) : ""));
        }
    }
}
