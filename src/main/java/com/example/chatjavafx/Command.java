package com.example.chatjavafx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {

    AUTH("/auth"){ // тут надо вернуть 2 параметра
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },          // /auth login1 pass1
    AUTHOK("/authok"){
        @Override
        public String[] parse(String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMITER)[1]}; //берем только ник, он под индексом 1
        }
    },      // /authok nick1
    PRIVATE_MESSAGE("/w"){
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER, 3);

            return new String[]{split[1], split[2]};
        }
    },  // /w nick1 ...long message
    END("/end"){
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    ERROR("/error"){ // /error сщщбщение об ошибке
        @Override
        public String[] parse(String commandText) {
            String errorMsg = commandText.split(COMMAND_DELIMITER, 2)[1];
            return new String[]{errorMsg};
        }
    },

    CHANGE_NICK("/change_nick"){ // /error сщщбщение об ошибке
        @Override
        public String[] parse(String commandText) {
            String newNick = commandText.split(COMMAND_DELIMITER, 2)[1];
            return new String[]{newNick};
        }
    },

    CLIENTS("/clients"){
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);

            return Arrays.stream(split).skip(1).toArray(String[]::new);

//            final String[] nicks = new String[split.length - 1];
//            for (int i = 1; i < split.length; i++) {
//                nicks[i - 1] = split[i];
//            }
//            return nicks;
        }
    };


    // Сделали мапу с перечислениями
    private static final Map<String, Command> map = Stream.of(Command.values())
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));

    private String command;
    private String[] params = new String[0];
    static final String COMMAND_DELIMITER = "\\s+";

    Command (String command) {
        this.command = command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public String[] getParams() {
        return params;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommand(String message) {
        message = message.trim();
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' is not a command");
        }
        final int index = message.indexOf(" ");
        String cmd = index > 0 ? message.substring(0, index) : message;
        final Command command = map.get(cmd);
        if (command == null){
            throw new RuntimeException("'" + cmd + "' not command");
        }
        return command;
    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        final String command = this.getCommand();

        return command + (params == null ? "" : " " + String.join(" ", params));
    }

}
