package com.teamide.ide.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.teamide.util.ObjectUtil;
import com.teamide.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.teamide.client.ClientSession;
import com.teamide.http.HttpRequest;
import com.teamide.http.HttpResponse;
import com.teamide.ide.bean.RoleBean;
import com.teamide.ide.bean.UserBean;
import com.teamide.ide.bean.UserLoginBean;
import com.teamide.ide.configure.IDEConfigure;
import com.teamide.ide.enums.UserActiveStatus;
import com.teamide.ide.enums.UserStatus;
import com.teamide.ide.util.TokenUtil;

@Resource
public class LoginService {

	public UserBean doLogin(ClientSession session, String loginname, String password) throws Exception {

		if (StringUtil.isEmpty(loginname)) {
			throw new Exception("登录名称不能为空.");

		}
		if (StringUtil.isEmpty(password)) {
			throw new Exception("密码不能为空.");

		}
		UserBean user = new UserService().getByLogin(loginname, password);

		if (user == null) {
			throw new Exception("用户名或密码错误！");
		}

		return doLoginById(session, user.getId());
	}

	public void recodeLogin(ClientSession session, String ip) throws Exception {
		if (session != null && session.getUser() != null) {
			com.teamide.bean.UserBean user = session.getUser();
			String LOGIN_USER_TOKEN = session.get("LOGIN_USER_TOKEN", String.class);
			UserLoginBean loginBean = new UserLoginBean();
			loginBean.setToken(LOGIN_USER_TOKEN);
			loginBean.setUserid(user.getId());
			loginBean.setUsername(user.getName());
			loginBean.setLoginname(user.getLoginname());
			loginBean.setStarttime(BaseService.PURE_DATETIME_FORMAT.format(new Date()));
			loginBean.setEndtime(BaseService.PURE_DATETIME_FORMAT.format(new Date()));
			setIP(loginBean, ip);
			loginBean = new UserLoginService().insert(session, loginBean);
			session.setCache("USER_LOGIN_ID", loginBean.getId());

		}
	}

	private void setIP(UserLoginBean loginBean, String ip) throws Exception {
		if (loginBean == null || StringUtil.isEmpty(ip)) {
			return;
		}
		loginBean.setIp(ip);

		try {
			String url = "https://api.map.baidu.com/location/ip?ak=26a9d76323f4844c62d222de456a5d31";
			url += "&coor=bd09ll";
			url += "&ip=" + ip;
			HttpRequest request = HttpRequest.get(url);
			request.timeout(1000 * 5);
			HttpResponse response = request.execute();
			String body = response.body();
			JSONObject json = JSONObject.parseObject(body);
			if (json.get("content") != null) {
				JSONObject content = json.getJSONObject("content");

				if (content.get("address_detail") != null) {
					JSONObject address_detail = content.getJSONObject("address_detail");
					loginBean.setProvince(address_detail.getString("province"));
					loginBean.setCity(address_detail.getString("city"));
					loginBean.setDistrict(address_detail.getString("district"));
					loginBean.setStreet(address_detail.getString("street"));
				}
			}
		} catch (Exception e) {
		}

	}

	public UserBean doLoginById(ClientSession session, String id) throws Exception {
		UserBean user = new UserService().get(id);
		if (user != null && session != null) {
			checkLogin(id);
			JSONObject userJSON = (JSONObject) JSONObject.toJSON(user);

			session.doLogin(userJSON.toJavaObject(com.teamide.bean.UserBean.class));

			List<RoleBean> roles = new RoleService().queryUserVisibleRoles(user.getId());
			session.setCache("user", user);
			session.setCache("roles", roles);
			session.setCache("isManager", false);
			JSONObject json = new JSONObject();
			json.put("id", user.getId());
			json.put("loginname", user.getLoginname());
			json.put("name", user.getName());
			json.put("timestamp", System.currentTimeMillis());
			String LOGIN_USER_TOKEN = TokenUtil.getToken(json);
			session.setCache("LOGIN_USER_TOKEN", LOGIN_USER_TOKEN);

			for (RoleBean role : roles) {
				if (role.isForsuper()) {
					session.setCache("isManager", true);
				}
			}

		}
		return user;
	}

	public void checkLogin(String id) throws Exception {
		UserBean user = new UserService().get(id);
		if (user == null) {
			return;
		}
		IDEConfigure configure = IDEConfigure.get();
		UserStatus status = UserStatus.get(user.getStatus());
		switch (status) {
		case DESTROY:
			throw new Exception("账号已销毁，请联系管理员！");
		case DISABLE:
			throw new Exception("账号已禁用，请联系管理员！");
		case LOCK:
			Integer unlockminute = 0;
			if (configure.getLogin() != null) {
				unlockminute = configure.getLogin().getUnlockminute();
			}
			if (unlockminute == null || unlockminute <= 0) {
				throw new Exception("账号已锁定，，请联系管理员解锁！");
			}

			String locktime = user.getLocktime();
			if (StringUtil.isEmpty(locktime)) {
				locktime = BaseService.PURE_DATETIME_FORMAT.format(new Date());
				UserBean one = new UserBean();
				one.setId(id);
				one.setLocktime(locktime);
				new UserService().update(one);
			}

			Date lockdate = BaseService.PURE_DATETIME_FORMAT.parse(locktime);

			long time = new Date().getTime() - lockdate.getTime();
			long waittime = unlockminute * 60 * 1000 - time;
			if (waittime > 0) {
				throw new Exception("账号已锁定，请等待“" + (waittime / 60 / 1000) + 1 + "”分钟后自动解锁，或联系管理员解锁！");
			} else {
				UserBean one = new UserBean();
				one.setId(id);
				one.setStatus(UserStatus.OK.getValue());
				one.setUnlocktime(BaseService.PURE_DATETIME_FORMAT.format(new Date()));
			}
			break;
		case OK:
			break;
		}

		if (configure.getAccount() != null && ObjectUtil.isTrue(configure.getAccount().getOpenactivation())) {
			UserActiveStatus activeStatus = UserActiveStatus.get(user.getActivestatus());
			switch (activeStatus) {
			case NOT_ACTIVE:
				throw new Exception("账号暂未激活，请先激活后使用！");
			case OK:
				break;
			}
		}

	}

}
