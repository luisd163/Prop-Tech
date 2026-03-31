package com.uniquindio.Service;

import org.springframework.stereotype.Service;
import java.util.Objects;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Alerta;
import com.uniquindio.Repositorio.AlertaRepositorio;

@Service
public class AsesorHomeService {
    
    AlertaRepositorio alertaRepositorio = new AlertaRepositorio();

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
}
