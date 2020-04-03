
package com.teamide.app.generater;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamide.app.AppContext;
import com.teamide.app.generater.jexl.JexlGenerater;
import com.teamide.app.generater.jexl.ScriptGenerater;
import com.teamide.app.plugin.AppBean;
import com.teamide.util.ObjectUtil;
import com.teamide.util.ResourceUtil;
import com.teamide.util.StringUtil;

public abstract class Generater {

	protected final File sourceFolder;

	protected final AppBean app;

	protected final AppContext context;

	public static final String HEAD_NOTE = "TeamIDE File Marker";

	public static final String HEAD_REMARK = "The current file is generated by TeamIDE, please do not modify and move!";

	public final JSONObject data = new JSONObject();

	protected List<String> imports = new ArrayList<String>();

	public Generater(File sourceFolder, AppBean app, AppContext context) {
		this.sourceFolder = sourceFolder;
		this.app = app;
		this.context = context;
	}

	public InputStream loadTemplate(String template) {
		return ResourceUtil.load(this.getClass().getClassLoader(), template);
	}

	public void initBaseData() {

		data.put("$AES_KEY", context.getApp().getAeskey());

		JSONObject $app_factory = new JSONObject();
		$app_factory.put("$package", getFactoryPackage());
		$app_factory.put("$classname", getAppFactoryClassname());
		data.put("$app_factory", $app_factory);

		JSONObject $database_factory = new JSONObject();
		$database_factory.put("$package", getFactoryPackage());
		$database_factory.put("$classname", getDatabaseFactoryClassname());
		data.put("$database_factory", $database_factory);

		JSONObject $IDao = new JSONObject();
		$IDao.put("$package", getIDaoPackage());
		$IDao.put("$classname", getIDaoClassname());
		data.put("$IDao", $IDao);

		JSONObject $IDaoImplDap = new JSONObject();
		$IDaoImplDap.put("$package", getIDaoImplDaoPackage());
		$IDaoImplDap.put("$classname", getIDaoImplDaoClassname());
		data.put("$IDaoImplDap", $IDaoImplDap);

		JSONObject $jexl_processor = new JSONObject();
		$jexl_processor.put("$package", getJexlPackage());
		$jexl_processor.put("$classname", getJexlProcessorClassname());
		data.put("$jexl_processor", $jexl_processor);

		JSONArray $scripts = new JSONArray();
		for (String propertyname : JexlGenerater.SCRIPT_MAP.keySet()) {
			String scriptName = JexlGenerater.SCRIPT_MAP.get(propertyname);
			ScriptGenerater scriptGenerater = new ScriptGenerater(sourceFolder, app, context, scriptName, propertyname);

			JSONObject $one = new JSONObject();
			$one.put("$package", scriptGenerater.getPackage());
			$one.put("$classname", scriptGenerater.getClassName());
			$one.put("$propertyname", scriptGenerater.getPropertyname());
			$scripts.add($one);
		}
		data.put("$scripts", $scripts);

		data.put("$imports", imports);
	}

	public abstract void generate() throws Exception;

	public String toHump(String name) {
		if (StringUtil.isEmpty(name)) {
			return name;
		}
		String[] chars = name.split("");
		String result = "";
		for (int i = 0; i < chars.length; i++) {
			if (StringUtil.isEmpty(chars[i])) {
				continue;
			}
			if (chars[i].equals("/") || chars[i].equals("\\")) {
				continue;
			}
			if (result.length() == 0) {
				result += chars[i];
			} else {
				if (chars[i - 1].equals("/") || chars[i - 1].equals("\\")) {
					result += chars[i].toUpperCase();
				} else {
					result += chars[i];
				}
			}
		}
		return result;
	}

	public String getFolderByName(String name) {
		if (name.indexOf("/") > 0) {
			return name.substring(0, name.lastIndexOf("/"));
		}
		return "";
	}

