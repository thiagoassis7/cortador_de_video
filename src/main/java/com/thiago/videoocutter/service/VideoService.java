package com.thiago.videoocutter.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService {

    private final DownloadService downloadService;
    private final CorteService corteService;
    private final LegendaService legendaService;


    public VideoService(
            DownloadService downloadService,
            CorteService corteService,
            LegendaService legendaService
    ) {

        this.downloadService = downloadService;
        this.corteService = corteService;
        this.legendaService = legendaService;
    }


    // ============================================================
    // APENAS DOWNLOAD
    // ============================================================

    @Async
    public void baixarAsync(String url) {

        try {

            downloadService.baixar(url);

            System.out.println(
                    "Download concluído!"
            );

        } catch (Exception e) {

            System.err.println(
                    "Erro no download: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // DOWNLOAD + LEGENDA + CORTE
    // ============================================================

    @Async
    public void baixarECortarAsync(
            String url,
            int duracaoCorte,
            String formato,
            boolean legenda
    ) {

        try {

            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "INICIANDO PROCESSAMENTO"
            );

            System.out.println(
                    "Formato: " + formato
            );

            System.out.println(
                    "Legenda: " + legenda
            );

            System.out.println(
                    "=================================\n"
            );


            // ====================================================
            // PLAYLIST
            // ====================================================

            if (url.contains("list=")) {

                downloadService.baixarPlaylist(url);

                System.out.println(
                        "Playlist baixada."
                );

                System.out.println(
                        "Processamento de playlist ainda não aplicado."
                );

                return;
            }


            // ====================================================
            // TÍTULO
            // ====================================================

            String titulo =
                    downloadService.obterTitulo(url);


            // ====================================================
            // DOWNLOAD
            // ====================================================

            System.out.println(
                    " Baixando vídeo..."
            );

            String caminhoVideo =
                    downloadService.baixarVideo(
                            url,
                            titulo
                    );


            System.out.println(
                    " Download concluído:"
            );

            System.out.println(
                    caminhoVideo
            );


            // ====================================================
            // FORMATO
            // ====================================================

            if (formato == null ||
                    formato.isBlank()) {

                formato = "ORIGINAL";
            }

            formato =
                    formato.toUpperCase();


            if (!formato.equals("ORIGINAL")
                    && !formato.equals("TIKTOK")) {

                throw new RuntimeException(
                        "Formato inválido: "
                                + formato
                );
            }


            // ====================================================
            // PREPARA VÍDEO
            // ====================================================
            //
            // ORIGINAL:
            // mantém o vídeo como está.
            //
            // TIKTOK:
            // transforma o vídeo completo em 1080x1920
            // usando crop, sem esticar e sem barras.
            //
            // ====================================================

            String videoProcessado =
                    caminhoVideo;


            if (formato.equals("TIKTOK")) {

                System.out.println(
                        "\n Preparando formato TikTok/Shorts..."
                );

                videoProcessado =
                        corteService.prepararTikTok(
                                caminhoVideo
                        );

                System.out.println(
                        " Formato TikTok preparado."
                );
            }


            // ====================================================
            // LEGENDA
            // ====================================================

            if (legenda) {

                System.out.println(
                        "\n Gerando legenda com Whisper..."
                );


                String caminhoLegenda =
                        legendaService.gerarLegenda(
                                videoProcessado
                        );


                System.out.println(
                        " Legenda gerada:"
                );

                System.out.println(
                        caminhoLegenda
                );


                // ------------------------------------------------
                // QUEIMA A LEGENDA NO VÍDEO COMPLETO
                // ------------------------------------------------

                System.out.println(
                        "\n Aplicando legenda no vídeo completo..."
                );


                videoProcessado =
                        corteService.aplicarLegenda(
                                videoProcessado,
                                caminhoLegenda
                        );


                System.out.println(
                        " Legenda aplicada no vídeo completo."
                );
            }


            // ====================================================
            // CORTES
            // ====================================================

            System.out.println(
                    "\n Iniciando cortes..."
            );


            List<String> cortes =
                    corteService.cortarVideo(
                            videoProcessado,
                            titulo,
                            duracaoCorte
                    );


            // ====================================================
            // RESULTADO
            // ====================================================

            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "PROCESSO CONCLUÍDO!"
            );

            System.out.println(
                    "Total de cortes: "
                            + cortes.size()
            );

            System.out.println(
                    "================================="
            );


            for (String corte : cortes) {

                System.out.println(
                        " O Programa esta Pronto para iniciar um novo processo ! "

                );
            }


        } catch (Exception e) {

            System.err.println(
                    "\n ERRO NO PROCESSO:"
            );

            System.err.println(
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }
}