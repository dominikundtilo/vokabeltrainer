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
    private boolean sync = true;

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
    private JComboBox<String> chosenLanguageAdd;
    private JTextField firstLanguageInput;
    private JTextField secondLanguageInput;
    private JButton addSubjectButton;
    private JCheckBox swapDirektionCheckBox;
    private JLabel seenWord;
    private JLabel knownWord;
    private JComboBox<String> chosenLanguagePractice;
    private JButton showButton;
    private JButton knownButton;
    private JButton unknownButton;
    private JComboBox<String> chosenLanguageEdit;
    private JTable wordTable;
    private JLabel errorLabel;

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

    public GUI(int user_id) {
        super("Vokabeltrainer");
        this.user_id = user_id;
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        addSubjectButton.addActionListener(e -> createSubject(firstLanguageInput.getText(), secondLanguageInput.getText()));

        updateLanguages();
        updateLanguage();

        chosenLanguageAdd.addActionListener(e -> {
            if (sync) {
                chosenLanguageEdit.setSelectedIndex(chosenLanguageAdd.getSelectedIndex());
                chosenLanguagePractice.setSelectedIndex(chosenLanguageAdd.getSelectedIndex());
                updateLanguage();
            }
        });
        chosenLanguageEdit.addActionListener(e -> {
            if (sync) {
                chosenLanguageAdd.setSelectedIndex(chosenLanguageEdit.getSelectedIndex());
                chosenLanguagePractice.setSelectedIndex(chosenLanguageEdit.getSelectedIndex());
                updateLanguage();
            }
        });
        chosenLanguagePractice.addActionListener(e -> {
            if (sync) {
                chosenLanguageAdd.setSelectedIndex(chosenLanguagePractice.getSelectedIndex());
                chosenLanguageEdit.setSelectedIndex(chosenLanguagePractice.getSelectedIndex());
                updateLanguage();
            }
        });
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
            errorLabel.setText("Fächerkombination existiert bereits");
        }
    }

    private void updateLanguage() {
        if (chosenLanguageAdd.getSelectedIndex() != -1) {
            try {
                SQL_SELECT_SUBJECT.setInt(1, subject_mapping.get(chosenLanguageAdd.getSelectedIndex()));
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

            sync = false;
            while (rs.next()){
                int id = rs.getInt(1);
                if (!subject_mapping.contains(id)) {
                    subject_mapping.add(id);
                    String cl1 = rs.getString(2);
                    String cl2 = rs.getString(3);

                    chosenLanguageAdd.addItem(cl1 + " - " + cl2);
                    chosenLanguagePractice.addItem(cl1 + " - " + cl2);
                    chosenLanguageEdit.addItem(cl1 + " - " + cl2);
                }
            }
            chosenLanguageAdd.setSelectedIndex(chosenLanguageAdd.getItemCount() - 1);
            chosenLanguageEdit.setSelectedIndex(chosenLanguageEdit.getItemCount() - 1);
            chosenLanguagePractice.setSelectedIndex(chosenLanguagePractice.getItemCount() - 1);
            sync = true;
            updateLanguage();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {

        wordTable = new JTable();
    }

}
