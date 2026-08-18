package com.thiago.videoocutter.model;

public class VideoRequest {

    private String url;
    private Integer duracaoCorte;
    private String formato;
    private Boolean legenda;


    public VideoRequest() {}


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public Integer getDuracaoCorte() {
        return duracaoCorte;
    }

    public void setDuracaoCorte(Integer duracaoCorte) {
        this.duracaoCorte = duracaoCorte;
    }


    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }


    public Boolean getLegenda() {
        return legenda;
    }

    public void setLegenda(Boolean legenda) {
        this.legenda = legenda;
    }
}