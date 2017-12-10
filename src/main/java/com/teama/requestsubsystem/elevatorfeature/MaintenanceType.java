package com.teama.requestsubsystem.elevatorfeature;

/**
 * Created by jakepardue on 12/9/17.
 */
public enum MaintenanceType {
    CODECHECK("Code check and updates"), MONTHLYINSPECTION("Monthly Inspection"), SAFETYCHECKS("Required checks"),
    TESTS("Conducting Tests"), PERSONTRAPPED("Passenger is trapped inside");

    private String type;

    MaintenanceType(String string){
        this.type = string;
    }

    public static MaintenanceType getMaintenanceType(String s) {
        for (MaintenanceType c: MaintenanceType.values()) {
            if (c.toString().equals(s)) {
                return c;
            }
        }
        throw new IllegalArgumentException("No such Maintenance Type, " + s);
    }
}
