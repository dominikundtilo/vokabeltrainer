package de.tilosp.vokabeltrainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static Connection connection;

    public static void main(String[] args) {


        try {
            connection = DriverManager.getConnection("jdbc:hsqldb:file:database", "SA", "");
            new LoginGUI().setVisible(true);
        } catch (SQLException e) {
            System.exit(2);
        }
    }
}
