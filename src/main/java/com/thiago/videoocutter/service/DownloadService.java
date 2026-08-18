package com.thiago.videoocutter.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class DownloadService {

    private final String YTDLP = "K:\\hd_backup_anterior\\dowload\\ffmpeg\\ffmpeg-7.1.1-essentials_build\\bin\\yt-dlp.exe";
    private final String PASTA_VIDEOS = "K:";

    //  Decide automaticamente: vídeo ou playlist
    public String baixar(String url) throws Exception {

        if (url.contains("list=")) {
            baixarPlaylist(url);
            return "Playlist processada com sucesso!";
        } else {
            String titulo = obterTitulo(url);
            return baixarVideo(url, titulo);
        }
    }

    //  Obtém título do vídeo
    public String obterTitulo(String url) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                YTDLP,
                "--encoding", "utf-8",
                "--get-title",
                url
        );

        Process p = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
        );

        String titulo = reader.readLine();
        p.waitFor();

        if (titulo == null || titulo.isEmpty()) {
            titulo = "video";
        }

        return titulo.replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }

    //  Baixar vídeo único com proteção contra travamento
    public String baixarVideo(String url, String titulo)
            throws IOException, InterruptedException {

        File pasta = new File(PASTA_VIDEOS);
        if (!pasta.exists()) pasta.mkdirs();

        String nomeArquivo = titulo.substring(0, Math.min(titulo.length(), 80));
        String caminhoFinal = PASTA_VIDEOS + nomeArquivo + "_" +  ".mp4";

        ProcessBuilder pb = new ProcessBuilder(
                YTDLP,
                "--cookies", "C:\\Users\\thiago\\Downloads\\cookies.txt",
                "--ignore-errors",
                "--no-abort-on-error",
                "--socket-timeout", "10",
                "--retries", "2",
                "--sleep-interval", "2",
                "--max-sleep-interval", "5",
                "--extractor-args", "youtube:player_client=android",
                "-f", "18",
                "-o", caminhoFinal,
                url
        );

        Process p = pb.start();

        //  Captura saída normal
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {

                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println("[yt-dlp] " + linha);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //  CAPTURA ERROS (isso aqui que faltava)
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()))) {

                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println(" [ERRO yt-dlp] " + linha);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        boolean terminou = p.waitFor(2, TimeUnit.MINUTES);

        if (!terminou) {
            p.destroy();
            throw new RuntimeException("Timeout ao baixar vídeo: " + url);
        }

        if (p.exitValue() != 0) {
            throw new RuntimeException("Erro ao baixar vídeo: " + url);
        }

        return caminhoFinal;
    }

    //  NOVO MODO PROFISSIONAL (playlist controlada)
    public void baixarPlaylist(String url) throws IOException, InterruptedException {

        File pasta = new File(PASTA_VIDEOS);
        if (!pasta.exists()) pasta.mkdirs();

        System.out.println(" Buscando vídeos da playlist...");

        ProcessBuilder pbLista = new ProcessBuilder(
                YTDLP,
                "--flat-playlist",
                "--print", "url",
                url
        );

        Process pLista = pbLista.start();

        BufferedReader readerLista = new BufferedReader(
                new InputStreamReader(pLista.getInputStream())
        );

        String videoUrl;
        int total = 0;
        int sucesso = 0;
        int erro = 0;

        //  lista de falhas
        java.util.List<String> falhas = new java.util.ArrayList<>();

        while ((videoUrl = readerLista.readLine()) != null) {

            total++;
            System.out.println("\n Baixando vídeo " + total + ": " + videoUrl);

            boolean baixou = false;

            //  tenta até 3 vezes
            for (int tentativa = 1; tentativa <= 3; tentativa++) {

                try {
                    System.out.println(" Tentativa " + tentativa);

                    String titulo = obterTitulo(videoUrl);
                    baixarVideo(videoUrl, titulo);

                    sucesso++;
                    baixou = true;
                    break;

                } catch (Exception e) {
                    System.out.println(" Falhou tentativa " + tentativa);
                }
            }

            if (!baixou) {
                erro++;
                falhas.add(videoUrl);
                System.out.println(" Falhou definitivamente, será tentado depois...");
            }
        }

        pLista.waitFor();

        //  SEGUNDA TENTATIVA GLOBAL
        if (!falhas.isEmpty()) {

            System.out.println("\n NOVA TENTATIVA PARA FALHAS...");

            java.util.List<String> aindaFalharam = new java.util.ArrayList<>();

            for (String video : falhas) {

                try {
                    String titulo = obterTitulo(video);
                    baixarVideo(video, titulo);
                    sucesso++;

                } catch (Exception e) {
                    aindaFalharam.add(video);
                    System.out.println(" Ainda falhou: " + video);
                }
            }

            System.out.println("\n VÍDEOS QUE NÃO BAIXARAM:");
            for (String v : aindaFalharam) {
                System.out.println(v);
            }
        }

        System.out.println("\n=======================");
        System.out.println(" RESULTADO FINAL");
        System.out.println("Total: " + total);
        System.out.println("Sucesso: " + sucesso);
        System.out.println("Erros: " + erro);
        System.out.println("=======================");
    }}