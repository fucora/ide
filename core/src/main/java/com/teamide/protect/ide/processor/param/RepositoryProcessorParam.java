package com.teamide.protect.ide.processor.param;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import com.alibaba.fastjson.JSONObject;
import com.teamide.util.IOUtil;
import com.teamide.util.StringUtil;
import com.teamide.ide.bean.SpaceRepositoryOptionBean;
import com.teamide.ide.client.Client;
import com.teamide.protect.ide.enums.OptionType;
import com.teamide.protect.ide.processor.repository.RepositoryLog;
import com.teamide.protect.ide.service.SpaceRepositoryOptionService;

public class RepositoryProcessorParam extends SpaceProcessorParam {

	private final String branch;

	private final File branchFolder;

	private final File branchsFolder;

	private final File sourceFolder;

	private final File pomFile;

	public static final String DEFAULT_BRANCH = "master";

	public RepositoryProcessorParam(Client client, String spaceid, String branch) {
		super(client, spaceid);

		if (StringUtil.isEmpty(branch)) {
			branch = DEFAULT_BRANCH;
		}
		this.branch = branch;

		this.branchsFolder = new File(this.getSpaceFolder(), "branchs");
		this.branchFolder = new File(this.branchsFolder, branch);
		this.sourceFolder = new File(this.branchFolder, "source");
		this.pomFile = new File(this.sourceFolder, "pom.xml");
	}

	public String getMavenHome(String path) {
		return null;
	}

	public RepositoryLog getLog() {

		return RepositoryLog.get(getLogName(), getLogFolder());
	}

	public RepositoryLog getRunnerLog(String token) {

		return RepositoryLog.get(getRunnerLogName(token), getRunnerLogFolder());
	}

	public String getRunnerLogName(String token) {
		if (StringUtil.isEmpty(token)) {
			throw new RuntimeException("token is null.");
		}

		return getSpace().getName() + "." + token;
	}

	public String getLogName() {

		return getSpace().getName();
	}

	public File getRunnerLogFolder() {

		return new File(getLogFolder(), "runner");
	}

	public File getLogFolder() {

		return new File(getBranchFolder(), "log");
	}

	public File getRunnerFolder() {

		return new File(getBranchFolder(), "runner");

	}

	public File getTokenRunnerFolder(String token) {
		File folder = getRunnerFolder();
		if (!StringUtil.isEmpty(token)) {
			folder = new File(folder, token);
		}
		return folder;
	}

	public Properties loadProperties(String path) {

		return loadProperties(new File(path));
	}

	public Properties loadProperties(File file) {

		Properties properties = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			properties.load(stream);
		} catch (Exception e) {
		} finally {
			IOUtil.close(stream);
		}
		return properties;
	}

	public void saveProperties(String path, Properties properties) {
		File file = new File(path);
		saveProperties(file, properties);
	}

	public void saveProperties(File file, Properties properties) {

		FileOutputStream stream = null;
		try {
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			stream = new FileOutputStream(file);
			properties.store(stream, null);
		} catch (Exception e) {
		} finally {
			IOUtil.close(stream);
		}
	}

	public void deleteOption(String path, String name, OptionType type) throws Exception {

		SpaceRepositoryOptionService service = new SpaceRepositoryOptionService();
		List<SpaceRepositoryOptionBean> options = service.query(getClient(), getSpace(), branch, path, name, type);
		for (SpaceRepositoryOptionBean option : options) {
			service.delete(option.getId());
		}
	}

	public List<SpaceRepositoryOptionBean> getOptions(String path, OptionType type) throws Exception {

		List<SpaceRepositoryOptionBean> options = queryOptions(path, null, type);
		return options;
	}

	public JSONObject getAppOption(String path) throws Exception {

		List<SpaceRepositoryOptionBean> options = queryOptions(path, null, OptionType.APP);
		if (options.size() > 0) {
			return options.get(0).getJSONOption();
		}
		return null;
	}

	public JSONObject getOption(String path, String name, OptionType type) throws Exception {

		List<SpaceRepositoryOptionBean> options = queryOptions(path, name, type);
		if (options.size() > 0) {
			return options.get(0).getJSONOption();
		}
		return null;
	}

	public List<SpaceRepositoryOptionBean> queryOptions(String path, String name, OptionType type) throws Exception {
		SpaceRepositoryOptionService service = new SpaceRepositoryOptionService();
		return service.query(getClient(), getSpace(), branch, path, name, type);
	}

	public JSONObject saveOption(String path, String name, OptionType type, JSONObject json) throws Exception {
		if (json == null) {
			json = new JSONObject();
		}

		SpaceRepositoryOptionService service = new SpaceRepositoryOptionService();

		List<SpaceRepositoryOptionBean> options = queryOptions(path, name, type);
		SpaceRepositoryOptionBean option = null;
		if (options.size() > 0) {
			option = options.get(0);
		}
		if (option == null) {
			option = new SpaceRepositoryOptionBean();
		}
		if (getClient() != null && getClient().getUser() != null) {
			option.setUserid(getClient().getUser().getId());
		}
		option.setName(name);
		option.setPath(path);
		option.setJSONOption(json);
		option.setType(type.name());
		option.setSpaceid(getSpaceid());
		option.setBranch(branch);

		if (StringUtil.isEmpty(option.getId())) {
			service.insert(getClient(), option);
		} else {
			service.update(getClient(), option);
		}
		return json;
	}

	public String getBranch() {
		return branch;
	}

	public File getBranchFolder() {
		return branchFolder;
	}

	public File getBranchsFolder() {
		return branchsFolder;
	}

	public File getSourceFolder() {
		return sourceFolder;
	}

	public File getPomFile() {
		return pomFile;
	}

	public String getPath(File file) {
		if (file == null) {
			return null;
		}

		String filePath = file.toURI().getPath();

		String sourcePath = getSourceFolder().toURI().getPath();

		String path = filePath.substring(sourcePath.length());
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;

	}

	public File getFile(String path) {
		if (StringUtil.isEmpty(path) || path.equals("/")) {
			return getSourceFolder();
		}
		return new File(getSourceFolder(), path);

	}

	public File getProjectFolder(String projectPath) {

		if (StringUtil.isEmpty(projectPath) || projectPath.equals("/")) {
			return getSourceFolder();
		}
		return new File(getSourceFolder(), projectPath);
	}

	public File getTargetFolder(String projectPath) {
		return new File(getProjectFolder(projectPath), "target");
	}

	public File getJavaFolder(String projectPath) {
		return new File(getProjectFolder(projectPath), "src/main/java/");
	}

	public File getResourcesFolder(String projectPath) {
		return new File(getProjectFolder(projectPath), "src/main/resources/");

	}

	public File getWebappFolder(String projectPath) {
		return new File(getProjectFolder(projectPath), "src/main/webapp/");
	}

	public boolean isAppJava(String projectPath, String path) {
		File file = getFile(path);
		File folder = getJavaFolder(projectPath);
		return file.getAbsolutePath().indexOf(folder.getAbsolutePath()) == 0;
	}

	public boolean isAppWebapp(String projectPath, String path) {
		File file = getFile(path);
		File folder = getWebappFolder(projectPath);
		return file.getAbsolutePath().indexOf(folder.getAbsolutePath()) == 0;
	}

	public boolean isAppResources(String projectPath, String path) {
		File file = getFile(path);
		File folder = getResourcesFolder(projectPath);
		return file.getAbsolutePath().indexOf(folder.getAbsolutePath()) == 0;
	}
}
