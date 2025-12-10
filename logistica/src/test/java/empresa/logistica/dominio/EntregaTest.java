package empresa.logistica.dominio;

import empresa.logistica.excecao.ValidacaoEntidadeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class EntregaTest {

    @Test
    void deveCriarEntregaValida() {
        Entrega entrega = new Entrega(
                "Fulano",
                "Rua A, 123",
                BigDecimal.valueOf(5.0),
                "EXP"
        );

        Assertions.assertEquals("Fulano", entrega.getDestinatario());
        Assertions.assertEquals("Rua A, 123", entrega.getEndereco());
        Assertions.assertEquals(BigDecimal.valueOf(5.0), entrega.getPesoEmKg());
        Assertions.assertEquals("EXP", entrega.getCodigoTipoFrete());
    }

    @Test
    void deveLancarExcecaoQuandoPesoInvalido() {
        Assertions.assertThrows(ValidacaoEntidadeException.class, () ->
                new Entrega("Fulano", "Rua A, 123", BigDecimal.ZERO, "EXP")
        );
    }

    @Test
    void deveLancarExcecaoQuandoDestinatarioVazio() {
        Assertions.assertThrows(ValidacaoEntidadeException.class, () ->
                new Entrega(" ", "Rua A, 123", BigDecimal.valueOf(5), "EXP")
        );
    }
}
