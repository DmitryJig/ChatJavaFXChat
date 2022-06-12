package com.example.chatjavafx.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DbAuthService implements AuthService {

    private Connection connection;
    private final Logger log4jLogger = LogManager.getLogger(DbAuthService.class);

    public DbAuthService() {
        run();
    }

    //    public static void main(String[] args) {
//        DbAuthService service = new DbAuthService();
//        try {
//            service.run();
//            service.changeNick("nick2", "godzilla1");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            service.close();
//        }
//    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM users WHERE login = ? AND password = ?;")) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            log4jLogger.warn("Ошибка поиска пользователя в базе данных");
//            System.out.println("Ошибка поиска пользователя в базе данных");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            log4jLogger.info("Сервис аутентификации запущен, соединение с базой данных установлено");
//            System.out.println("Сервис аутентификации запущен, соединение с базой данных установлено");
        } catch (SQLException e) {
            log4jLogger.warn("Ошибка подключения к базе данных {}", e.getMessage());
//            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                log4jLogger.info("Сервис аутентификации успешно остановлен");
//                System.out.println("Сервис аутентификации успешно остановлен");
            }
        } catch (SQLException e) {
            log4jLogger.warn("Ошибка закрытия соединения с базой данных");
            throw new RuntimeException("Ошибка закрытия соединения с базой данных");
        }
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?")) {
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            log4jLogger.warn("Ошибка смены ника в БД, возможно ник занят");
//            System.out.println("Ошибка смены ника в БД, возможно ник занят");
            return false;
        }
        return true;
    }

    /**
     * Метод записи в базу 5 юзеров, используем 1 раз, можно потом удалить
     */
    private void insertUsersBach() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into users (login, password, nick) values (?, ?, ?);")) {
            for (int i = 0; i < 5; i++) {
                ps.setString(1, "login" + i);
                ps.setString(2, "pass" + i);
                ps.setString(3, "nick" + i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Метод удаления бд
     *
     * @param tableName
     * @throws SQLException
     */
    private void dropTable(String tableName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName + ";")) {
            ps.executeUpdate();
        }
    }

    /**
     * Метод создания таблицы, используем 1 раз, потом можно удалить
     */
    private void createTable() {
        try (final PreparedStatement statement = connection.prepareStatement("" +
                " CREATE TABLE IF NOT EXISTS users (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " login TEXT," +
                " password TEXT," +
                " nick TEXT UNIQUE);")) {
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
