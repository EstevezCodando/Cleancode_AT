package empresa.logistica.etiqueta;

import empresa.logistica.dominio.Entrega;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class FormatadorEtiquetaTextoSimples implements FormatadorEtiqueta {

    private final NumberFormat formatoMoeda;

    public FormatadorEtiquetaTextoSimples() {
        this.formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    }

    @Override
    public String gerarEtiqueta(Entrega entrega, BigDecimal valorFrete) {
        return "Destinatário: " + entrega.getDestinatario() +
                "\nEndereço: " + entrega.getEndereco() +
                "\nValor do Frete: " + formatoMoeda.format(valorFrete);
    }

    @Override
    public String gerarResumo(Entrega entrega, BigDecimal valorFrete) {
        return "Pedido para " + entrega.getDestinatario() +
                " com frete tipo " + entrega.getCodigoTipoFrete() +
                " no valor de " + formatoMoeda.format(valorFrete);
    }
}
