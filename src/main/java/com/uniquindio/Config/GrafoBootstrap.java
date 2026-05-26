package com.uniquindio.Config;

import com.uniquindio.Service.GrafoService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GrafoBootstrap {

    @EventListener(ApplicationReadyEvent.class)
    public void inicializarGrafo() {
        GrafoService.getInstancia().cargarDesdePersistencia();
    }
}
