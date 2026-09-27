package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.database.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe de base qui partage la connexion JDBC entre les DAO concrets.
 *
 * @param <T> type d'objet gere par le DAO
 */
public abstract class AbstractDao<T> implements Dao<T> {
    protected final Connection connection;

    protected AbstractDao() throws SQLException {
        this.connection = DBConnection.getInstance().getConnection();
    }
}
