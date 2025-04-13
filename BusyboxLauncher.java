import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class BusyboxLauncher {
    private static final String BUSYBOX_PATH = "bin/busybox";
    private static final String X86_64_URL = "https://github.com/d0m-1k/BusyboxLauncher/raw/main/busybox/busybox-x86_64.bin";
    private static final String AARCH64_URL = "https://github.com/d0m-1k/BusyboxLauncher/raw/main/busybox/busybox-aarch64.bin";

    public static void main(String[] args) throws IOException, InterruptedException {
        File busyboxFile = new File(BUSYBOX_PATH);
        
        if (!busyboxFile.exists()) {
            String architecture = getNormalizedArchitecture();
            String downloadUrl = getDownloadUrl(architecture);
            downloadBusybox(downloadUrl, busyboxFile);
            setExecutable(busyboxFile);
        }

        startShell();
    }

    private static String getNormalizedArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64")) return "aarch64";
        if (arch.contains("amd64") || arch.contains("x86_64")) return "x86_64";
        return arch;
    }

    private static String getDownloadUrl(String architecture) throws IOException {
        switch (architecture) {
            case "x86_64": return X86_64_URL;
            case "aarch64": return AARCH64_URL;
            default: throw new IOException("Неподдерживаемая архитектура: " + architecture);
        }
    }

    private static void downloadBusybox(String urlStr, File targetFile) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        System.out.println("Скачивание busybox для " + getNormalizedArchitecture() + "...");
        
        Path targetPath = Paths.get(targetFile.getParent());
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        System.out.println("Файл сохранён: " + targetFile.getAbsolutePath());
    }

    private static void setExecutable(File file) throws IOException {
        if (!file.setExecutable(true)) {
            throw new IOException("Ошибка установки прав на выполнение");
        }
    }

    private static void startShell() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(BUSYBOX_PATH, "sh").start();

        redirectStream(process.getInputStream(), System.out);
        redirectStream(process.getErrorStream(), System.err);

        Thread inputThread = new Thread(() -> {
            try (OutputStream processOut = process.getOutputStream()) {
                int c;
                while ((c = System.in.read()) != -1) {
                    processOut.write(c);
                    processOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputThread.start();

        process.waitFor();
        inputThread.join();
    }

    private static void redirectStream(InputStream in, PrintStream out) {
        new Thread(() -> {
            try {
                int c;
                while ((c = in.read()) != -1) {
                    out.write(c);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
