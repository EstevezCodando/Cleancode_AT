package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FreteEconomico implements CalculadoraFrete {

    private static final BigDecimal FATOR_PESO = BigDecimal.valueOf(1.1);
    private static final BigDecimal DESCONTO_FIXO = BigDecimal.valueOf(5);
    private static final BigDecimal LIMITE_PESO_GRATIS = BigDecimal.valueOf(2);

    @Override
    public String getCodigoTipoFrete() {
        return "ECO";
    }

    @Override
    public BigDecimal calcular(Entrega entrega) {
        BigDecimal peso = aplicarPromocaoPorPeso(entrega.getPesoEmKg());

        if (peso.compareTo(LIMITE_PESO_GRATIS) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal valor = peso.multiply(FATOR_PESO).subtract(DESCONTO_FIXO);
        return valor.max(BigDecimal.ZERO);
    }

    @Override
    public boolean ehFreteGratis(Entrega entrega) {
        BigDecimal peso = aplicarPromocaoPorPeso(entrega.getPesoEmKg());
        return peso.compareTo(LIMITE_PESO_GRATIS) < 0;
    }
}
