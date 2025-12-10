package empresa.logistica.dominio;

import empresa.logistica.excecao.ValidacaoEntidadeException;

import java.math.BigDecimal;
import java.util.Objects;

public final class Entrega {

    private final String destinatario;
    private final String endereco;
    private final BigDecimal pesoEmKg;
    private final String codigoTipoFrete;

    public Entrega(String destinatario,
                   String endereco,
                   BigDecimal pesoEmKg,
                   String codigoTipoFrete) {

        if (destinatario == null || destinatario.isBlank()) {
            throw new ValidacaoEntidadeException("Destinatário é obrigatório.");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new ValidacaoEntidadeException("Endereço é obrigatório.");
        }
        if (pesoEmKg == null) {
            throw new ValidacaoEntidadeException("Peso é obrigatório.");
        }
        if (pesoEmKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoEntidadeException("Peso deve ser maior que zero.");
        }
        if (codigoTipoFrete == null || codigoTipoFrete.isBlank()) {
            throw new ValidacaoEntidadeException("Tipo de frete é obrigatório.");
        }

        this.destinatario = destinatario.trim();
        this.endereco = endereco.trim();
        this.pesoEmKg = pesoEmKg;
        this.codigoTipoFrete = codigoTipoFrete.trim().toUpperCase();
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getEndereco() {
        return endereco;
    }

    public BigDecimal getPesoEmKg() {
        return pesoEmKg;
    }

    public String getCodigoTipoFrete() {
        return codigoTipoFrete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrega entrega)) return false;
        return Objects.equals(destinatario, entrega.destinatario) &&
                Objects.equals(endereco, entrega.endereco) &&
                Objects.equals(pesoEmKg, entrega.pesoEmKg) &&
                Objects.equals(codigoTipoFrete, entrega.codigoTipoFrete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinatario, endereco, pesoEmKg, codigoTipoFrete);
    }

    @Override
    public String toString() {
        return "Entrega{" +
                "destinatario='" + destinatario + '\'' +
                ", endereco='" + endereco + '\'' +
                ", pesoEmKg=" + pesoEmKg +
                ", codigoTipoFrete='" + codigoTipoFrete + '\'' +
                '}';
    }
}
