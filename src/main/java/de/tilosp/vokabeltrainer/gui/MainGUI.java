package de.tilosp.vokabeltrainer.gui;

import de.tilosp.vokabeltrainer.localisation.Localisation;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.tilosp.vokabeltrainer.Main.connection;

/**
 * Created by Dominik on 22.06.2016.
 */
public class MainGUI extends JFrame {
    private int user_id;
    private int selected_word_id = -1;
    private String word2;
    private ArrayList<Integer> subject_mapping = new ArrayList<>();
    private String[] columnNames = { "ID", "", "", "Level" };
    private ArrayList<String[]> rowData = new ArrayList<>();

    private JPanel panel;
    private JButton addVocable;
    private JLabel primaryLanguage;
    private JLabel secondaryLanguage;
    private JTextField firstLanguageInput;
    private JTextField secondLanguageInput;
    private JButton addSubjectButton;
    private JCheckBox swapDirektionCheckBox;
    private JButton showButton;
    private JButton knownButton;
    private JButton unknownButton;
    private JTable wordTable;
    private JComboBox<String> subjectComboBox;
    private JButton logoutButton;
    private JLabel userNameLabel;
    private JTextArea vocable1TextArea;
    private JTextArea vocable2TextArea;
    private JTextArea word1Practise;
    private JTextArea word2Practise;
    private JLabel primaryLanguage1;
    private JLabel secondaryLanguage1;

    private static PreparedStatement SQL_INSERT_SUBJECT;
    private static PreparedStatement SQL_SELECT_SUBJECTS;
    private static PreparedStatement SQL_SELECT_SUBJECT;
    private static PreparedStatement SQL_INSERT_VOCABLE;
    private static PreparedStatement SQL_SELECT_VOCABLES;
    private static PreparedStatement SQL_SELECT_VOCABLE;
    private static PreparedStatement SQL_UPDATE_VOCABLE;

