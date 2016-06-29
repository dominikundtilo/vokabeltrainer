package de.tilosp.vokabeltrainer.localisation;

import java.util.ResourceBundle;

public class Localisation {

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle(Localisation.class.getName().toLowerCase());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
}
