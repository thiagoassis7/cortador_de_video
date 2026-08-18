package com.thiago.videoocutter.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class CorteService {

    private final String FFPROBE =
            "K:\\hd_backup_anterior\\dowload\\ffmpeg\\ffmpeg-7.1.1-essentials_build\\bin\\ffprobe.exe";

    private final String FFMPEG =
            "K:\\hd_backup_anterior\\dowload\\ffmpeg\\ffmpeg-7.1.1-essentials_build\\bin\\ffmpeg.exe";


    // ============================================================
    // NORMALIZAR TÍTULO
    // ============================================================

    private String normalizarTitulo(String titulo) {

        String texto =
                Normalizer.normalize(
                                titulo,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{InCombiningDiacriticalMarks}+",
                                ""
                        );

        texto =
                texto.replaceAll(
                        "[\\\\/:*?\"<>|]",
                        ""
                );

        texto =
                texto.trim()
                        .replaceAll(" +", " ");

        return texto;
    }


    // ============================================================
    // PREPARAR TIKTOK / SHORTS
    // ============================================================

    public String prepararTikTok(
            String caminhoVideo
    ) throws IOException, InterruptedException {

        File video =
                new File(caminhoVideo);


        if (!video.exists()) {

            throw new RuntimeException(
                    "Vídeo não encontrado: "
                            + caminhoVideo
            );
        }


        String caminhoTemporario =
                caminhoVideo.replace(
                        ".mp4",
                        "_tiktok_temp.mp4"
                );


        File arquivoTemp =
                new File(caminhoTemporario);


        if (arquivoTemp.exists()) {

            arquivoTemp.delete();
        }


        System.out.println(
                " Convertendo para 1080x1920..."
        );


        ProcessBuilder pb =
                new ProcessBuilder(

                        FFMPEG,

                        "-y",

                        "-i",
                        caminhoVideo,

                        // ========================================
                        // CROP PARA 9:16
                        // ========================================
                        //
                        // aumenta o vídeo mantendo a proporção
                        // e depois corta o excesso.
                        //
                        "-vf",
                        "scale=1080:1920:force_original_aspect_ratio=increase,"
                                + "crop=1080:1920",

                        "-c:v",
                        "libx264",

                        "-preset",
                        "ultrafast",

                        "-c:a",
                        "aac",

                        "-b:a",
                        "128k",

                        "-movflags",
                        "+faststart",

                        caminhoTemporario
                );


        pb.redirectErrorStream(true);


        Process processo =
                pb.start();


        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        processo.getInputStream()
                                )
                        )
        ) {

            String linha;

            while (
                    (linha = reader.readLine())
                            != null
            ) {

                System.out.println(
                        "[TIKTOK] "
                                + linha
                );
            }
        }


        int exitCode =
                processo.waitFor();


        if (exitCode != 0) {

            if (arquivoTemp.exists()) {
                arquivoTemp.delete();
            }

            throw new RuntimeException(
                    "Erro ao preparar vídeo para TikTok."
            );
        }


        if (!arquivoTemp.exists()
                || arquivoTemp.length() < 1000) {

            throw new RuntimeException(
                    "Vídeo TikTok não foi criado corretamente."
            );
        }


        // ========================================================
        // REMOVE ORIGINAL
        // ========================================================

        if (!video.delete()) {

            arquivoTemp.delete();

            throw new RuntimeException(
                    "Não foi possível remover "
                            + "o vídeo original."
            );
        }


        // ========================================================
        // RENOMEIA
        // ========================================================

        if (!arquivoTemp.renameTo(video)) {

            throw new RuntimeException(
                    "Não foi possível renomear "
                            + "o vídeo TikTok."
            );
        }


        System.out.println(
                " Vídeo convertido para 1080x1920."
        );


        return caminhoVideo;
    }


    // ============================================================
    // APLICAR LEGENDA
    // ============================================================

    public String aplicarLegenda(
            String caminhoVideo,
            String caminhoLegenda
    ) throws IOException, InterruptedException {

        File video =
                new File(caminhoVideo);


        File legenda =
                new File(caminhoLegenda);


        if (!video.exists()) {

            throw new RuntimeException(
                    "Vídeo não encontrado: "
                            + caminhoVideo
            );
        }


        if (!legenda.exists()) {

            throw new RuntimeException(
                    "Legenda não encontrada: "
                            + caminhoLegenda
            );
        }


        String caminhoTemporario =
                caminhoVideo.replace(
                        ".mp4",
                        "_legenda_temp.mp4"
                );


        File arquivoTemp =
                new File(caminhoTemporario);


        if (arquivoTemp.exists()) {

            arquivoTemp.delete();
        }


        // ========================================================
        // PREPARA CAMINHO DA LEGENDA PARA FFMPEG
        // ========================================================

        String caminhoLegendaFFmpeg =
                legenda
                        .getAbsolutePath()
                        .replace("\\", "/")
                        .replace(":", "\\:")
                        .replace("'", "\\'");


        String filtro =
                "subtitles='"
                        + caminhoLegendaFFmpeg
                        + "'";


        System.out.println(
                " Aplicando legenda..."
        );


        System.out.println(
                " "
                        + legenda.getAbsolutePath()
        );


        // ========================================================
        // FFMPEG
        // ========================================================

        ProcessBuilder pb =
                new ProcessBuilder(

                        FFMPEG,

                        "-y",

                        "-i",
                        caminhoVideo,

                        "-vf",
                        filtro,

                        "-c:v",
                        "libx264",

                        "-preset",
                        "ultrafast",

                        "-c:a",
                        "aac",

                        "-b:a",
                        "128k",

                        "-movflags",
                        "+faststart",

                        caminhoTemporario
                );


        pb.redirectErrorStream(true);


        Process processo =
                pb.start();


        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        processo.getInputStream()
                                )
                        )
        ) {

            String linha;

            while (
                    (linha = reader.readLine())
                            != null
            ) {

                System.out.println(
                        "[LEGENDA] "
                                + linha
                );
            }
        }


        int exitCode =
                processo.waitFor();


        if (exitCode != 0) {

            if (arquivoTemp.exists()) {
                arquivoTemp.delete();
            }

            throw new RuntimeException(
                    "Erro ao aplicar legenda no vídeo."
            );
        }


        if (!arquivoTemp.exists()
                || arquivoTemp.length() < 1000) {

            throw new RuntimeException(
                    "FFmpeg não gerou corretamente "
                            + "o vídeo com legenda."
            );
        }


        // ========================================================
        // REMOVE ORIGINAL
        // ========================================================

        if (!video.delete()) {

            arquivoTemp.delete();

            throw new RuntimeException(
                    "Não foi possível remover "
                            + "o vídeo original."
            );
        }


        // ========================================================
        // RENOMEIA
        // ========================================================

        if (!arquivoTemp.renameTo(video)) {

            throw new RuntimeException(
                    "Erro ao renomear vídeo "
                            + "com legenda."
            );
        }


        System.out.println(
                " Legenda aplicada com sucesso!"
        );


        return caminhoVideo;
    }


    // ============================================================
    // CORTAR VÍDEO
    // ============================================================

    public List<String> cortarVideo(
            String caminhoVideo,
            String titulo,
            int duracaoCorte
    ) throws IOException, InterruptedException {

        List<String> cortes =
                new ArrayList<>();


        File video =
                new File(caminhoVideo);


        if (!video.exists()) {

            throw new RuntimeException(
                    "Vídeo não encontrado: "
                            + caminhoVideo
            );
        }


        if (duracaoCorte <= 0) {

            throw new RuntimeException(
                    "A duração do corte deve "
                            + "ser maior que zero."
            );
        }


        titulo =
                normalizarTitulo(titulo);


        // ========================================================
        // PASTA DOS CORTES
        // ========================================================

        String pastaCortes =
                video.getParent()
                        + "\\cortes\\";


        File pasta =
                new File(pastaCortes);


        if (!pasta.exists()) {

            if (!pasta.mkdirs()) {

                throw new RuntimeException(
                        "Não foi possível criar "
                                + "a pasta de cortes."
                );
            }
        }


        // ========================================================
        // DURAÇÃO
        // ========================================================

        double duracaoTotal =
                obterDuracaoVideo(
                        caminhoVideo
                );


        int partes =
                (int) Math.ceil(
                        duracaoTotal
                                / duracaoCorte
                );


        System.out.println(
                "\n Duração total: "
                        + duracaoTotal
                        + " segundos"
        );


        System.out.println(
                " Total de partes: "
                        + partes
        );


        // ========================================================
        // CORTES
        // ========================================================

        for (
                int i = 0;
                i < partes;
                i++
        ) {

            int inicioSegundos =
                    i * duracaoCorte;


            double restante =
                    duracaoTotal
                            - inicioSegundos;


            double tempoCorte =
                    Math.min(
                            duracaoCorte,
                            restante
                    );


            if (tempoCorte <= 0) {
                continue;
            }


            String nomeCorte =
                    pastaCortes
                            + titulo
                            + "_parte_"
                            + (i + 1)
                            + ".mp4";


            File arquivoCorte =
                    new File(nomeCorte);


            if (arquivoCorte.exists()) {

                System.out.println(
                        " Removendo corte antigo..."
                );

                arquivoCorte.delete();
            }


            System.out.println(
                    "\n=============================="
            );


            System.out.println(
                    " GERANDO CORTE "
                            + (i + 1)
            );


            System.out.println(
                    " Início: "
                            + inicioSegundos
            );


            System.out.println(
                    "Duração: "
                            + tempoCorte
            );


            System.out.println(
                    "=============================="
            );


            // ====================================================
            // FFMPEG
            // ====================================================

            ProcessBuilder pb =
                    new ProcessBuilder(

                            FFMPEG,

                            "-y",

                            "-ss",
                            String.valueOf(
                                    inicioSegundos
                            ),

                            "-i",
                            caminhoVideo,

                            "-t",
                            String.valueOf(
                                    tempoCorte
                            ),

                            "-c:v",
                            "libx264",

                            "-preset",
                            "ultrafast",

                            "-c:a",
                            "aac",

                            "-b:a",
                            "128k",

                            "-movflags",
                            "+faststart",

                            nomeCorte
                    );


            pb.redirectErrorStream(true);


            Process processo =
                    pb.start();


            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            processo.getInputStream()
                                    )
                            )
            ) {

                String linha;

                while (
                        (linha = reader.readLine())
                                != null
                ) {

                    System.out.println(
                            "[CORTE] "
                                    + linha
                    );
                }
            }


            int exitCode =
                    processo.waitFor();


            if (exitCode != 0) {

                throw new RuntimeException(
                        "Erro ao gerar corte "
                                + (i + 1)
                );
            }


            if (!arquivoCorte.exists()) {

                throw new RuntimeException(
                        "FFmpeg não gerou: "
                                + nomeCorte
                );
            }


            if (arquivoCorte.length() < 1000) {

                arquivoCorte.delete();

                throw new RuntimeException(
                        "Arquivo corrompido: "
                                + nomeCorte
                );
            }


            System.out.println(
                    " Corte criado: "
                            + arquivoCorte.getName()
            );


            cortes.add(
                    nomeCorte
            );
        }


        System.out.println(
                "\n Todos os cortes foram criados!"
        );


        return cortes;
    }


    // ============================================================
    // OBTER DURAÇÃO
    // ============================================================

    private double obterDuracaoVideo(
            String caminhoVideo
    ) throws IOException, InterruptedException {

        ProcessBuilder pb =
                new ProcessBuilder(

                        FFPROBE,

                        "-v",
                        "error",

                        "-show_entries",
                        "format=duration",

                        "-of",
                        "default=noprint_wrappers=1:nokey=1",

                        caminhoVideo
                );


        Process processo =
                pb.start();


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                processo.getInputStream()
                        )
                );


        String duracao =
                reader.readLine();


        processo.waitFor();


        if (
                duracao == null
                        || duracao.isBlank()
        ) {

            throw new RuntimeException(
                    "Não foi possível obter "
                            + "a duração do vídeo."
            );
        }


        return Double.parseDouble(
                duracao
        );
    }
}