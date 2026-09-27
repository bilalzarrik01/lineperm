package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO JDBC pour la table users. */
public class UserDao extends AbstractDao<User> {
    public UserDao() throws SQLException {
        super();
    }

    @Override
    public User ajouter(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            statement.executeUpdate();
        }

        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (result.next()) {
                user.setId(result.getInt(1));
            }
        }
        return user;
    }

    public Optional<User> trouverParLogin(String login) throws SQLException {
        String sql = "SELECT id, login, password FROM users WHERE login = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapper(result));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> trouverParId(int id) throws SQLException {
        String sql = "SELECT id, login, password FROM users WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapper(result));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> trouverTout() throws SQLException {
        String sql = "SELECT id, login, password FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(mapper(result));
            }
        }
        return users;
    }

    @Override
    public boolean supprimerParId(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private User mapper(ResultSet result) throws SQLException {
        return new User(result.getInt("id"), result.getString("login"), result.getString("password"));
    }
}
