<#ftl encoding="utf-8">
<form id="edit_form" class="form-inline" name="edit_form" method="post" action="save.htm">
	 <input type="hidden" name="csrfToken" value="${csrfToken!}">
	<#include "edit1.ftl">

</form>
<div class="alert" id="task_save_result" style="display: none"></div>
<script>
	$(document).ready(
			function() {
                $("#pushFlag").val(${entity.pushFlag});

				$("#myModalOK").unbind("click").click(function() {
					$("#edit_form").submit();

				});
				$("#edit_form").validate(
						{
							submitHandler : function(form) {
								Common.ajaxSubmit("#edit_form", function(
										success, text) {
									if (success) {
                                        $("#task_save_result").removeClass(
                                                "alert-danger").addClass(
                                                "alert-success");
                                        edit_form.id.value=text;
                                    }
									else
										$("#task_save_result").removeClass(
												"alert-success").addClass(
												"alert-danger");
									$("#task_save_result").html(
                                            (success ? "保存成功!": text)+" ["+new Date().toLocaleString()+"]");
									$("#task_save_result").show();

								});

							}
						});

			});

	function onAppChange(sender) {
		var v=sender.value;
        Common.ajaxPost("getIpList.htm",{"appName":v},function (success,text) {
            var list=JSON.parse(text);
            var h="";
            for(var i=0;i<list.length;i++) {
                  h += '<li><a href="#" onclick="edit_form.targetIp.value=this.innerText">'+list[i]+'</a></li>';
            }
            $("#targetIpList").html(h);
        });
    }
</script>
