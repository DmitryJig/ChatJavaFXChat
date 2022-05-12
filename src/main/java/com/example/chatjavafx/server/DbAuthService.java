package com.example.chatjavafx.server;

import java.sql.*;

public class DbAuthService implements AuthService {

    private Connection connection;

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
            System.out.println("Ошибка поиска пользователя в базе данных");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            System.out.println("Сервис аутентификации запущен, соединение с базой данных установлено");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Сервис аутентификации успешно остановлен");
            }
        } catch (SQLException e) {
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
            System.out.println("Ошибка смены ника в БД, возможно ник занят");
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
