package com.uniquindio.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialItem {

    private Inmueble inmueble;
    private String accion;
    @Builder.Default
    private List<String> acciones = new ArrayList<>();
    private String fechaTexto;
}
