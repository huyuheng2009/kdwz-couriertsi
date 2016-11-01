package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.ManagerDao;
import com.yogapay.couriertsi.utils.StringUtil;
import com.yogapay.couriertsi.utils.StringUtils;

/**
 * 用户service
 * @author 
 *
 */
@Service
public class UserService {

	@Resource
	private Dao dao ;
	
	@Resource
	private ManagerDao managerDao ;
	
	public boolean save(User user) throws SQLException {
		String sql = "insert into courier(courier_no,user_name,pass_word,real_name,id_card,phone,head_image,regist_time,queue_name,substation_no) values(?,?,?,?,?,?,?,?,?,?)";
		List<Object> list = new ArrayList<Object>();
		list.add(user.getCourierNo());
		list.add(user.getUserName()) ;
		list.add(user.getPassWord()) ;
		list.add(user.getRealName()) ;
		list.add(user.getIdCard()) ;
		list.add(user.getPhone()) ;
		list.add(user.getHeadImage()) ;
		list.add(DateUtils.formatDate(user.getRegistTime(),"yyyy-MM-dd HH:mm:ss")) ;
		list.add(user.getQueueName()) ;
		list.add(user.getSubstationNo());
		dao.updateGetID(sql, list);
		return true ;
	}

	/**
	 * 查询登录
	 * @param userName
	 * @param pwd
	 * @return
	 * @throws SQLException
	 */
	public User getUserByPwd(String userName, String pwd) throws SQLException {
		String sql = "select * from courier where user_name=? and pass_word=?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(userName) ;
		list.add(pwd);
		return dao.findFirst(User.class, sql,list.toArray() ) ;
	}
	/**
	 * 查询用户信息
	 * @param userName
	 * @param pwd
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> getMapByPwd(String userName, String pwd) throws SQLException {
		String sql = "select * from courier where user_name=? and pass_word=?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(userName) ;
		list.add(pwd);
		return dao.findFirst(sql,list.toArray() ) ;
	}
	public User getUserByNoPwd(String courierNo, String pwd) throws SQLException {
		String sql = "select * from courier where courier_no=? and pass_word=?" ;
		List<Object> list = new ArrayList<Object>();
		list.add(courierNo) ;
		list.add(pwd);
		return dao.findFirst(User.class, sql,list.toArray() ) ;
	}

	//查询快递员信息
	public User getUserByNo(String courierNo) throws SQLException {
		String sql = "select * from courier where courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(courierNo) ;
		return dao.findFirst(User.class, sql,list.toArray() ) ;
	}

	public boolean update(User user) throws SQLException {
		String sql = "update courier set real_name=?," + "id_card=?,"
				+ "phone=?," + "substation_no=?,"+ "head_image=? "
				+ " where id=? and courier_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(user.getRealName()) ;
		list.add(user.getIdCard()) ;
		list.add(user.getPhone()) ;
		list.add(user.getSubstationNo()) ;
		list.add(user.getHeadImage()) ;
		list.add(user.getId()) ;
		list.add(user.getCourierNo()) ;
		dao.updateGetID(sql, list);
		return true;
	}

	public boolean updateInfo(User user) throws SQLException {
		String sql = "update courier set real_name=?,id_card=?,regist_time=? where courier_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(user.getRealName()) ;
		list.add(user.getIdCard()) ;
		list.add(DateUtils.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss"));
		list.add(user.getCourierNo()) ;
		dao.updateGetID(sql, list);
		return true;
	}




	public User getUserByUserName(String userName) throws SQLException {
		String sql = "select * from courier where user_name=? " ;
		return dao.findFirst(User.class, sql, userName) ;
	}


	public User getUserByUserNo(String userNo) throws SQLException {
		String sql = "select * from courier where courier_no=? " ;
		return dao.findFirst(User.class, sql, userNo) ;
	}

	public int cpwd(String userNo, String pwd) throws SQLException {
		String sql = "update courier set pass_word=? where courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(pwd) ;
		list.add(userNo) ;
		return dao.update(sql, list.toArray()) ;
	}

	public int cphone(String userNo,String phone) throws SQLException {
		String sql = "update courier set user_name=?,phone=? where user_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(phone) ;
		list.add(phone) ;
		list.add(userNo) ;
		return dao.update(sql, list.toArray()) ;
	}

	public Map<String, Object> getUserInfo(String userNo) throws SQLException {
		String sql = "select s.sarea sarea,c.real_name realName,c.phone,c.id_card idCard,c.head_image headImage,c.queue_name queueName,s.exchange,l.name lgcName,l.contact  contact  "
				+" from courier c,substation s,lgc l where c.substation_no=s.substation_no and s.lgc_no=l.lgc_no and c.courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(userNo) ;
		return dao.findFirst(sql,list.toArray()) ;
	}

	/**
	 * 查询公司号
	 * @param userNo
	 * @return
	 * @throws SQLException
	 */
	public String getUserLgcNo(String userNo) throws SQLException {
		String sql = "select l.lgc_no lgcNo"
				+" from courier c,substation s,lgc l where c.substation_no=s.substation_no and s.lgc_no=l.lgc_no and c.courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(userNo) ;
		Map<String, Object> ret = dao.findFirst(sql,list.toArray()) ;
		String lgcNo = "" ;
		if (ret!=null) {
			lgcNo = ret.get("lgcNo").toString() ;
		}
		return lgcNo ;
	}
	/***
	 * 查询分站号
	 * @param userNo
	 * @return
	 * @throws SQLException
	 */
	public String getUserSubNo(String userNo) throws SQLException {
		String sql = "select substation_no  from courier where courier_no=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(userNo) ;
		Map<String, Object> ret = dao.findFirst(sql,list.toArray()) ;
		String subNo = "" ;
		if (ret!=null) {
			subNo = ret.get("substation_no").toString() ;
		}
		return subNo ;
	}
	public int changePhone(String phone,String userNo) throws SQLException {
		String sql = "update courier set  phone=?   where   courier_no=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(phone) ;
		list.add(userNo) ;
		return dao.update(sql, list.toArray()) ;
	}
	public String getUserNoByRealName(String realName) throws SQLException {
		String sql = "select courier_no  "
				+" from courier where real_name=? " ;
		List<Object> list = new ArrayList<Object>();
		list.add(realName) ;

		Map<String,Object> map =	dao.findFirst(sql,list.toArray()) ;
		if(map==null){
			return "";
		}
		return String.valueOf(map.get("courier_no"));
	}

