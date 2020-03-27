package com.teamide.ide.generater.code.sql;

import com.teamide.app.AppContext;
import com.teamide.app.process.dao.sql.Delete;
import com.teamide.util.StringUtil;

public class DeleteGenerater extends SqlGenerater {

	protected final Delete delete;

	public DeleteGenerater(AppContext context, String factory_classname, Delete delete) {
		super(context, factory_classname, delete);
		this.delete = delete;
	}

	public void doGenerate(int tab) {
		StringBuffer sql = new StringBuffer();

		sql.append("DELETE FROM ");

		String table = StringUtil.trim(delete.getTable());
		if (!StringUtil.isEmpty(table)) {
			sql.append(table);
			sql.append(" ");
		}

		content.append(getTab(tab)).append("// 删除语句").append("\n");
		content.append(getTab(tab)).append("sql.append(\"" + sql + "\");").append("\n");

		content_mapper.append(getTab(tab)).append(sql).append("\n");

		content.append(getTab(tab)).append("// 组合条件语句").append("\n");
		ignoreWhereTablealias(delete.getWheres());
		appendWhere(tab, delete.getWheres());

		if (delete.getAppends() != null && delete.getAppends().size() > 0) {
			content.append("\n");
			content.append(getTab(tab)).append("// 追加SQL").append("\n");
			appendAppends(tab, delete.getAppends());
		}

		content.append("\n");
		content_mapper.append("\n");

	}

}
