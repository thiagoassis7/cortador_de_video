package com.thiago.videoocutter.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class LegendaService {

    private final String PYTHON = "python";

    private final String SCRIPT =
            "K:\\whisper\\transcrever.py";


    public String gerarLegenda(String caminhoVideo)
            throws IOException, InterruptedException {

        File video = new File(caminhoVideo);

        if (!video.exists()) {
            throw new RuntimeException(
                    "Vídeo não encontrado: " + caminhoVideo
            );
        }

        System.out.println(" Gerando legenda com Whisper...");

        ProcessBuilder pb = new ProcessBuilder(
                PYTHON,
                SCRIPT,
                caminhoVideo
        );

        pb.redirectErrorStream(true);

        Process processo = pb.start();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        processo.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String linha;

            while ((linha = reader.readLine()) != null) {

                System.out.println(
                        "[WHISPER] " + linha
                );
            }
        }

        int exitCode = processo.waitFor();

        if (exitCode != 0) {

            throw new RuntimeException(
                    "Erro ao gerar legenda com Whisper."
            );
        }

        /*
         * O Python vai gerar a legenda na mesma pasta
         * do vídeo.
         */

        File legenda = new File(
                video.getParent(),
                "legenda.srt"
        );

        if (!legenda.exists()) {

            throw new RuntimeException(
                    "Whisper terminou, mas a legenda não foi encontrada."
            );
        }

        System.out.println(
                " Legenda criada: "
                        + legenda.getAbsolutePath()
        );

        return legenda.getAbsolutePath();
    }
}