    static {
        try {
            SQL_INSERT_SUBJECT = connection.prepareStatement("INSERT INTO subject (user_id, language_1, language_2) VALUES (?, ?, ?)");
            SQL_SELECT_SUBJECTS = connection.prepareStatement("SELECT subject_id, language_1, language_2 FROM subject WHERE user_id = ?");
            SQL_SELECT_SUBJECT = connection.prepareStatement("SELECT language_1, language_2 FROM subject WHERE subject_id = ?");
            SQL_INSERT_VOCABLE = connection.prepareStatement("INSERT INTO vocable (subject_id, word_1, word_2, level) VALUES (?, ?, ?, 0)");
            SQL_SELECT_VOCABLES = connection.prepareStatement("SELECT vocable_id, word_1, word_2, level FROM vocable WHERE subject_id = ?");
            SQL_SELECT_VOCABLE = connection.prepareStatement("SELECT vocable_id, word_1, word_2 FROM vocable WHERE subject_id = ? ORDER BY level, RAND() LIMIT 1");
            SQL_UPDATE_VOCABLE = connection.prepareStatement("UPDATE vocable SET level = ((level + ?) + ABS(level + ?)) / 2 WHERE vocable_id = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MainGUI(int user_id, String username) {
        super(Localisation.getString("main_gui"));
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
        addVocable.addActionListener(e -> createVocable(vocable1TextArea.getText(), vocable2TextArea.getText()));
        wordTable.setModel(new AbstractTableModel() {
            public String getColumnName(int column) { return columnNames[column]; }
            public int getRowCount() { return rowData.size(); }
            public int getColumnCount() { return columnNames.length; }
            public Object getValueAt(int row, int col) { return rowData.get(row)[col]; }
        });

        updateLanguages();
        updateLanguage();
        showButton.addActionListener(e -> showSecondWord());
        unknownButton.addActionListener(e -> {
            try {
                SQL_UPDATE_VOCABLE.setInt(1, -2);
                SQL_UPDATE_VOCABLE.setInt(2, -2);
                SQL_UPDATE_VOCABLE.setInt(3, selected_word_id);
                SQL_UPDATE_VOCABLE.executeUpdate();
                updateWords();
                showRandomWord();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        knownButton.addActionListener(e -> {
            try {
                SQL_UPDATE_VOCABLE.setInt(1, 1);
                SQL_UPDATE_VOCABLE.setInt(2, 1);
                SQL_UPDATE_VOCABLE.setInt(3, selected_word_id);
                SQL_UPDATE_VOCABLE.executeUpdate();
                updateWords();
                showRandomWord();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        swapDirektionCheckBox.addActionListener(e -> showRandomWord());
    }

    private void showSecondWord() {
        word2Practise.setText(word2);
        unknownButton.setEnabled(true);
        knownButton.setEnabled(true);
        showButton.setEnabled(false);
    }

    private void showRandomWord() {
        if (subjectComboBox.getSelectedIndex() != -1) {
            try {
                SQL_SELECT_VOCABLE.setInt(1, subject_mapping.get(subjectComboBox.getSelectedIndex()));
                ResultSet rs = SQL_SELECT_VOCABLE.executeQuery();
                if (rs.next()) {
                    selected_word_id = rs.getInt(1);
                    word1Practise.setText(rs.getString(swapDirektionCheckBox.isSelected() ? 3 : 2));
                    word2Practise.setText("");
                    word2 = rs.getString(swapDirektionCheckBox.isSelected() ? 2 : 3);
                    showButton.setEnabled(true);
                    unknownButton.setEnabled(false);
                    knownButton.setEnabled(false);
                } else {
                    word1Practise.setText("");
                    word2Practise.setText("");
                    showButton.setEnabled(false);
                    unknownButton.setEnabled(false);
                    knownButton.setEnabled(false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createVocable(String word1, String word2) {
        if (word1.length() < 1 || word2.length() < 1)
            return;
        if (subjectComboBox.getSelectedIndex() != -1) {
            try {
                SQL_INSERT_VOCABLE.setInt(1, subject_mapping.get(subjectComboBox.getSelectedIndex()));
                SQL_INSERT_VOCABLE.setString(2, word1);
                SQL_INSERT_VOCABLE.setString(3, word2);
                SQL_INSERT_VOCABLE.executeUpdate();

                vocable1TextArea.setText("");
                vocable2TextArea.setText("");
                updateWords();
                showRandomWord();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
            subjectComboBox.setSelectedIndex(subjectComboBox.getItemCount() - 1);
            updateLanguage();
        }
        else {
            JOptionPane.showMessageDialog(this, "Fächerkombination existiert bereits", "Fächerkombination existiert bereits", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateLanguage() {
        if (subjectComboBox.getSelectedIndex() != -1) {
            try {
                SQL_SELECT_SUBJECT.setInt(1, subject_mapping.get(subjectComboBox.getSelectedIndex()));
                ResultSet rs = SQL_SELECT_SUBJECT.executeQuery();
                rs.next();
                String l1 = rs.getString(1), l2 = rs.getString(2);
                primaryLanguage.setText(l1);
                secondaryLanguage.setText(l2);
                primaryLanguage1.setText(columnNames[1] = l1);
                secondaryLanguage1.setText(columnNames[2] = l2);
                wordTable.tableChanged(new TableModelEvent(wordTable.getModel(), TableModelEvent.HEADER_ROW));
                updateWords();
                showRandomWord();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWords() {
        try {
            SQL_SELECT_VOCABLES.setInt(1, subject_mapping.get(subjectComboBox.getSelectedIndex()));
            ResultSet rs = SQL_SELECT_VOCABLES.executeQuery();
            rowData.clear();
            while (rs.next()) {
                rowData.add(new String[] { Integer.toString(rs.getInt(1)), rs.getString(2), rs.getString(3), Integer.toString(rs.getInt(4)) });
            }
            wordTable.addNotify();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLanguages(){
        try {
            SQL_SELECT_SUBJECTS.setInt(1, user_id);
            ResultSet rs = SQL_SELECT_SUBJECTS.executeQuery();

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
        wordTable.getTableHeader().setReorderingAllowed(false);
    }

}
