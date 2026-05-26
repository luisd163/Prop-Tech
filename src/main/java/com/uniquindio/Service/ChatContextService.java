package com.uniquindio.Service;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Repositorio.VisitaRepositorio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ChatContextService {

    private final ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
    private final InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
    private final VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
    private final GrafoService grafoService = GrafoService.getInstancia();

    public String construirContextoRag(String idCliente) {
        grafoService.cargarDesdePersistencia();

        Cliente cliente = clienteRepositorio.obtenerClientes().get(idCliente);
        if (cliente == null) {
            return "No hay datos de perfil para este cliente.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFIL DEL CLIENTE (datos reales del sistema) ===\n");
        sb.append("Nombre: ").append(valor(cliente.getNombre())).append('\n');
        sb.append("Presupuesto máximo (COP): ").append(cliente.getPresupuesto()).append('\n');
        sb.append("Zonas de interés: ").append(lista(cliente.getZonasDeInteres())).append('\n');
        sb.append("Tipo inmueble deseado: ").append(
                cliente.getTipoInmuebleDeseado() != null ? cliente.getTipoInmuebleDeseado().name() : "N/D"
        ).append('\n');
        sb.append("Habitaciones mínimas: ").append(cliente.getCantidadMinimaHabitaciones()).append('\n');
        sb.append("Estado búsqueda: ").append(
                cliente.getEstadoBusqueda() != null ? cliente.getEstadoBusqueda().name() : "N/D"
        ).append('\n');

        sb.append("\n=== FAVORITOS DEL CLIENTE ===\n");
        if (cliente.getFavoritos() == null || cliente.getFavoritos().isEmpty()) {
            sb.append("Sin favoritos guardados.\n");
        } else {
            for (Inmueble fav : cliente.getFavoritos()) {
                if (fav != null) {
                    sb.append("- ").append(describirInmueble(fav)).append('\n');
                }
            }
        }

        sb.append("\n=== RESUMEN DEL CATÁLOGO ===\n");
        sb.append(resumirCatalogo());

        sb.append("\n=== INMUEBLES RECOMENDADOS (máx. 5, grafo + presupuesto) ===\n");
        List<Inmueble> top5 = obtenerTop5Inmuebles(idCliente, cliente);
        if (top5.isEmpty()) {
            sb.append("No hay inmuebles recomendados en catálogo para este perfil.\n");
        } else {
            int i = 1;
            for (Inmueble inm : top5) {
                sb.append(i++).append(". ").append(describirInmueble(inm)).append('\n');
            }
        }

        sb.append("\n=== VISITAS DEL CLIENTE ===\n");
        List<Visita> visitasCliente = listarVisitasCliente(idCliente);
        if (visitasCliente.isEmpty()) {
            sb.append("Sin visitas registradas.\n");
        } else {
            for (Visita v : visitasCliente) {
                if (v.getInmueble() == null) {
                    continue;
                }
                sb.append("- ").append(v.getEstado() != null ? v.getEstado().name() : "N/D")
                        .append(" | ").append(describirInmueble(v.getInmueble()));
                if (v.getFecha() != null) {
                    sb.append(" | fecha ").append(v.getFecha());
                }
                sb.append('\n');
            }
        }

        sb.append("\n=== INMUEBLES VISITADOS EN EL GRAFO ===\n");
        List<String> visitadosGrafo = grafoService.obtenerInmueblesVisitadosPor(idCliente);
        if (visitadosGrafo.isEmpty()) {
            sb.append("Ninguna visita REALIZADA en el grafo aún.\n");
        } else {
            for (String codigo : visitadosGrafo) {
                Inmueble inm = inmuebleRepositorio.obtenerInmueble(codigo);
                if (inm != null) {
                    sb.append("- ").append(describirInmueble(inm)).append('\n');
                } else {
                    sb.append("- código ").append(codigo).append('\n');
                }
            }
        }

        return sb.toString();
    }

    private List<Inmueble> obtenerTop5Inmuebles(String idCliente, Cliente cliente) {
        Set<String> codigos = new LinkedHashSet<>();
        for (String cod : grafoService.recomendarPorComportamiento(idCliente)) {
            codigos.add(cod);
            if (codigos.size() >= 5) {
                break;
            }
        }

        if (codigos.size() < 5) {
            for (Inmueble inm : inmuebleRepositorio.obtenerInmuebles().values()) {
                if (inm == null || inm.getCodigo() == null) {
                    continue;
                }
                if (!cumplePresupuesto(cliente, inm)) {
                    continue;
                }
                codigos.add(inm.getCodigo());
                if (codigos.size() >= 5) {
                    break;
                }
            }
        }

        List<Inmueble> resultado = new ArrayList<>();
        for (String codigo : codigos) {
            Inmueble inm = inmuebleRepositorio.obtenerInmueble(codigo);
            if (inm != null) {
                resultado.add(inm);
            }
        }
        return resultado;
    }

    private boolean cumplePresupuesto(Cliente cliente, Inmueble inm) {
        if (cliente.getPresupuesto() <= 0) {
            return true;
        }
        return inm.getPrecio() <= cliente.getPresupuesto();
    }

    private List<Visita> listarVisitasCliente(String idCliente) {
        List<Visita> lista = new ArrayList<>();
        for (Visita v : visitaRepositorio.obtenerVisitas().values()) {
            if (v != null && v.getCliente() != null
                    && idCliente.equals(v.getCliente().getIdentificacion())) {
                lista.add(v);
            }
        }
        return lista;
    }

    private String describirInmueble(Inmueble inm) {
        return String.format(Locale.ROOT,
                "[%s] %s | %s %s | %s | precio COP %.0f | %d hab | %s",
                inm.getCodigo(),
                valor(inm.getNombre()),
                inm.getTipoInmueble() != null ? inm.getTipoInmueble().name() : "TIPO",
                inm.getFinalidad() != null ? inm.getFinalidad().name() : "",
                valor(inm.getCiudad()) + " / " + valor(inm.getBarrio()),
                (double) inm.getPrecio(),
                inm.getNumeroHabitaciones(),
                inm.getDisponibilidad() != null ? inm.getDisponibilidad().name() : "N/D"
        );
    }

    private String lista(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "N/D";
        }
        return String.join(", ", items);
    }

    private String valor(String s) {
        return s == null || s.isBlank() ? "N/D" : s.trim();
    }

    private String resumirCatalogo() {
        int total = 0;
        int disponibles = 0;
        int reservados = 0;
        StringBuilder zonas = new StringBuilder();

        for (Inmueble inm : inmuebleRepositorio.obtenerInmuebles().values()) {
            if (inm == null) {
                continue;
            }
            total++;
            if (inm.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE) {
                disponibles++;
            } else if (inm.getDisponibilidad() == Inmueble.Disponibilidad.RESERVADO
                    || inm.getDisponibilidad() == Inmueble.Disponibilidad.NO_DISPONIBLE) {
                reservados++;
            }
            String zona = inm.getBarrio() != null && !inm.getBarrio().isBlank()
                    ? inm.getBarrio()
                    : inm.getCiudad();
            if (zona != null && !zona.isBlank() && zonas.indexOf(zona) < 0) {
                if (zonas.length() > 0) {
                    zonas.append(", ");
                }
                zonas.append(zona);
            }
        }

        return "Total inmuebles: " + total
                + " | Disponibles: " + disponibles
                + " | Reservados/no disponibles: " + reservados
                + "\nZonas en catálogo: " + (zonas.length() > 0 ? zonas : "N/D") + "\n";
    }
}
