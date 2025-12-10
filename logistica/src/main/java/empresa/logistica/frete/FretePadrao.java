package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FretePadrao implements CalculadoraFrete {

    private static final BigDecimal FATOR_PESO = BigDecimal.valueOf(1.2);

    @Override
    public String getCodigoTipoFrete() {
        return "PAD";
    }

    @Override
    public BigDecimal calcular(Entrega entrega) {
        BigDecimal pesoConsiderado = aplicarPromocaoPorPeso(entrega.getPesoEmKg());
        BigDecimal valor = pesoConsiderado.multiply(FATOR_PESO);
        return valor.max(BigDecimal.ZERO);
    }
}