	//保存坐标轨迹
	public int  saveTrack(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into courier_track_location(user_no,app_version,android_id,create_time,ip,track_longitude,track_latitude)values(?,?,?,?,?,?,?)";		
		list.add(params.get("user_no"));//签到15分钟后才能签退
		list.add(params.get("app_version"));//签到时间
		list.add(params.get("android_id"));//最后更改时间
		list.add(params.get("create_time"));//签到次数
		list.add(params.get("ip"));//用户号
		list.add(params.get("track_longitude"));	
		list.add(params.get("track_latitude"));	//
		return dao.update(sql, list.toArray()); 
	}
	//保存地址
	public int  addrSyn(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into yx_addr_manage(courierId,senderUserId,name,phone,cityId,areaAddr,addr,defaultz,useTimes,areaId,createTime)values(?,?,?,?,?,?,?,?,?,?,?)";		
		list.add(params.get("courierId"));
		list.add(params.get("senderUserId"));
		list.add(params.get("name"));
		list.add(params.get("phone"));
		list.add(params.get("cityId"));
		list.add(params.get("areaAddr"));
		list.add(params.get("addr"));	
		list.add(params.get("defaultz"));	
		list.add(params.get("useTimes"));	
		list.add(params.get("areaId"));	
		list.add(params.get("createTime"));	
		return dao.update(sql, list.toArray()); 
	}
	///查询最新的地址
	public List<Map<String,Object>>  addrList(Map<String, String> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select * from yx_addr_manage where courierId=?");
		list.add(params.get("courierNo"));
		if(!StringUtil.isEmptyWithTrim(params.get("createTime"))){
			sql.append("AND createTime > ?");
			list.add(params.get("createTime"));
		}
		return dao.find(sql.toString(), list.toArray()); 
	}	
	// 根据电话号码 查询所有地址
	public List<Map<String,Object>>  checkAddrList(String user,String phone,String area,String addr) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select * from yx_addr_manage where courierId=? and phone=? and areaAddr=? ");
		list.add(user);
		list.add(phone);
		list.add(area);
		if(StringUtils.isNotEmptyWithTrim(addr)){
			sql.append(" and addr=?");	
			list.add(addr);	
		}	
		return dao.find(sql.toString(), list.toArray()); 
	}	


	//查询VIP信息 通过账户
	public Map<String, Object>  checkVipbInfo(String vipNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select  "
				//				+ "a.substation_no substationNo,"
				//				+ "a.dis_user_no disUserNo,"
				+ "a.dis_type disType,"
				+ "a.dis_user_name disUserName,"
				+ "a.contact_name contactName,"
				+ "a.contact_phone contactPhone,"
				//				+ "a.email email,"
				//				+ "a.pwd pwd,"
				//				+ "a.operator operator,"
				//				+ "a.create_time createTime,"
				+ "a.note note,"
				//				+ "a.status status,"
				+ "b.uid uid,"
				+ "b.balance balance"		
				+ " from 	dis_user  a JOIN dis_user_balance b ON  a.id=b.uid where dis_user_no = ?";
		list.add(vipNo);
		return dao.findFirst(sql,list.toArray()); 
	}	
	//查询VIP信息 密码
	public Map<String, Object>  checkVipbInfoByPwd(String vipNo,String pwd) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select  "
				//				+ "a.substation_no substationNo,"
				//				+ "a.dis_user_no disUserNo,"
				+ "a.dis_type disType,"
				+ "a.dis_user_name disUserName,"
				+ "a.contact_name contactName,"
				+ "a.contact_phone contactPhone,"
				//				+ "a.email email,"
				//				+ "a.pwd pwd,"
				+ "a.operator operator,"
				+ "a.create_time createTime,"
				+ "a.note note,"
				+ "a.status status,"
				+ "b.uid uid,"
				+ "b.balance balance"		
				+ " from 	dis_user  a JOIN dis_user_balance b ON  a.id=b.uid where dis_user_no = ? and pwd=?";
		list.add(vipNo);
		list.add(pwd);
		return dao.findFirst(sql,list.toArray()); 
	}	
	//添加VIP信息
	public long  addVip(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into dis_user(substation_no,dis_user_no,dis_type,dis_user_name,contact_name,"
				+ "contact_phone,email,pwd,operator,create_time,note,status) "
				+ "                                 values(?,?,?,?,?,?,?,?,?,?,?,?)";		
		list.add(params.get("substation_no"));
		list.add(params.get("dis_user_no"));
		list.add(params.get("dis_type"));
		list.add(params.get("dis_user_name"));
		list.add(params.get("contact_name"));
		list.add(params.get("contact_phone"));
		list.add(params.get("email"));	
		list.add(params.get("pwd"));	
		list.add(params.get("operator"));	
		list.add(params.get("create_time"));	
		list.add(params.get("note"));	
		list.add(params.get("status"));	
		return dao.updateGetID(sql,list); 
	}


	//新增VIP金额
	public int  addVipMoney(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into dis_user_balance(uid,balance,last_update_time)values(?,?,?)";
		list.add(params.get("uid"));
		list.add(params.get("balance"));
		list.add(params.get("last_update_time"));
		return dao.update(sql,list.toArray()); 
	}
	//vip金额消费
	public int  minusBalance(float reality,String lastUpdateTime,String UID) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update  dis_user_balance set balance=balance-? ,last_update_time=? where uid=?";
		list.add(reality);
		list.add(lastUpdateTime);
		list.add(UID);
		return dao.update(sql,list.toArray()); 
	}
	//vip金额添加
	public int  addBalance(float reality,String lastUpdateTime,String UID) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update  dis_user_balance set balance=balance+? ,last_update_time=? where uid=?";
		list.add(reality);
		list.add(lastUpdateTime);
		list.add(UID);
		return dao.update(sql,list.toArray()); 
	}

	//添加会员充值记录
	public void  addHistory(Map<String, Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into recharge_history("
				+ "dis_user_no,"
				+ "rmoney,"
				+ "omoney,"
				+ "af_balance,"
				+ "status,"
				+ "discount_text,"
				+ "lied,"
				+ "gather_no,"
				+ "gather_no_type,"
				+ "operator,"
				+ "create_time,"
				+ "last_update_time,"
				+ "note,"
				+ "source,"
				+ "pay_type,"
				+ "acount_number"
				+ ")values("
				+ " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		list.add(params.get("dis_user_no"));
		list.add(params.get("rmoney"));
		list.add(params.get("omoney"));
		list.add(params.get("af_balance"));
		list.add(params.get("status"));
		list.add(params.get("discount_text"));
		list.add(params.get("lied"));
		list.add(params.get("gather_no"));
		list.add(params.get("gather_no_type"));
		list.add(params.get("operator"));
		list.add(params.get("create_time"));
		list.add(params.get("last_update_time"));
		list.add(params.get("note"));
		list.add(params.get("source"));;
		list.add(params.get("pay_type"));;
		list.add(params.get("acount_number"));;
		dao.update(sql,list.toArray()); 
	}
	//查询VIP信息
	public Map<String, Object>  checkPrivilegeType(String vipNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select * from discount_type where id=?";
		list.add(vipNo);
		return dao.findFirst(sql,list.toArray()); 
	}	
	//查询充值记录信息
	public Page<Map<String, Object>>  checkVipHistory(String vipNo,PageRequest pageRequest) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select "
				+ "dis_user_no disUserNo ,"
				+ "rmoney ,"
				+ "pay_type payType,"
				+ "create_time createTime "
				+ "from recharge_history where gather_no= ?  and  status ='SUCCESS'  and create_time >? order by create_time desc";
		list.add(vipNo);
		list.add(DateUtils.formatDate(DateUtils.addDate(new Date(), -30, 0,0),"yyyy-MM-dd HH:mm:ss"));
		return dao.find(sql,list.toArray(),pageRequest); 
	}	
	//查询充值记录总金额
	public String  checkVipHisMoney(String vipNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select "				
				+ "sum(rmoney) sumMoney "
				+ "from recharge_history where gather_no= ? and status ='SUCCESS'  and create_time >? order by create_time desc";
		list.add(vipNo);
		list.add(DateUtils.formatDate(DateUtils.addDate(new Date(), -30, 0,0),"yyyy-MM-dd HH:mm:ss"));
		Map<String,Object> map=dao.findFirst(sql,list.toArray()); 	
		return String.valueOf(map.get("sumMoney"));

	}	
	//会员优惠类型
	public List<Map<String,Object>>  vipType() throws SQLException {
		String sql = "select  id,name,discount ,discount_text discountText,note  from discount_type order by id";
		return dao.find(sql);	
	}	
	//问题件类型
	public List<Map<String,Object>>  getProOrderReason() throws SQLException {
		String sql = "select  id,reason_no reasonNo ,context  from pro_order_reason where status ='1' order by id";
		return dao.find(sql);	
	}	
	//物品类型
	public List<Map<String,Object>>  getItemType() throws SQLException {
		String sql = "select  id,item_text itemText ,note  from item_type order by id";
		return dao.find(sql);	
	}	
	//时效件
	public List<Map<String,Object>>  getAgingType() throws SQLException {
		String sql = "select  id,item_text itemText ,note  from aging_type order by id";
		return dao.find(sql);	
	}	
	//物品类型
	public List<Map<String,Object>>  getRequired() throws SQLException {
		String sql = "select rl.name name ,rl.describe,rl.required required  from require_list rl ,require_type rt "
				+ "where rl.tid =rt.id and rt.code='ORDER_TAKE'  and rl.required='Y' ";  // and rl.required='Y'
		return dao.find(sql);	
	}	
	
	public List<Map<String,Object>>  getAllRequired() throws SQLException {
		String sql = "select rl.name name ,rl.describe,rl.required required  from require_list rl ,require_type rt "
				+ "where rl.tid =rt.id and rt.code='ORDER_TAKE'  ";  // and rl.required='Y'
		return dao.find(sql);	
	}
	
	//是否有订单大厅
	public Map<String,Object> lgcMode() throws SQLException {
		String sql = "select name,code,`describe`,order_room orderRoom  from lgc_mode where status=1";			
		return dao.findFirst(sql);	
	}	
	//所有的付款类型
	public List<Map<String,Object>>  payType() throws SQLException {
		String sql = "select *  from pay_type order by id";
		return dao.find(sql);	
	}	
	//更改会员优惠等级
	public int  updateVipType(String disUserNo,String type ) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update  dis_user set dis_type=? where dis_user_no=?";
		list.add(type);
		list.add(disUserNo);
		return dao.update(sql,list.toArray()); 
	}
	//所有优惠类型
	public Map<String,Object>  disType(int id) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select *  from discount_type  where id=?";
		list.add(id);
		return dao.findFirst(sql,list.toArray());	
	}	
	//vip消费记录
	public int vipExpense(Map<String,Object> params) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "insert into consume_history("
				+ "dis_user_no,"
				+ "rmoney,"
				+ "omoney,"
				+ "af_balance,"
				+ "status,"
				+ "order_no,"
				+ "discount_text,"
				+ "lied,"
				+ "courier_no,"
				+ "operator,"
				+ "create_time,"
				+ "last_update_time,"
				+ "note,"
				+ "source)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		list.add(params.get("dis_user_no"));
		list.add(params.get("rmoney"));
		list.add(params.get("omoney"));
		list.add(params.get("af_balance"));
		list.add(params.get("status"));
		list.add(params.get("order_no"));
		list.add(params.get("discount_text"));
		list.add(params.get("lied"));
		list.add(params.get("courier_no"));
		list.add(params.get("operator"));
		list.add(params.get("create_time"));
		list.add(params.get("last_update_time"));
		list.add(params.get("note"));
		list.add(params.get("source"));
		return dao.update(sql,list.toArray());	
	}


	public String queryBalance(String uid) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select * from  dis_user_balance where uid=?";
		list.add(uid);
		Map<String,Object> map  =  dao.findFirst(sql,list.toArray());		
		return  String.valueOf(map.get("balance"));
	}

	public String queryReasonContext(String object) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "select  context  from  pro_order_reason where id=?";
		list.add(object);
		Map<String,Object> map  =  dao.findFirst(sql,list.toArray());		
		if(map==null){
			return "";
		}
		return  String.valueOf(map.get("context"));
	}
	/**
	 * 会员充值成功后将状态改为启用
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public void updateVipStatus(String VipNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update dis_user set status = 1 where  dis_user_no = ?";
		list.add(VipNo)	;
		dao.update(sql, list.toArray());
	}
	/**
	 * 会员充值失败后更改充值记录状态
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public void updateVipRecStatus(String VipNo) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update recharge_history set status ='SUCCESS'  where  dis_user_no = ?";
		list.add(VipNo)	;
		dao.update(sql, list.toArray());
	}

	/**
	 * 会员充值流水状态
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public void  updateStausAcount(float rmoney,float afBalance, String Acount,String disText) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update  recharge_history set status='SUCCESS' ,rmoney=? ,af_balance=? ,discount_text= ? where acount_number = ?";
		list.add(rmoney);
		list.add(afBalance);
		list.add(disText);
		list.add(Acount);
		dao.update(sql,list.toArray());	

	}
	/**
	 * 会员新增流水状态
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public void  updateStausAcount( String Acount) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update  recharge_history set status='SUCCESS'  where acount_number = ?";
		list.add(Acount);
		dao.update(sql,list.toArray());	

	}
	/**
	 * 查询快递员信息
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public 	List<Map<String,Object>>  queryC( String message) throws SQLException {
		String sql = "select user_name userName,real_name realName ,inner_phone innerPhone   from courier where   user_name like  '%"+message+"%'  OR    real_name like '%"+message+"%'    OR  inner_phone like '%"+message+"%'";	
	return dao.find(sql);	
	}
	/**
	 * 修改密码
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public 	void changePwd( String userNo,String newPwd) throws SQLException {
		List<Object> list = new ArrayList<Object>();
		String sql = "update courier set pass_word  = ? where courier_no = ?"	;
		list.add(newPwd);
		list.add(userNo);				
	 dao.update(sql,list.toArray());
	 
	}
}
