import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class BusyboxLauncher {

	public static void main(String[] args) {
		try {
			String arch = getNormalizedArch();
			String url = getDownloadUrl(arch);
			
			Path outputPath = Paths.get("bin", "busybox");
			Files.createDirectories(outputPath.getParent());

			downloadFile(url, outputPath);
			
			setExecutable(outputPath.toFile());
			
			runBusybox(outputPath.toString());

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static String getNormalizedArch() {
		String arch = System.getProperty("os.arch").toLowerCase();
		if (arch.contains("aarch64") || arch.contains("arm64")) {
			return "aarch64";
		}
		return "x86_64";
	}

	private static String getDownloadUrl(String arch) {
		return "https://github.com/d0m-1k/BusyboxLauncher/raw/refs/tags/v1.0.0/busybox/busybox-" + arch + ".bin";
	}

	private static void downloadFile(String url, Path outputPath) throws IOException {
		URL downloadUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
		connection.setRequestMethod("GET");

		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Ошибка при скачивании: HTTP " + connection.getResponseCode());
		}

		Files.copy(connection.getInputStream(), outputPath, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("BusyBox успешно скачан в " + outputPath);
	}

	private static void setExecutable(File file) throws SecurityException {
		if (!file.setExecutable(true)) {
			throw new SecurityException("Не удалось установить права на выполнение");
		}
		System.out.println("Права на выполнение установлены");
	}

	private static void runBusybox(String path) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(path, "sh")
			.inheritIO()
			.directory(new File(System.getProperty("user.dir")));
		
		System.out.println("Запускаем BusyBox...");
		Process process = pb.start();
		int exitCode = process.waitFor();
		System.out.println("BusyBox завершил работу с кодом: " + exitCode);
	}
}
