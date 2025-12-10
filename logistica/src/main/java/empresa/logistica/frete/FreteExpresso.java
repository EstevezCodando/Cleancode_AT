package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FreteExpresso implements CalculadoraFrete {

    private static final BigDecimal FATOR_PESO = BigDecimal.valueOf(1.5);
    private static final BigDecimal TAXA_FIXA = BigDecimal.TEN;

    @Override
    public String getCodigoTipoFrete() {
        return "EXP";
    }

    @Override
    public BigDecimal calcular(Entrega entrega) {
        BigDecimal pesoConsiderado = aplicarPromocaoPorPeso(entrega.getPesoEmKg());
        BigDecimal valor = pesoConsiderado.multiply(FATOR_PESO).add(TAXA_FIXA);
        return valor.max(BigDecimal.ZERO);
    }
}
