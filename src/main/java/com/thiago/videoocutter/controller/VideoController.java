package com.thiago.videoocutter.controller;

import com.thiago.videoocutter.model.VideoRequest;
import com.thiago.videoocutter.service.LegendaService;
import com.thiago.videoocutter.service.VideoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
public class VideoController {

    private final VideoService videoService;
    private final LegendaService legendaService;


    public VideoController(
            VideoService videoService,
            LegendaService legendaService
    ) {

        this.videoService = videoService;
        this.legendaService = legendaService;
    }


    // ============================================================
    // TESTE DE LEGENDA
    // ============================================================

    @PostMapping("/legenda")
    public String gerarLegenda(
            @RequestParam String caminho
    ) {

        try {

            String legenda =
                    legendaService.gerarLegenda(caminho);

            return "Legenda criada: " + legenda;

        } catch (Exception e) {

            e.printStackTrace();

            return "Erro: " + e.getMessage();
        }
    }


    // ============================================================
    // PROCESSAMENTO NORMAL
    // ============================================================

    @PostMapping("/processar")
    public String processar(
            @RequestBody VideoRequest request
    ) {

        if (
                request.getUrl() == null
                        || request.getUrl().isBlank()
        ) {

            return "URL não informada!";
        }


        // ========================================================
        // SEM DURAÇÃO = APENAS DOWNLOAD
        // ========================================================

        if (
                request.getDuracaoCorte() == null
                        || request.getDuracaoCorte() <= 0
        ) {

            videoService.baixarAsync(
                    request.getUrl()
            );

            return "Download iniciado!";
        }


        // ========================================================
        // FORMATO PADRÃO
        // ========================================================

        String formato =
                request.getFormato();


        if (
                formato == null
                        || formato.isBlank()
        ) {

            formato = "ORIGINAL";
        }


        // ========================================================
        // VALIDA FORMATO
        // ========================================================

        if (
                !formato.equalsIgnoreCase("ORIGINAL")
                        &&
                        !formato.equalsIgnoreCase("TIKTOK")
        ) {

            return "Formato inválido. Use ORIGINAL ou TIKTOK.";
        }


        // ========================================================
        // PROCESSA
        // ========================================================

        videoService.baixarECortarAsync(
                request.getUrl(),
                request.getDuracaoCorte(),
                formato.toUpperCase(),Boolean.TRUE.equals(request.getLegenda())
        );


        return "Download e corte iniciados!";
    }
}