package ma.youcode.lineperm.dao;

import ma.youcode.lineperm.models.Fichier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO JDBC pour la table fichiers. */
public class FichierDao extends AbstractDao<Fichier> {
    public FichierDao() throws SQLException {
        super();
    }

    @Override
    public Fichier ajouter(Fichier fichier) throws SQLException {
        String sql = "INSERT INTO fichiers (nom, droits, user_id) "
                + "SELECT ?, ?, id FROM users WHERE login = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fichier.getName());
            statement.setString(2, fichier.getDroits());
            statement.setString(3, fichier.getOwner());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Proprietaire introuvable: " + fichier.getOwner());
            }
        }

        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (result.next()) {
                fichier.setId(result.getInt(1));
            }
        }
        return fichier;
    }

    public List<Fichier> trouverParProprietaire(String login) throws SQLException {
        String sql = "SELECT f.id, f.nom, f.droits, u.login AS proprietaire "
                + "FROM fichiers f JOIN users u ON f.user_id = u.id "
                + "WHERE u.login = ? ORDER BY f.id";
        List<Fichier> fichiers = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    fichiers.add(mapper(result));
                }
            }
        }
        return fichiers;
    }

    public boolean modifierDroits(int id, String nouveauxDroits) throws SQLException {
        String sql = "UPDATE fichiers SET droits = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nouveauxDroits);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Fichier> trouverParId(int id) throws SQLException {
        String sql = "SELECT f.id, f.nom, f.droits, u.login AS proprietaire "
                + "FROM fichiers f JOIN users u ON f.user_id = u.id WHERE f.id = ?";
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
    public List<Fichier> trouverTout() throws SQLException {
        String sql = "SELECT f.id, f.nom, f.droits, u.login AS proprietaire "
                + "FROM fichiers f JOIN users u ON f.user_id = u.id ORDER BY f.id";
        List<Fichier> fichiers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                fichiers.add(mapper(result));
            }
        }
        return fichiers;
    }

    @Override
    public boolean supprimerParId(int id) throws SQLException {
        String sql = "DELETE FROM fichiers WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Fichier mapper(ResultSet result) throws SQLException {
        return new Fichier(result.getInt("id"), result.getString("nom"),
                result.getString("proprietaire"), result.getString("droits"));
    }
}
