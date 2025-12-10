package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;

import java.math.BigDecimal;

public interface CalculadoraFrete {

    String getCodigoTipoFrete();

    BigDecimal calcular(Entrega entrega);

    default boolean ehFreteGratis(Entrega entrega) {
        return calcular(entrega).compareTo(BigDecimal.ZERO) == 0;
    }

    default BigDecimal aplicarPromocaoPorPeso(BigDecimal pesoEmKg) {
        if (pesoEmKg.compareTo(BigDecimal.TEN) > 0) {
            return pesoEmKg.subtract(BigDecimal.ONE);
        }
        return pesoEmKg;
    }
}
