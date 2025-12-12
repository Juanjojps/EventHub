package Dao;

import Modelo.Entrada;
import Modelo.Evento;
import Modelo.Asistente;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class EntradaDAO {

    /**
     * Intenta vender una entrada para un evento y un asistente.
     * Realiza la comprobacion de aforo dentro de la transaccion.
     * 
     * @param evento
     * @param asistente
     * @throws Exception si no hay aforo o hay error
     */
    public void venderEntrada(Evento evento, Asistente asistente) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // 1. Consultar entradas vendidas para este evento
            // IMPORTANTE: Refrescar el evento o hacer count query para tener el dato real
            // en esta transaccion
            Query<Long> countQuery = session.createQuery("select count(e) from Entrada e where e.evento.id = :eventoId",
                    Long.class);
            countQuery.setParameter("eventoId", evento.getId());
            Long entradasVendidas = countQuery.uniqueResult();

            // 2. Comprobar aforo (usamos el aforo del objeto pasado,
            // pero idealmente deberíamos recargar el evento de la BD por si cambió)
            Evento eventoPersistido = session.get(Evento.class, evento.getId());

            if (entradasVendidas >= eventoPersistido.getAforoMaximo()) {
                throw new Exception("Aforo completo. No se pueden vender más entradas.");
            }

            // 3. Crear y guardar nueva entrada
            Entrada nuevaEntrada = new Entrada();
            nuevaEntrada.setEvento(eventoPersistido); // Usar el persistido para asociar en sesion
            nuevaEntrada.setAsistente(asistente); // Igual, quizas merge si no esta en sesion
            nuevaEntrada.setFechaCompra(LocalDateTime.now());
            nuevaEntrada.setPrecioFinal(eventoPersistido.getPrecioEntrada()); // Simple: precio evento

            session.persist(nuevaEntrada);

            // 4. Commit
            transaction.commit();
            System.out.println("Entrada vendida con éxito.");

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            throw e; // Relanzar para avisar a la UI
        }
    }

    public List<Entrada> listarPorEvento(Long eventoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Entrada> query = session.createQuery("from Entrada where evento.id = :eventoId", Entrada.class);
            query.setParameter("eventoId", eventoId);
            return query.list();
        }
    }

    public List<Entrada> listarPorAsistente(Long asistenteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Entrada> query = session.createQuery("from Entrada where asistente.id = :asistenteId", Entrada.class);
            query.setParameter("asistenteId", asistenteId);
            return query.list();
        }
    }

    // Consultas de reporte

    public double calcularRecaudacion(Long eventoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                    "select sum(e.precioFinal) from Entrada e where e.evento.id = :eventoId", Double.class);
            query.setParameter("eventoId", eventoId);
            Double res = query.uniqueResult();
            return res == null ? 0.0 : res;
        }
    }
}
