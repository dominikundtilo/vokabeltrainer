package de.tilosp.vokabeltrainer;

import javax.swing.*;

/**
 * Created by Dominik on 22.06.2016.
 */
public class GUI extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel panel;
    private JPanel createPanel;
    private JPanel practicePanel;
    private JPanel editPanel;
    private JButton addWord;
    private JLabel primaryLanguage;
    private JLabel secondaryLanguage;
    private JTextField inputprimaryWord;
    private JTextField inputSeondaryWord;
    private JComboBox chosenLanguageAdd;
    private JTextField firstLanguageInput;
    private JTextField secondLanguageInput;
    private JButton addSubjectButton;
    private JCheckBox swapDirektionCheckBox;
    private JLabel seenWord;
    private JLabel knownWord;
    private JComboBox chosenLanguagePractice;
    private JButton showButton;
    private JButton knownButton;
    private JButton unknownButton;
    private JComboBox comboBox1;
    private JTable wordTable;

    public GUI() {
        super("Vokabeltrainer");
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }


    private void createUIComponents() {
        String[] headLine = {"numbre", "first language", "second language", "word level", "bearbeiten"};

        Object[][] data = new Object[15/*anzahl der Vokabelpaare*/][5];

        for(int x = 0; x < 15; x++){
            data[x] = new Object[]{x,"tell", "sagen", x+1, 1};
        }

        wordTable = new JTable(data,headLine);
    }

}
