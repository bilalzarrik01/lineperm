package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.models.AccessLog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap;

public class LogDao extends AbstractDao<AccessLog> {
    private static final String SELECT_LOGS = "SELECT l.id, l.user_id, l.file_id, l.action, l.resultat, "
            + "l.created_at, u.login AS utilisateur, f.nom AS fichier "
            + "FROM logs l JOIN users u ON l.user_id = u.id "
            + "JOIN fichiers f ON l.file_id = f.id ";

    public LogDao() throws SQLException {
        super();
    }

    @Override
    public AccessLog ajouter(AccessLog log) throws SQLException {
        int userId = log.getUserId() > 0 ? log.getUserId() : trouverUserId(log.getUtilisateur());
        int fileId = log.getFileId() > 0 ? log.getFileId() : trouverFileId(log.getFichier(), userId);

        String sql = "INSERT INTO logs (user_id, file_id, action, resultat) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, fileId);
            statement.setString(3, log.getAction());
            statement.setString(4, log.getResultat());
            statement.executeUpdate();
        }

        log.setUserId(userId);
        log.setFileId(fileId);
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (result.next()) {
                log.setId(result.getInt(1));
            }
        }
        return log;
    }

    public long compterActions() throws SQLException {
        return compter("SELECT COUNT(*) FROM logs");
    }

    public long compterAccesRefuses() throws SQLException {
        return compter("SELECT COUNT(*) FROM logs WHERE UPPER(resultat) = 'REFUSE'");
    }

    public long compterUtilisateursDistincts() throws SQLException {
        return compter("SELECT COUNT(DISTINCT user_id) FROM logs");
    }

    public Map<String, Long> compterActionsParUtilisateur() throws SQLException {
        String sql = "SELECT u.login, COUNT(*) AS total FROM logs l "
                + "JOIN users u ON l.user_id = u.id GROUP BY u.id, u.login ORDER BY u.login";
        Map<String, Long> resultats = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                resultats.put(result.getString("login"), result.getLong("total"));
            }
        }
        return resultats;
    }

    public List<Map.Entry<String, Long>> top3FichiersConsultes() throws SQLException {
        String sql = "SELECT f.nom, COUNT(*) AS total FROM logs l "
                + "JOIN fichiers f ON l.file_id = f.id WHERE UPPER(l.action) = 'READ' "
                + "GROUP BY f.id, f.nom ORDER BY total DESC, f.nom LIMIT 3";
        List<Map.Entry<String, Long>> resultats = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                resultats.add(new AbstractMap.SimpleEntry<>(result.getString("nom"), result.getLong("total")));
            }
        }
        return resultats;
    }

    public List<AccessLog> trouverRefusParUtilisateur(String login) throws SQLException {
        String sql = SELECT_LOGS + "WHERE UPPER(l.resultat) = 'REFUSE' AND LOWER(u.login) = LOWER(?) "
                + "ORDER BY l.created_at DESC, l.id DESC";
        List<AccessLog> logs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    logs.add(mapper(result));
                }
            }
        }
        return logs;
    }

    public Optional<Map.Entry<String, Long>> trouverUtilisateurLePlusActif() throws SQLException {
        String sql = "SELECT u.login, COUNT(*) AS total FROM logs l "
                + "JOIN users u ON l.user_id = u.id GROUP BY u.id, u.login "
                + "ORDER BY total DESC, u.login LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return Optional.of(new AbstractMap.SimpleEntry<>(result.getString("login"), result.getLong("total")));
            }
        }
        return Optional.empty();
    }

    public Map<String, Long> compterActionsParType() throws SQLException {
        String sql = "SELECT action, COUNT(*) AS total FROM logs GROUP BY action ORDER BY action";
        Map<String, Long> resultats = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                resultats.put(result.getString("action"), result.getLong("total"));
            }
        }
        return resultats;
    }

    @Override
    public Optional<AccessLog> trouverParId(int id) throws SQLException {
        String sql = SELECT_LOGS + "WHERE l.id = ?";
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
    public List<AccessLog> trouverTout() throws SQLException {
        List<AccessLog> logs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_LOGS + "ORDER BY l.id");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                logs.add(mapper(result));
            }
        }
        return logs;
    }

    @Override
    public boolean supprimerParId(int id) throws SQLException {
        throw new SQLException("Un log d'audit est immuable et ne peut pas etre supprime.");
    }

    private long compter(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getLong(1) : 0;
        }
    }

    private int trouverUserId(String login) throws SQLException {
        String sql = "SELECT id FROM users WHERE login = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id");
                }
            }
        }
        throw new SQLException("Utilisateur introuvable: " + login);
    }

    private int trouverFileId(String nom, int userId) throws SQLException {
        String sql = "SELECT id FROM fichiers WHERE nom = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nom);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id");
                }
            }
        }
        throw new SQLException("Fichier introuvable pour cet utilisateur: " + nom);
    }

    private AccessLog mapper(ResultSet result) throws SQLException {
        LocalDateTime dateHeure = LocalDateTime.parse(result.getString("created_at").replace(' ', 'T'));
        return new AccessLog(result.getInt("id"), result.getInt("user_id"), result.getInt("file_id"),
                dateHeure.toLocalDate(), dateHeure.toLocalTime(), result.getString("utilisateur"),
                result.getString("action"), result.getString("fichier"), result.getString("resultat"));
    }
}
