# 说明
本接口使用 HTTP + JSON 方式进行调用。

	通用错误码：
	-9000 - 未知访问

# 目录

1. [转件：获取快递公司](#order_transfer_get_cpn)
2. [转件：进行转件](#order_transfer_get_cpn)
3. [收件：计算运费](#order_take_freight_calculate)
4. [消息：批量删除消息](#msg_del)
5. [获取处罚条例](#lgc_punish)
6. [个人中心：修改密码](#user_cpwd)
7. [个人中心：仓管员修改密码](#user_mcpwd)

# 接口

##1. <a name="login">转件：获取快递公司</a>
#### 说明：
#### URL：
	/api2/order/transfer/get_cpn
#### 请求参数：
	名称        类型(长度)     是否必需    说明
#### 返回值：
	{
		value:[
			{
				id:1,			/* 快递公司ID */
				cpn_name:""		/* 快递公司名称 */
			}
		]
	}

##2. <a name="login">转件：进行转件</a>
#### 说明：
#### URL：
	/api2/order/transfer/transfer_order
#### 请求参数：
	名称            类型(长度)     是否必需    说明
	c_order_no      String         是          原始运单号
	cpn_id          int            是          转出的目标快递公司ID
    cpn_order_no    String         是          转出的目标快递公司运单号
#### 返回值：

##3. <a name="order_take_freight_calculate">收件：计算运费</a>
#### 说明：
#### URL：
	/api2/order/take/freight_calculate
#### 请求参数：
	名称            类型(长度)     是否必需    说明
	time_type       String         是          时效类型
	item_type       String         是          物品类型
    item_weight     double         是          重量
	distance        double         是          距离
#### 返回值：
	{
		value:
			{
				freight:8			/* 运费 */
			}
	}


##4. <a name="msg_del">消息：批量删除消息</a>
#### 说明：
#### URL：
	/msg/del
#### 请求参数：
	名称            类型(长度)     是否必需    说明
	delAll          String         是          是否全部删除，1：全部删除，默认为0
	msgCode         String         是          消息code
    msgIds          String         否          消息id，多个以逗号分隔
#### 返回值：
	{
	    respMsg：'删除成功'
	}



##5. <a name="lgc_punish">获取处罚条例</a>
#### 说明：
#### URL：
	/lgc/punish
#### 请求参数：
	名称            类型(长度)     是否必需    说明
#### 返回值：
	{
        respTime:"2016-02-02 02:02:02"，
		isSuccess:true，
		respNo:"1234"，
		respCode:"0100"，            /* 返回码 */
		respMsg:""                  /* 返回提示文本 */
		list:
			[
              {
				punishText:"无故拒绝收派件。"，			/* 违规事项 */
				ruleText:"50元/票"			            /* 处罚标准 */
			  }
         ]
	}


##6. <a name="user_cpwd">个人中心：修改密码</a>
#### 说明：
#### URL：
	/user/cpwd
#### 请求参数：
	名称            类型(长度)     是否必需    说明
	oldPassWord     String         是          旧密码
	nwPassWord      String         是          新密码
#### 返回值：
	{
	    respMsg：'修改成功'
	}



##7. <a name="user_mcpwd">个人中心：仓管员修改密码</a>
#### 说明：
#### URL：
	/user/mcpwd
#### 请求参数：
	名称            类型(长度)     是否必需    说明
	oldPassWord     String         是          旧密码
	nwPassWord      String         是          新密码
#### 返回值：
	{
	    respMsg：'修改成功'
	}