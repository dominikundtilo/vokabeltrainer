package de.tilosp.vokabeltrainer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Dominik on 22.06.2016.
 */
public class LoginGUI extends JFrame {
    private JPanel panel1;
    private JComboBox logincomboBox;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JTextField createNameField;
    private JPasswordField createMainPasswordField;
    private JPasswordField createCheckingPasswordField;
    private JButton registerButton;
    private JLabel loginErrorLabel;
    private JLabel registerErrorLabel;

    public LoginGUI() {
        super("LoginGUI");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerErrorLabel.setText("");

                boolean passwordAccording = true;
                String username = createNameField.getText();
                String password = String.valueOf(createMainPasswordField.getPassword());
                String passwordCheck = String.valueOf((createCheckingPasswordField.getPassword()));

                if(password.length() == passwordCheck.length()){
                    if(!password.equals(passwordCheck)){
                        passwordAccording = false;
                    }
                }
                else{
                    passwordAccording = false;
                }

                if(passwordAccording){
                    //überprüfen ob Name bereits existiert
                    //Name und Passwort in Datenbank einfürgen
                }
                else {
                    registerErrorLabel.setText("Passwörter stimmen nicht über ein");
                }

            }
        });
        pack();
    }

    public static void main() {
        JFrame frame = new JFrame("LoginGUI");
        frame.setContentPane(new LoginGUI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
