package ma.youcode.lineperm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton qui fournit une connexion SQLite partagee dans toute l'application.
 */
public final class DBConnection {
    private static final String URL = "jdbc:sqlite:resources/DB/linepermDB.db";
    private static DBConnection instance;

    private final Connection connection;

    private DBConnection() throws SQLException {
        connection = DriverManager.getConnection(URL);

       
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
