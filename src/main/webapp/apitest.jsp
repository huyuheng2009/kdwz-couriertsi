<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="random" class="java.util.Random" />
<!DOCTYPE html>
<html>
    <head>
		<base href="api2/" target="_new" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0" />
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-status-bar-style" content="black" />
		<meta content="telephone=no" name="format-detection" />
		<script type="text/javascript" src="/scripts/jquery-1.9.1.min.js"></script>
		<style>
			h1 {cursor: pointer;}
		</style>
		<script type="text/javascript">
			$(function () {
				$("h1").next("form").hide();
				$("h1").data("open", false);
				$("h1").click(function () {
					var open = $(this).data("open");
					$("h1").next("form").hide();
					$("h1").data("open", false);
					if (!open) {
						$(this).next("form").show();
					}
					$(this).data("open", !open);
				});
			});
		</script>
        <title>API TEST</title>
		<style>
			textArea {width: 800px;height: 300px;}
		</style>
    </head>
    <body>
		<c:if test="${config['api2.test']}">
			<form action="../user/userLogin" method="POST">
				<input type="hidden" name="reqNo" value="1" />
				<input type="hidden" name="reqTime" value="2016-06-24 00:00:00" />
				<input type="hidden" name="appVersion" value="android.kdwz_c.3.4.0" />
				<input type="hidden" name="uid" value="yx_courier" />
				<input type="hidden" name="userName" value="13800138000" />
				<input type="hidden" name="passWord" value="1234" />
				<input type="hidden" name="androidId" value="13800138000" />
				<input type="submit" value="登录" />
			</form>
			<a href="order/transfer/get_cpn">转件：获取快递公司</a>
			<br />
			<h1>转件：进行转件</h1>
			<form action="order/transfer/transfer_order" method="POST">
				<table>
					<tr>
						<td>c_order_no</td>
						<td><input type="text" name="c_order_no" value="" /></td>
					</tr>
					<tr>
						<td>cpn_id</td>
						<td><input type="text" name="cpn_id" value="" /></td>
					</tr>
					<tr>
						<td>cpn_order_no</td>
						<td><input type="text" name="cpn_order_no" value="" /></td>
					</tr>
					<tr>
						<td></td>
						<td><input type="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
		</c:if>
	</body>
</html>
