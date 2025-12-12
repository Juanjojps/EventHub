package Dao;

import Modelo.Asistente;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class AsistenteDAO {

    public void guardar(Asistente asistente) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(asistente);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            // Propagar excepción para manejar duplicados de DNI por ejemplo
            new Exception("No se puede guardar el asistente (posiblemente DNI duplicado)");
        }
    }

    public void actualizar(Asistente asistente) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(asistente);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Asistente asistente = session.get(Asistente.class, id);
            if (asistente != null) {
                session.remove(asistente);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("No se puede eliminar el asistente (posiblemente tiene entradas).");
        }
    }

    public List<Asistente> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Asistente", Asistente.class).list();
        }
    }

    public List<Asistente> buscar(String termino) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Asistente> query = session
                    .createQuery("from Asistente where dni like :termino or nombre like :termino", Asistente.class);
            query.setParameter("termino", "%" + termino + "%");
            return query.list();
        }
    }

    public Asistente buscarPorDni(String dni) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Asistente> query = session.createQuery("from Asistente where dni = :dni", Asistente.class);
            query.setParameter("dni", dni);
            return query.uniqueResult();
        }
    }
}
