package com.cbot.integration;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class SettingsManager {
    private final String SETTINGS_FILE = "settings.json";
    private String dataFolderPath = "";
    private JSONObject settings = new JSONObject();
    private Logger logger = null;

    public SettingsManager(Logger logger, String dataFolder) {
        this.dataFolderPath = dataFolder;

        try {
            settings.put("authorization", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        logger.info(message);
    }

    // Returns the absolute path to the settings file
    private String getSettingsPath() {
        return dataFolderPath + "/" + SETTINGS_FILE;
    }

    // Attempts to save settings. Will establish a blank
    public void saveSettings() {
        try {
            File settingsFile = new File(getSettingsPath());
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
            }

            if (settings == null) {
                log("Settings object is invalid!");
                return;
            }

            FileWriter writer = new FileWriter(getSettingsPath());
            writer.write(settings.toString());
            writer.flush();
        } catch (IOException e) {
            log("Failed to save settings!");
            e.printStackTrace();
        }
    }

    // Attempts to read in the json settings
    // Will save default settings if they don't exist
    public void loadSettings() {
        try {

            File f = new File(dataFolderPath + '/');
            if (!f.exists()) {
                f.mkdir();
            }

            // Get a new file
            File settingsFile = new File(getSettingsPath());

            // Settings don't exist, go ahead and save default
            if (!settingsFile.exists()) {
                saveSettings();
                return;
            }

            String content = FileUtils.readFileToString(settingsFile);

            // Create a new json reader + parser
            settings = new JSONObject(content);

        } catch (IOException | JSONException e) {
            log("Failed to load settings!");
            e.printStackTrace();
        }
    }

    public String get(String key) {
        String value = null;
        try {
            value = settings.get(key).toString();
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }

        return value;
    }

    public boolean set(String key, String value) {
        try {
            settings.put(key, value);

            saveSettings();
        } catch (JSONException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }
}
