package empresa.logistica.etiqueta;

import empresa.logistica.dominio.Entrega;

import java.math.BigDecimal;

public interface FormatadorEtiqueta {

    String gerarEtiqueta(Entrega entrega, BigDecimal valorFrete);

    String gerarResumo(Entrega entrega, BigDecimal valorFrete);
}
