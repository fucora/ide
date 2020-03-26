package com.teamide.starter.shell.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.teamide.shell.Shell;
import com.teamide.shell.java.JavaShell;
import com.teamide.starter.StarterParam;

public class JavaJarStarterProcess extends JavaStarterProcess {

	public JavaJarStarterProcess(StarterParam param) {
		super(param);
	}

	@Override
	public Shell getShell() {
		JavaShell shell = new JavaShell(param.starterFolder);
		return shell;
	}

	@Override
	public String getStartShell() throws Exception {

		JavaShell shell = (JavaShell) this.shell;
		shell.setJava_home(getJavaHome());
		shell.setJava_envp(getJavaEnvp());

		List<File> lib_folders = new ArrayList<File>();
		lib_folders.add(new File(param.workFolder, "lib"));
		shell.setLib_folders(lib_folders);
		shell.setJar_file(new File(param.workFolder, "app.jar"));

		return shell.getShellString();
	}

	@Override
	public String getStopShell() throws Exception {
		return null;
	}

	public File getJarFile() {

		return null;
	}

	@Override
	public File getPIDFile() throws Exception {
		return shell.getPIDFile();
	}

}