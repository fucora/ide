package com.teamide.ide.deployer;

import java.io.File;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.teamide.starter.enums.DeployStatus;
import com.teamide.starter.enums.StarterStatus;

public class LocalDeploy extends Deploy {

	public LocalDeploy(File starterFolder) {
		super(starterFolder);
	}

	public void deploy() throws Exception {
		this.starter.writeDeployStatus(DeployStatus.DEPLOYING);
		install();
		start();
		this.starter.writeDeployStatus(DeployStatus.DEPLOYED);
	}

	public void remove() throws Exception {
		this.starter.remove();
	}

	public void start() throws Exception {
		this.starter.writeStatus(StarterStatus.STARTING);

		installProject();
		this.starter.start();
	}

	public void stop() throws Exception {
		this.starter.writeStatus(StarterStatus.STOPPING);
		this.starter.stop();
	}

	public void cleanLog() {
		this.starter.cleanLog();
	}

	public JSONObject getStatus() {
		return (JSONObject) JSON.toJSON(this.starter.getStarterOption());
	}

	@Override
	public JSONObject read(int start, int end, String timestamp) {
		return this.starter.getLog().read(start, end, timestamp);
	}
}
