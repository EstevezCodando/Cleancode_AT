package empresa.logistica.excecao;

public class TipoFreteNaoSuportadoException extends RuntimeException {

    private final String codigoTipoFrete;

    public TipoFreteNaoSuportadoException(String codigoTipoFrete) {
        super("Tipo de frete não suportado: " + codigoTipoFrete);
        this.codigoTipoFrete = codigoTipoFrete;
    }

    public String getCodigoTipoFrete() {
        return codigoTipoFrete;
    }
}
