package Dao;

import Modelo.Evento;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object para la entidad Evento.
 */
public class EventoDAO {

    public void guardar(Evento evento) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(evento);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Evento evento) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(evento);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void eliminar(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Evento evento = session.get(Evento.class, id);
            if (evento != null) {
                // Comprobacion extra: solo eliminar si no tiene ventas?
                // Esto se podria manejar aqui o en el controlador/servicio antes de llamar.
                // Como es DAO puro, intentamos borrar. Si falla por FK, saltará excepción.
                session.remove(evento);
                System.out.println("Evento eliminado con id: " + id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("No se puede eliminar el evento (posiblemente tiene entradas vendidas).");
        }
    }

    public Evento obtener(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Evento.class, id);
        }
    }

    public List<Evento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Evento", Evento.class).list();
        }
    }

    // Filtros

    public List<Evento> buscarPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Uso de parámetros para evitar inyección SQL (aunque en HQL es menos riesgo,
            // es buena practica)
            Query<Evento> query = session.createQuery("from Evento where nombre like :nombre", Evento.class);
            query.setParameter("nombre", "%" + nombre + "%");
            return query.list();
        }
    }

    public List<Evento> filtrar(String tipo, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("from Evento where 1=1");
            if (tipo != null && !tipo.isEmpty()) {
                hql.append(" and tipo = :tipo");
            }
            if (fechaInicio != null) {
                hql.append(" and fecha >= :fechaInicio");
            }
            if (fechaFin != null) {
                hql.append(" and fecha <= :fechaFin");
            }

            Query<Evento> query = session.createQuery(hql.toString(), Evento.class);

            if (tipo != null && !tipo.isEmpty()) {
                query.setParameter("tipo", tipo);
            }
            if (fechaInicio != null) {
                query.setParameter("fechaInicio", fechaInicio);
            }
            if (fechaFin != null) {
                query.setParameter("fechaFin", fechaFin);
            }

            return query.list();
        }
    }
}
