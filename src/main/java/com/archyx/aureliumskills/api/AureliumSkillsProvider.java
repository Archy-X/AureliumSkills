package com.archyx.aureliumskills.api;

import com.archyx.aureliumskills.AureliumSkills;

public class AureliumSkillsProvider {

    private static AureliumSkills instance;

    /**
     * Gets an instance of the main plugin class for easier access to
     * internal classes, use with caution as internal code may change
     * between versions.
     */
    public static AureliumSkills get() {
        if (instance == null) {
            throw new IllegalStateException("The AureliumSkills API is not loaded yet");
        }
        return instance;
    }

    /**
     * Registers the API to the plugin instance. For internal use only, do not call
     */
    public static void register(AureliumSkills instance) {
        if (AureliumSkillsProvider.instance == null) {
            AureliumSkillsProvider.instance = instance;
        } else {
            throw new IllegalStateException("The AureliumSkills API is already registered");
        }
    }

}
