package com.urbancode.terraform.main;

public enum AllowedCommands {

    CREATE("create"), DESTROY("destroy"), SUSPEND("suspend"), RESUME("resume"), TAKE_SNAPSHOT("snapshot");

    private String commandName;

    //----------------------------------------------------------------------------------------------
    private AllowedCommands(String cmd) {
        this.commandName = cmd;
    }

    //----------------------------------------------------------------------------------------------
    public String getCommandName() {
        return this.commandName;
    }

    //----------------------------------------------------------------------------------------------
    public boolean equalsIgnoreCase(String string) {
        return commandName.equalsIgnoreCase(string);
    }

    //----------------------------------------------------------------------------------------------
    public static boolean contains(String testCommand) {
        for (AllowedCommands c : values()) {
            if (c.getCommandName().equals(testCommand)) {
                return true;
            }
        }

        return false;
    }
}
