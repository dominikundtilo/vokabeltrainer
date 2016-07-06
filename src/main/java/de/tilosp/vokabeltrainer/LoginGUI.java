package de.tilosp.vokabeltrainer;

import de.tilosp.vokabeltrainer.localisation.Localisation;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.tilosp.vokabeltrainer.Main.connection;

/**
 * Created by Dominik on 22.06.2016.
 */
public class LoginGUI extends JFrame {


    private static PreparedStatement SQL_SELECT_USERS;
    private static PreparedStatement SQL_INSERT_USER;
    private static PreparedStatement SQL_SELECT_USER;
    private static MessageDigest MD5;

    static {
        try {
            SQL_SELECT_USERS = connection.prepareStatement("SELECT user_name FROM user");
            SQL_INSERT_USER = connection.prepareStatement("INSERT INTO user (user_name, password) VALUES (?, ?)");
            SQL_SELECT_USER = connection.prepareStatement("SELECT user_id FROM user WHERE user_name = ? AND password = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.exit(2);
        }
    }

    private final ArrayList<String> users= new ArrayList<>();

    private JPanel panel1;
    private JComboBox<String> loginComboBox;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JTextField createNameField;
    private JPasswordField createMainPasswordField;
    private JPasswordField createCheckingPasswordField;
    private JButton registerButton;
    private JLabel loginErrorLabel;
    private JLabel registerErrorLabel;

    public LoginGUI() {
        super(Localisation.getString("login_gui"));
        setContentPane(panel1);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        updateUsers();

        ActionListener loginListener = e -> {
            try {
                SQL_SELECT_USER.setString(1, (String) loginComboBox.getSelectedItem());
                SQL_SELECT_USER.setString(2, md5(String.valueOf(loginPasswordField.getPassword())));
                ResultSet rs = SQL_SELECT_USER.executeQuery();
                if (rs.next()) {
                    new GUI(rs.getInt(1)).setVisible(true);
                    dispose();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        };
        loginButton.addActionListener(loginListener);
        loginPasswordField.addActionListener(loginListener);

        ActionListener registerListener = e -> {
            registerErrorLabel.setText("");

            String username = createNameField.getText();
            String password = String.valueOf(createMainPasswordField.getPassword());
            String passwordCheck = String.valueOf(createCheckingPasswordField.getPassword());

            if (password.equals(passwordCheck)){
                if (password.length() >= 4) {
                    if (!users.contains(username)) {
                        try {
                            SQL_INSERT_USER.setString(1, username);
                            SQL_INSERT_USER.setString(2, md5(password));
                            SQL_INSERT_USER.executeUpdate();

                            SQL_SELECT_USER.setString(1, username);
                            SQL_SELECT_USER.setString(2, md5(password));
                            ResultSet rs = SQL_SELECT_USER.executeQuery();
                            rs.next();
                            new GUI(rs.getInt(1)).setVisible(true);
                            dispose();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        registerErrorLabel.setText("Username is already in use");
                    }
                } else {
                    registerErrorLabel.setText("Password too short");
                }

            }
            else {
                registerErrorLabel.setText("Passwörter stimmen nicht über ein");
            }

        };
        createCheckingPasswordField.addActionListener(registerListener);
        registerButton.addActionListener(registerListener);

        pack();
        setResizable(false);
    }

    private void updateUsers() {
        try {
            ResultSet rs = SQL_SELECT_USERS.executeQuery();
            while (rs.next()) {
                String s = rs.getString(1);
                if (!users.contains(s)) {
                    users.add(s);
                    loginComboBox.addItem(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String md5(String input) {
        try {
            byte[] tmp = input.getBytes("UTF-8");
            MD5.reset();
            return DatatypeConverter.printHexBinary(MD5.digest(tmp));
        } catch (UnsupportedEncodingException e) {
            System.exit(2);
            return null;
        }
    }
}
