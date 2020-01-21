(function() {
	var DaoEditor = coos.Editor.Dao;

	DaoEditor.prototype.createSqlDeleteView = function(model) {
		var that = this;
		var $box = $('<li />');
		model = model || {};
		model.wheres = model.wheres || [];
		var wheres = model.wheres;

		var $ul = $('<ul class="sub1"/>')
		$box.append($ul);

		var $li = $('<li />');
		$ul.append($li);
		$li.append('<span class="pdr-10 color-orange">DELETE FROM</span>');

		var $input = $('<input class="input" name="table" />');
		app.autocomplete({
			$input : $input,
			datas : that.getOptions('TABLE')
		})
		$input.val(model.table);
		$li.append($input);
		that.bindLiEvent($li, model, false);

		var table = that.getTableByName(model.table);
		that.appendWhereLi($ul, wheres, table);

		return $box;
	};

})();