package de.tilosp.vokabeltrainer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.tilosp.vokabeltrainer.Main.connection;

/**
 * Created by Dominik on 22.06.2016.
 */
public class GUI extends JFrame {
    private int user_id;
    private ArrayList<Integer> subject_mapping = new ArrayList<>();

    private JTabbedPane tabbedPane1;
    private JPanel panel;
    private JPanel createPanel;
    private JPanel practicePanel;
    private JPanel editPanel;
    private JButton addWord;
    private JLabel primaryLanguage;
    private JLabel secondaryLanguage;
    private JTextField inputPrimaryWord;
    private JTextField inputSeondaryWord;
    private JTextField firstLanguageInput;
    private JTextField secondLanguageInput;
    private JButton addSubjectButton;
    private JCheckBox swapDirektionCheckBox;
    private JLabel seenWord;
    private JLabel knownWord;
    private JButton showButton;
    private JButton knownButton;
    private JButton unknownButton;
    private JTable wordTable;
    private JComboBox<String> subjectComboBox;
    private JButton logoutButton;
    private JLabel userNameLabel;

    private static PreparedStatement SQL_INSERT_SUBJECT;
    private static PreparedStatement SQL_SELECT_SUBJECTS;
    private static PreparedStatement SQL_SELECT_SUBJECT;

    static {
        try {
            SQL_INSERT_SUBJECT = connection.prepareStatement("INSERT INTO subject (user_id, language_1, language_2) VALUES (?, ?, ?)");
            SQL_SELECT_SUBJECTS = connection.prepareStatement("SELECT subject_id, language_1, language_2 FROM subject WHERE user_id = ?");
            SQL_SELECT_SUBJECT = connection.prepareStatement("SELECT language_1, language_2 FROM subject WHERE subject_id = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GUI(int user_id, String username) {
        super("Vokabeltrainer");
        this.user_id = user_id;
        userNameLabel.setText(username);
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        addSubjectButton.addActionListener(e -> createSubject(firstLanguageInput.getText(), secondLanguageInput.getText()));
        subjectComboBox.addActionListener(e -> updateLanguage());
        logoutButton.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });

        updateLanguages();
        updateLanguage();
    }

    private void createSubject(String language1, String language2){
        if (language1.length() < 1 || language2.length() < 1)
            return;

        boolean unique = true;

        try{
            SQL_SELECT_SUBJECTS.setInt(1, user_id);
            ResultSet rs = SQL_SELECT_SUBJECTS.executeQuery();

            while (unique && rs.next()){
                String l1 = rs.getString(2);
                String l2 = rs.getString(3);

                if((language1.equalsIgnoreCase(l1) && language2.equalsIgnoreCase(l2)) || (language1.equalsIgnoreCase(l2) && language2.equalsIgnoreCase(l1))) {
                    unique = false;
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        if(unique){
            try {
                SQL_INSERT_SUBJECT.setInt(1, user_id);
                SQL_INSERT_SUBJECT.setString(2, language1);
                SQL_INSERT_SUBJECT.setString(3, language2);
                SQL_INSERT_SUBJECT.executeUpdate();

                firstLanguageInput.setText("");
                secondLanguageInput.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            updateLanguages();
        }
        else {
            JOptionPane.showMessageDialog(this, "Fächerkombination existiert bereits", "Fächerkombination existiert bereits", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateLanguage() {
        if (subject_mapping.size() > 0) {
            try {
                SQL_SELECT_SUBJECT.setInt(1, subject_mapping.get(subjectComboBox.getSelectedIndex()));
                ResultSet rs = SQL_SELECT_SUBJECT.executeQuery();
                rs.next();
                primaryLanguage.setText(rs.getString(1));
                secondaryLanguage.setText(rs.getString(2));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLanguages(){
        try {
            SQL_SELECT_SUBJECTS.setInt(1, user_id);
            ResultSet rs = SQL_SELECT_SUBJECTS.executeQuery();

            int oldSelection = subjectComboBox.getSelectedIndex();
            subjectComboBox.removeAllItems();
            subject_mapping.clear();

            while (rs.next()){
                subject_mapping.add(rs.getInt(1));

                subjectComboBox.addItem(rs.getString(2) + " - " + rs.getString(3));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {

        wordTable = new JTable();
    }

}
