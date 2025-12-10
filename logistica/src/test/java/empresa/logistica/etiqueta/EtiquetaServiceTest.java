package empresa.logistica.etiqueta;

import empresa.logistica.dominio.Entrega;
import empresa.logistica.frete.CalculadoraFreteProvider;
import empresa.logistica.frete.FretePadrao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class EtiquetaServiceTest {

    @Test
    void deveGerarEtiquetaComValorDeFrete() {
        CalculadoraFreteProvider provider = new CalculadoraFreteProvider(
                List.of(new FretePadrao())
        );
        FormatadorEtiqueta formatador = new FormatadorEtiquetaTextoSimples();
        EtiquetaService service = new EtiquetaService(provider, formatador);

        Entrega entrega = new Entrega(
                "Fulano",
                "Rua A, 123",
                BigDecimal.valueOf(10),
                "PAD"
        );

        String etiqueta = service.gerarEtiqueta(entrega);

        Assertions.assertTrue(etiqueta.contains("Fulano"));
        Assertions.assertTrue(etiqueta.contains("Rua A, 123"));
        Assertions.assertTrue(etiqueta.contains("R$"));
    }

    @Test
    void deveIdentificarFreteGratisParaEconomicoLeve() {
        CalculadoraFreteProvider provider = new CalculadoraFreteProvider(
                List.of(new FreteEconomicoDummy())
        );
        FormatadorEtiqueta formatador = new FormatadorEtiquetaTextoSimples();
        EtiquetaService service = new EtiquetaService(provider, formatador);

        Entrega entrega = new Entrega(
                "Ciclano",
                "Rua B, 456",
                BigDecimal.valueOf(1.5),
                "ECO"
        );

        Assertions.assertTrue(service.ehFreteGratis(entrega));
    }

    private static class FreteEconomicoDummy extends empresa.logistica.frete.FreteEconomico {
        // herda comportamento real
    }
}
