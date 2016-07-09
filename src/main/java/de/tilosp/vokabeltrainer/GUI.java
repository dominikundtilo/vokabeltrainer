package de.tilosp.vokabeltrainer;

import com.sun.xml.internal.fastinfoset.util.StringArray;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.xml.transform.Result;
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
    private ArrayList<String[]> word = new ArrayList<>();

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
    private static PreparedStatement SQL_SELECT_SUBJECT;
    private static PreparedStatement SQL_INSERT_VOCABLE;
    private static PreparedStatement SQL_SELECT_VOCABLE;
    private static PreparedStatement SQL_SELECT_VOCABLE_ROW;

    static {
        try {
            SQL_INSERT_SUBJECT = connection.prepareStatement("INSERT INTO subject (user_id, language_1, language_2) VALUES (?, ?, ?)");
            SQL_SELECT_SUBJECT = connection.prepareStatement("SELECT language_1, language_2 FROM subject WHERE user_id = ?");
            SQL_INSERT_VOCABLE = connection.prepareStatement("INSERT INTO vocable (subject_id, word_1, word_2, level) SELECT s.subject_id, ?, ?, 0 FROM subject s WHERE s.language_1 = ? AND s.language_2 = ?");
            SQL_SELECT_VOCABLE = connection.prepareStatement("SELECT v.word_1, v.word_2 FROM vocable v, subject s WHERE s.subject_id = v.subject_id AND s.language_1 = ? AND s.language_2 = ? AND s.user_id = ?");
            SQL_SELECT_VOCABLE_ROW = connection.prepareStatement("SELECT v.vocable_id, v.word_1, v.word_2, v.level FROM vocable v, subject s WHERE s.subject_id = v.subject_id AND s.language_1 = ? AND s.language_2 = ? AND s.user_id = ?");
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

        addSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createSubject(firstLanguageInput.getText(), secondLanguageInput.getText());
            }
        });

        updateChosenLanguage();
        updatePrimarySecondaryLanguage();
        updateWords();
        updateTable();

        if (chosenLanguageAdd.getSelectedIndex() == -1) {
            knownButton.setEnabled(false);
            unknownButton.setEnabled(false);
            showButton.setEnabled(false);
        }



            addWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWord();
                updateWords();
                updateTable();
            }
        });
        chosenLanguageAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePrimarySecondaryLanguage();
            }
        });
        knownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomWord();
                //increase level
            }
        });
        unknownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomWord();
                //reduce level
            }
        });
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                knownWord.setVisible(true);

                knownButton.setEnabled(true);
                unknownButton.setEnabled(true);
                showButton.setEnabled(false);
            }
        });
        chosenLanguageEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable();
            }
        });
        chosenLanguagePractice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWords();
            }
        });
    }

    private void createSubject(String language1, String language2){
        boolean unique = true;

        try{
            SQL_SELECT_SUBJECT.setInt(1, user_id);
            ResultSet rs = SQL_SELECT_SUBJECT.executeQuery();

            while (unique && rs.next()){
                String l1 = rs.getString(1);
                String l2 = rs.getString(2);

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
            updateChosenLanguage();
        }
        else {
            errorLabel.setText("Fächerkombination existiert bereits");
        }
    }

    private void updateChosenLanguage(){
        try {
            SQL_SELECT_SUBJECT.setInt(1, user_id);
            ResultSet rs = SQL_SELECT_SUBJECT.executeQuery();

            chosenLanguageAdd.removeAllItems();
            chosenLanguagePractice.removeAllItems();
            chosenLanguageEdit.removeAllItems();

            while (rs.next()){
                String cl1 = rs.getString(1);
                String cl2 = rs.getString(2);

                chosenLanguageAdd.addItem(cl1 + " – " + cl2);
                chosenLanguagePractice.addItem(cl1 + " – " + cl2);
                chosenLanguageEdit.addItem(cl1 + " – " + cl2);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addWord(){
        String subjects = (String) chosenLanguageAdd.getSelectedItem();
        String[] subject = subjects.split(" – ");

        try {
            SQL_INSERT_VOCABLE.setString(1, inputPrimaryWord.getText());
            SQL_INSERT_VOCABLE.setString(2, inputSeondaryWord.getText());
            SQL_INSERT_VOCABLE.setString(3, subject[0]);
            SQL_INSERT_VOCABLE.setString(4, subject[1]);
            SQL_INSERT_VOCABLE.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        inputPrimaryWord.setText("");
        inputSeondaryWord.setText("");
    }

    private void updatePrimarySecondaryLanguage(){
        if (chosenLanguageAdd.getSelectedIndex() != -1) {
            String subjects = (String) chosenLanguageAdd.getSelectedItem();
            String[] subject = subjects.split(" – ");

            primaryLanguage.setText(subject[0]);
            secondaryLanguage.setText(subject[1]);
        }
    }

    private void updateWords(){
        if (chosenLanguagePractice.getSelectedIndex() != -1) {
            String subjects = (String) chosenLanguagePractice.getSelectedItem();
            String[] subject = subjects.split(" – ");
            int x = 0;

            try {
                SQL_SELECT_VOCABLE.setString(1, subject[0]);
                SQL_SELECT_VOCABLE.setString(2, subject[1]);
                SQL_SELECT_VOCABLE.setInt(3, user_id);
                ResultSet rs = SQL_SELECT_VOCABLE.executeQuery();

                while(word.size() > 0){
                    word.remove(0);
                }

                while (rs.next()){
                    word.add(new String[] {rs.getString(1),rs.getString(2)});
                    x++;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            randomWord();
        }

    }

    private void randomWord(){
        int random = (int) Math.round(Math.random() * (word.size() - 1));

        knownWord.setVisible(false);
        String[] temp = word.get(random);

        if(swapDirektionCheckBox.isSelected()){
            seenWord.setText(temp[0]);
            knownWord.setText(temp[1]);
        }
        else {
            seenWord.setText(temp[1]);
            knownWord.setText(temp[0]);
        }

        knownButton.setEnabled(false);
        unknownButton.setEnabled(false);
        showButton.setEnabled(true);
    }

    private void updateTable(){
        String subjects = (String) chosenLanguageEdit.getSelectedItem();
        String[] subject = subjects.split(" – ");

        ArrayList<String[]> vocableRow = new ArrayList<>();


        try {
            SQL_SELECT_VOCABLE_ROW.setString(1, subject[0]);
            SQL_SELECT_VOCABLE_ROW.setString(2, subject[1]);
            SQL_SELECT_VOCABLE_ROW.setInt(3, user_id);
            ResultSet rs = SQL_SELECT_VOCABLE_ROW.executeQuery();

            String[] headLine = {"number", subject[0], subject[1], "word level"};


            while (rs.next()){
                vocableRow.add(new String[] {Integer.toString(rs.getInt(1)), rs.getString(2), rs.getString(3), Integer.toString(rs.getInt(4))});
            }

            wordTable.setModel(new AbstractTableModel() {
                public String getColumnName(int column) { return headLine[column]; }
                public int getRowCount() { return vocableRow.size(); }
                public int getColumnCount() { return headLine.length; }
                public Object getValueAt(int row, int col) { return vocableRow.get(row)[col]; }
                public boolean isCellEditable(int row, int column) { return false; }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createUIComponents() {
        //add interface


        //edit interface

        //Object[][] data = new Object[15][5];
        //for(int x = 0; x < 15; x++){
        //    data[x] = new Object[]{x,"tell", "sagen", x+1, 1};
        //}
        wordTable = new JTable();
    }

}
