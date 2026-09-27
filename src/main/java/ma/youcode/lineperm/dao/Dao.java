package ma.youcode.lineperm.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrat commun pour lire et enregistrer des objets en base de donnees.
 *
 * @param <T> type d'objet gere par le DAO (User, Fichier ou AccessLog)
 */
public interface Dao<T> {
    T ajouter(T objet) throws SQLException;

    Optional<T> trouverParId(int id) throws SQLException;

    List<T> trouverTout() throws SQLException;

    boolean supprimerParId(int id) throws SQLException;
}
