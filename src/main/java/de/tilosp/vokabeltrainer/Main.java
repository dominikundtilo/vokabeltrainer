package de.tilosp.vokabeltrainer;

import de.tilosp.vokabeltrainer.gui.LoginGUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static Connection connection;

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "user " +
                    "(user_id integer NOT NULL IDENTITY, " +
                    "user_name varchar(40) NOT NULL, " +
                    "password varchar(40) NOT NULL, " +
                    "PRIMARY KEY (user_id))";

    private static final String SQL_CREATE_SUBJECT_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "subject " +
                    "(subject_id integer NOT NULL IDENTITY, " +
                    "user_id integer NOT NULL, " +
                    "language_1 varchar(40) NOT NULL, " +
                    "language_2 varchar(40) NOT NULL, " +
                    "PRIMARY KEY (subject_id))";

    private static final String SQL_CREATE_VOCABLE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    "vocable " +
                    "(vocable_id integer NOT NULL IDENTITY, " +
                    "subject_id integer NOT NULL, " +
                    "word_1 varchar(160) NOT NULL, " +
                    "word_2 varchar(160) NOT NULL, " +
                    "level integer NOT NULL, " +
                    "PRIMARY KEY (vocable_id))";

    public static void main(String[] args) {


        try {
            connection = DriverManager.getConnection("jdbc:hsqldb:file:vokabeltrainer/database", "SA", "");
            createTables();
            new LoginGUI().setVisible(true);
        } catch (SQLException e) {
            System.exit(2);
        }
    }

    private static void createTables() throws SQLException {

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(SQL_CREATE_USER_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) { stmt.close(); }
        }
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(SQL_CREATE_SUBJECT_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) { stmt.close(); }
        }
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(SQL_CREATE_VOCABLE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) { stmt.close(); }
        }
    }
}