	public String getFolderPackage(String folder) {
		String pack = "";
		int level = -1;
		if (context.getJava() != null) {
			if (StringUtil.isNotEmpty(context.getJava().getDirectorypackagelevel())) {
				try {
					level = Integer.valueOf(context.getJava().getDirectorypackagelevel());
				} catch (Exception e) {

				}
			}
		}
		if (!StringUtil.isEmpty(folder)) {
			String[] folders = folder.split("/");
			if (level < 0 || level > folders.length) {
				level = folders.length;
			}
			for (int i = 0; i < level; i++) {
				String f = folders[i];
				if (StringUtil.isEmpty(f)) {
					continue;
				}
				if (StringUtil.isNotEmpty(pack)) {
					pack += ".";
				}
				pack += f;
			}
		}
		return pack;
	}

	public File getJavaFolder() {
		String javadirectory = null;
		if (context.getJava() != null) {
			javadirectory = context.getJava().getJavadirectory();
		}
		if (StringUtil.isEmpty(javadirectory)) {
			javadirectory = "src/main/java";
		}

		return new File(sourceFolder, javadirectory);
	}

	public File getResourcesFolder() {
		String resourcesdirectory = null;
		if (context.getJava() != null) {
			resourcesdirectory = context.getJava().getResourcesdirectory();
		}
		if (StringUtil.isEmpty(resourcesdirectory)) {
			resourcesdirectory = "src/main/resources";
		}

		return new File(sourceFolder, resourcesdirectory);
	}

	public boolean isUsemybatis() {
		Boolean usemybatis = null;
		if (context.getJava() != null) {
			usemybatis = context.getJava().getUsemybatis();
		}
		return ObjectUtil.isTrue(usemybatis);
	}

	public boolean isMergedirectory() {
		Boolean mergedirectory = null;
		if (context.getJava() != null) {
			mergedirectory = context.getJava().getMergedirectory();
		}
		return ObjectUtil.isTrue(mergedirectory);
	}

	public String getFactoryPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getFactorypackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".factory";
		}
		return pack;
	}

	public String getJexlPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getJexlpackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".jexl";
		}
		return pack;
	}

	public String getJexlScriptPackage() {
		String pack = getJexlPackage() + ".script";
		return pack;
	}

	public String getIDaoPackage() {
		String pack = getDaoPackage();
		return pack;
	}

	public String getIDaoImplDaoPackage() {
		String pack = getDaoPackage();
		return pack + ".impl";
	}

	public String getControllerPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getControllerpackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".controller";
		}
		return pack;
	}

	public String getDaoPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getDaopackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".dao";
		}
		return pack;
	}

	public String getServicePackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getServicepackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".service";
		}
		return pack;
	}

	public String getDictionaryPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getDictionarypackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".dictionary";
		}
		return pack;
	}

	public String getBeanPackage() {
		String pack = null;
		if (context.getJava() != null) {
			pack = context.getJava().getBeanpackage();
		}
		if (StringUtil.isEmpty(pack)) {
			pack = getBasePackage() + ".bean";
		}
		return pack;
	}

	public String getJexlProcessorClassname() {
		return "JexlProcessor";
	}

	public String getAppFactoryClassname() {
		return "AppFactory";
	}

	public String getDatabaseFactoryClassname() {
		return "DatabaseFactory";
	}

	public String getDaoComponentClassname() {
		return "DaoComponent";
	}

	public String getTransactionComponentClassname() {
		return "TransactionComponent";
	}

	public String getIDaoClassname() {
		return "IDao";
	}

	public String getIDaoImplDaoClassname() {
		return "Dao";
	}

	public String getBasePackage() {
		String basepackage = null;
		if (context.getJava() != null) {
			basepackage = context.getJava().getBasepackage();
		}
		if (StringUtil.isEmpty(basepackage)) {
			basepackage = "com.teamide.app";
		}

		return basepackage;
	}

	public String packageToPath(String pack) {
		if (StringUtil.isEmpty(pack)) {
			return pack;
		}
		return pack.replaceAll("\\.", "/");
	}

}