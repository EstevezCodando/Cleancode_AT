package empresa.logistica.etiqueta;

import empresa.logistica.dominio.Entrega;
import empresa.logistica.frete.CalculadoraFreteProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class EtiquetaService {

    private final CalculadoraFreteProvider calculadoraFreteProvider;
    private final FormatadorEtiqueta formatadorEtiqueta;

    public EtiquetaService(CalculadoraFreteProvider calculadoraFreteProvider,
                           FormatadorEtiqueta formatadorEtiqueta) {
        this.calculadoraFreteProvider = Objects.requireNonNull(calculadoraFreteProvider);
        this.formatadorEtiqueta = Objects.requireNonNull(formatadorEtiqueta);
    }

    public String gerarEtiqueta(Entrega entrega) {
        BigDecimal valorFrete = calculadoraFreteProvider.calcularFrete(entrega);
        return formatadorEtiqueta.gerarEtiqueta(entrega, valorFrete);
    }

    public String gerarResumoPedido(Entrega entrega) {
        BigDecimal valorFrete = calculadoraFreteProvider.calcularFrete(entrega);
        return formatadorEtiqueta.gerarResumo(entrega, valorFrete);
    }

    public boolean ehFreteGratis(Entrega entrega) {
        return calculadoraFreteProvider.ehFreteGratis(entrega);
    }
}
