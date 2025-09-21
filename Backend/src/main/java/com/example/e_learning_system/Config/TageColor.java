package com.example.e_learning_system.Config;


public enum TageColor {
    PROGRAMMING("#FF5733"),        // Red-Orange
    DATA_SCIENCE("#33FF57"),       // Green
    MACHINE_LEARNING("#3357FF"),   // Blue
    WEB_DEVELOPMENT("#FF33A8"),    // Pink
    MOBILE_DEVELOPMENT("#A833FF"), // Purple
    CLOUD_COMPUTING("#33FFF6"),    // Cyan
    CYBER_SECURITY("#FF8C33"),     // Orange
    DEVOPS("#8C33FF"),             // Violet
    DATABASES("#33FF8C"),          // Light Green
    SOFTWARE_ENGINEERING("#FFC300"), // Yellow
    ARTIFICIAL_INTELLIGENCE("#FFB833"), // Light Orange
    GAME_DEVELOPMENT("#33B8FF"),        // Light Blue
    UI_UX_DESIGN("#FF33B8"),            // Magenta
    NETWORKING("#B8FF33"),              // Lime
    BLOCKCHAIN("#B833FF"),              // Deep Purple
    AR_VR("#33FFB8"),                   // Aqua
    ROBOTICS("#FF338C"),                // Rose
    IOT("#338CFF"),                     // Sky Blue
    BIG_DATA("#8CFF33"),                // Chartreuse
    SECURITY("#FF3333");                // Bright Red

    private final String colorCode;

    TageColor(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }
}
