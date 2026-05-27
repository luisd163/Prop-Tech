package com.uniquindio.Service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.YearMonth;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Operacion;
import com.uniquindio.Estructuras.ArrayListPropio;
import com.uniquindio.Model.Alerta;
import com.uniquindio.Repositorio.AlertaRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;

@Service
public class AsesorHomeService {
    
    AlertaRepositorio alertaRepositorio = new AlertaRepositorio();
    InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();

    // Cantidad de alertas del asesor
    public int cantidadAlertas(Asesor asesor){
        if (asesor == null) {
            return 0;
        }

        int cant = 0;
        for (Alerta alerta : alertaRepositorio.obtenerAlertas()) {
            if (alerta != null && Objects.equals(alerta.getAsesorId(), asesor.getIdentificacion())) {
                cant++;
            }
        }
        return cant;
    }

    // Obtiene los inmuebles asociados a un asesor
    public List<Inmueble> obtenerInmueblesAsesor(Asesor asesor){
        if (asesor == null) {
            return new ArrayList<>();
        }

        InmuebleRepositorio repositorioActualizado = new InmuebleRepositorio();
        List<Inmueble> inmuebles = new ArrayList<>();
        for (Inmueble inmueble : repositorioActualizado.obtenerInmuebles().values()) {
            if (inmueble != null && Objects.equals(inmueble.getCodigoAsesorResponsable(), asesor.getIdentificacion())) {
                inmuebles.add(inmueble);
            }
        }
        return inmuebles;
    }

    // Devuelve la cantidad de inmuebles asociados a un asesor
    public int cantidadInmueblesAsociados(Asesor asesor){
        return obtenerInmueblesAsesor(asesor).size();
    }

    // Obtiene los inmuebles que están relacionados en alertas del asesor
    public java.util.List<Inmueble> obtenerInmueblesEnAlerta(Asesor asesor) {
        java.util.List<Inmueble> resultado = new java.util.ArrayList<>();
        if (asesor == null) return resultado;

        ArrayListPropio<Alerta> alertas = alertaRepositorio.obtenerAlertas();
        java.util.Map<String, Inmueble> todos = inmuebleRepositorio.obtenerInmuebles();

        for (Alerta alerta : alertas) {
            if (alerta == null) continue;
            if (!Objects.equals(alerta.getAsesorId(), asesor.getIdentificacion())) continue;
            String entidad = alerta.getEntidadRelacionada();
            if (entidad == null) continue;
            Inmueble inm = todos.get(entidad);
            if (inm != null) {
                // evitar duplicados
                boolean existe = false;
                for (Inmueble i : resultado) {
                    if (i != null && Objects.equals(i.getCodigo(), inm.getCodigo())) { existe = true; break; }
                }
                if (!existe) resultado.add(inm);
            }
        }

        return resultado;
    }

    // Devuelve la cantidad de operaciones finalizadas del mes actual asociadas al asesor
    public int cantidadCierresMes(Asesor asesor) {
        if (asesor == null || asesor.getOperaciones() == null) {
            return 0;
        }

        YearMonth mesActual = YearMonth.now();
        int cierres = 0;

        for (Operacion operacion : asesor.getOperaciones()) {
            if (operacion == null || operacion.getFecha() == null) {
                continue;
            }

            boolean esDelMesActual = YearMonth.from(operacion.getFecha()).equals(mesActual);
            boolean estaFinalizada = operacion.getEstado() == Operacion.EstadoOperacion.FINALIZADA;

            if (esDelMesActual && estaFinalizada) {
                cierres++;
            }
        }

        return cierres;
    }

    // Calcula la cantidad de visitas de esta semana
    public int cantidadVisitasEstaSemana(Asesor asesor) {
        if (asesor == null) {
            return 0;
        }
        // Por ahora retorna 0 ya que no tenemos persistencia de visitas
        return 0;
    }
}
