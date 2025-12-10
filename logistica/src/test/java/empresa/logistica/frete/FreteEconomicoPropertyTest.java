package empresa.logistica.frete;

import empresa.logistica.dominio.Entrega;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreteEconomicoPropertyTest {

    private final FreteEconomico freteEconomico = new FreteEconomico();

    @Property
    void freteEconomicoDeveSerGratisQuandoPesoMenorQueDois(
            @ForAll @DoubleRange(min = 0.01, max = 1.99) double peso) {

        Entrega entrega = new Entrega(
                "Fulano",
                "Rua A, 123",
                BigDecimal.valueOf(peso),
                "ECO"
        );

        assertTrue(freteEconomico.ehFreteGratis(entrega));
        assertEquals(0, freteEconomico.calcular(entrega).compareTo(BigDecimal.ZERO));
    }

    @Property
    void freteEconomicoNuncaDeveSerNegativo(
            @ForAll @DoubleRange(min = 0.01, max = 50.0) double peso) {

        Entrega entrega = new Entrega(
                "Fulano",
                "Rua A, 123",
                BigDecimal.valueOf(peso),
                "ECO"
        );

        assertTrue(freteEconomico.calcular(entrega).compareTo(BigDecimal.ZERO) >= 0);
    }
}
