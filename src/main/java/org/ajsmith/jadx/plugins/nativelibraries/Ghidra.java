package org.ajsmith.jadx.plugins.nativelibraries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Ghidra {
	private final String ghidraPath;

	public Ghidra(String ghidraPath) {
		this.ghidraPath = ghidraPath;
	}

	public void checkSuccess(Process process) throws IOException {
		if (process.exitValue() != 0) {
			throw new IOException("Ghidra exited with code " + process.exitValue());
		}

		String stdout = new String(process.getInputStream().readAllBytes());
		String[] lines = stdout.split("\n");
		String lastLines = String.join("\n", Arrays.asList(lines).subList(Math.max(lines.length - 3, 0), lines.length));
		if (lastLines.contains("ERROR REPORT") && !lastLines.contains("conflicting program file in project")) {
			throw new IOException(lastLines);
		}
	}

	public static class Project {
		private final Path path;
		private final String name;

		public Project(Path path, String name) {
			this.path = path;
			this.name = name;
		}

		private String getProjectFile() {
			return path.resolve(name) + ".gpr";
		}

		private String getLockName() {
			return name + ".lock";
		}
	}

	public List<String> getGlobalArgs() {
		return List.of(
				ghidraPath,
				"bg", // mode
				"jdk", // java-type
				"Ghidra", // name
				"", // max-memory
				"" // vmarg-list
		);
	}

	public CompletableFuture<Process> importFile(Project project, String fileName) throws IOException {
		Files.createDirectories(project.path);

		List<String> importArgs = new ArrayList<>(getGlobalArgs());
		importArgs.addAll(List.of(
				"ghidra.app.util.headless.AnalyzeHeadless", // app-classname
				project.path.toString(), // project location
				project.name, // project name
				"-import",
				fileName // file to import
		));


		Process process = new ProcessBuilder(importArgs).start();
		return process.onExit();
	}

	public CompletableFuture<Process> launchGhidra(Project project) throws IOException, ConcurrentModificationException {
		File[] files = new File(project.path.toString()).listFiles();
		if (files != null && Arrays.stream(files).anyMatch(f -> f.getName().equals(project.getLockName()))) {
			throw new ConcurrentModificationException("project is already open in Ghidra");
		}

		List<String> launchArgs = new ArrayList<>(getGlobalArgs());
		launchArgs.addAll(List.of(
				"ghidra.GhidraRun", // app-classname
				project.getProjectFile()
		));

		Process process = new ProcessBuilder(launchArgs).start();
		return process.onExit();
	}
}
