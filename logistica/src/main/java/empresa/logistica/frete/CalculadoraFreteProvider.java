package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;
import empresa.logistica.excecao.TipoFreteNaoSuportadoException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CalculadoraFreteProvider {

    private final Map<String, CalculadoraFrete> calculadorasPorCodigo;

    public CalculadoraFreteProvider(Collection<CalculadoraFrete> calculadoras) {
        Objects.requireNonNull(calculadoras, "Coleção de calculadoras não pode ser nula.");
        this.calculadorasPorCodigo = calculadoras.stream()
                .collect(Collectors.toUnmodifiableMap(
                        CalculadoraFrete::getCodigoTipoFrete,
                        c -> c
                ));
    }

    public CalculadoraFrete obterPorCodigo(String codigoTipoFrete) {
        CalculadoraFrete calculadora = calculadorasPorCodigo.get(codigoTipoFrete);
        if (calculadora == null) {
            throw new TipoFreteNaoSuportadoException(codigoTipoFrete);
        }
        return calculadora;
    }

    public BigDecimal calcularFrete(Entrega entrega) {
        CalculadoraFrete calculadora = obterPorCodigo(entrega.getCodigoTipoFrete());
        return calculadora.calcular(entrega);
    }

    public boolean ehFreteGratis(Entrega entrega) {
        CalculadoraFrete calculadora = obterPorCodigo(entrega.getCodigoTipoFrete());
        return calculadora.ehFreteGratis(entrega);
    }
}
