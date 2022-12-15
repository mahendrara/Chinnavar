/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

/**
 *
 * @author Jia Li
 */
 public enum Operation {
    UNKNOWN,
    MODIFY,
    CREATE,
    DELETE,
    ARCHIVE;

    public static Operation value(String operation) {
        return Operation.valueOf(operation);
    }

    public static String name(Operation operataion) {
        return operataion.name();
    }
}
