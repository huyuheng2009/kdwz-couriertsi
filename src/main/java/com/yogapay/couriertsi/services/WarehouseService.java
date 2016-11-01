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
import com.yogapay.couriertsi.utils.StringUtils;

@Service
public class WarehouseService {

	@Resource
	private Dao dao ;
	/**
	 * 插入仓记录
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public void  savaInputOrOutPut(Map<String,Object> map) throws SQLException{
		String sql ="insert into  warehouse_io_register(manager_no,lgc_order_no,create_time,input_or_output,out_sub_or_courier,out_number_no,`signal`,error_context)"
				+ "values(?,?,?,?,?,?,?,?)";	
		List<Object> list  = new ArrayList<Object>();
		list.add(map.get("managerNo"));
		list.add(map.get("lgcOrderNo"));
		list.add(map.get("createTime"));
		list.add(map.get("inputOrOutput"));
		list.add(map.get("outSubOrCourier"));
		list.add(map.get("outNumberNo"));
		list.add(map.get("signal"));
		list.add(map.get("errorContext"));
		dao.update(sql,list.toArray());
	}
	/**
	 * 查询出入仓记录
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object> getMapByOrder(String lgcOrderNo,String IO,String signal) throws SQLException{
		String sql ="select id, "
				+ "manager_no managerNo,"
				+ "lgc_order_no lgcOrderNo,"
				+ "input_or_output inputOrOutput,`signal` , "
				+ "error_context errorContext"
				+ " from warehouse_io_register w "
				+ "where  w.lgc_order_no=? AND w.input_or_output = ? AND w.signal=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(lgcOrderNo);
		list.add(IO);
		list.add(signal);
		return dao.findFirst(sql,list.toArray());
	}
	/**
	 * 查询分站快递员
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>>getListBySubstation(String sub) throws SQLException{
		String sql ="select id, "
				+ "real_name realName,"
				+ "courier_no courierNo"
				+ "from courier "
				+ "where  substation_no=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(sub);
		return dao.find(sql,list.toArray());
	}
	/**
	 * 查询是否有仓管员权限
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public Map<String,Object>getManagerGroup(String userName,String pwd) throws SQLException{
		String sql1 ="select id,user_name,real_name,'' as email,status,pass_word as password,create_operator,warehouse_no as sno,"+
					 "-1 as fail_times,regist_time as update_time,regist_time as create_time,0 as input_times,"+
					 "'Y' as clogin,substation_no as substationNo from warehouse_staff where user_name=? and pass_word=?";
		String sql ="select a.*,c.clogin,d.substation_no	substationNo	from boss_user a ,	"
				+ "	boss_user_group b ,boss_group c ,substation_user d"
				+ "	where a.id = b.user_id and  b.group_id= c.id and d.user_id=a.id"
				+ "	and a.user_name=?  and a.password=? and c.clogin='Y'";
		List<Object> list  = new ArrayList<Object>();
		list.add(userName);
		list.add(pwd);
		Map<String, Object>  m = dao.findFirst(sql1,list.toArray());
		if(m!=null && !m.isEmpty()){
			return m;
		}
		return dao.findFirst(sql,list.toArray());
	}
	/**
	 * 仓管员分站权限
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public String getManagerSubStationByUserNo(String userName) throws SQLException{
		String sql1 ="select real_name as realName,substation_no as substationNo from warehouse_staff where user_name=?";
		String sql ="select a.real_name realName,d.substation_no	substationNo	from boss_user a ,	"
				+ "	boss_user_group b ,boss_group c ,substation_user d"
				+ "	where a.id = b.user_id and  b.group_id= c.id and d.user_id=a.id"
				+ "	and a.user_name=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(userName);
		Map<String,Object>  m = dao.findFirst(sql1,list.toArray());
		if(m!=null && !m.isEmpty()){
			return (String)m.get("substationNo");
		}
		Map<String,Object>  map = 	dao.findFirst(sql,list.toArray());
		if(map==null){
			return "";
		}
		return (String)map.get("substationNo");
	}
	/**
	 * 仓管员分站权限
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public String getMangerRealName(String userNo) throws SQLException{
		String sql1 ="select real_name as realName from warehouse_staff where user_name = ?";
		String sql ="select real_name realName from boss_user where user_name = ?";
		List<Object> list  = new ArrayList<Object>();
		list.add(userNo);
		Map<String,Object>  m = 	dao.findFirst(sql1,list.toArray());
		if(m!=null && !m.isEmpty()){
			return (String)m.get("realName");
		}
		Map<String,Object>  map = 	dao.findFirst(sql,list.toArray());
		if(map==null){
			return "";
		}
		return (String)map.get("realName");
	}
	/**
	 * 获取仓管员姓名
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public String getManagerRealName(String userName) throws SQLException{
		String sql1="select real_name as realName from warehouse_staff where user_name=?";
		String sql ="select a.real_name realName 	from boss_user a ,	"
				+ "	boss_user_group b ,boss_group c ,substation_user d"
				+ "	where a.id = b.user_id and  b.group_id= c.id and d.user_id=a.id"
				+ "	and a.user_name=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(userName);
		Map<String,Object>  m = 	dao.findFirst(sql1,list.toArray());
		if(m!=null && !m.isEmpty()){
			return (String)m.get("realName");
		}
		Map<String,Object>  map = 	dao.findFirst(sql,list.toArray());
		if(map==null){
			return "";
		}
		return (String)map.get("realName");
	}
	
	


	public void cpwd(String userName,String pwd) throws SQLException{
		String sql="update warehouse_staff set pass_word=? where user_name=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(pwd);
		list.add(userName);
        dao.update(sql, list.toArray()) ;
	}
	
	
	
	/**
	 * 查询是否存在分站
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public boolean SubStationISExist(String substationNo) throws SQLException{
		String sql ="select *	from substation where substation_no=?	";			
		List<Object> list  = new ArrayList<Object>();
		list.add(substationNo);
		Map<String,Object>  map = 	dao.findFirst(sql,list.toArray());
		if(map==null){
			return false;
		}
		return true;
	}
	/**
	 * 查询分站所有快递员
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>>  querySubCourier(String substationNo) throws SQLException{
		String sql ="select  real_name name, courier_no number,'C' `signal` 	from courier where substation_no=?	";			
		List<Object> list  = new ArrayList<Object>();
		list.add(substationNo);
		return dao.find(sql,list.toArray());
	}
	/**
	 * 查询所有分站
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>>  queryAllSub(String LGC) throws SQLException{
		String sql ="select  substation_name name, substation_no number, 'S'  `signal` 	from substation where lgc_no=?	";			
		List<Object> list  = new ArrayList<Object>();
		list.add(LGC);
		return dao.find(sql,list.toArray());
	}
	/**
	 * 查询分站名称
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public String getSubStationName(String subNo) throws SQLException{
		String sql ="select  substation_name substationName	from substation where substation_no=?	";			
		List<Object> list  = new ArrayList<Object>();
		list.add(subNo);
		Map<String,Object> map =  dao.findFirst(sql,list.toArray());
		if(map==null){
			return "";
		}
		return (String)map.get("substationName");
	}
	/**
	 * 查询出入仓记录
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public Page<Map<String, Object>> list(Map<String, String> params,PageRequest pageRequest) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		String io = params.get("io");
		if("I".equals(io)){
			sql.append( "select  lgc_order_no orderNo ,create_time IOTime ,input_or_output IO from "
					+ "warehouse_io_register w where w.manager_no = ?  AND w.signal= ?  ");
			list.add(params.get("managerUser"));
			list.add(params.get("signal"));		
			if(StringUtils.isNotEmptyWithTrim(params.get("io"))){
				sql.append("and w.input_or_output=?  ");
				list.add(params.get("io"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("startDate"))){
				sql.append(" and substr(w.create_time,1,10) = ?  ");
				list.add(params.get("startDate"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("beginTime"))){
				sql.append(" and substr(w.create_time,1,10) >= ?  ");
				list.add(params.get("beginTime"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("endTime"))){
				sql.append(" and substr(w.create_time,1,10) <=?  ");
				list.add(params.get("endTime"));
			}	
			sql.append("  order by w.create_time desc  ");		
		}else{
			sql.append("select n.* from (");
			sql.append( "select  w.lgc_order_no orderNo ,w.create_time IOTime ,w.input_or_output IO,"+
					"  IF(w.out_sub_or_courier='COURIER','C','S') OS,c.real_name realName from   "+     
					"warehouse_io_register w  join  courier c on w.out_number_no = c.courier_no where "
					+ " w.manager_no = ?  AND w.signal= ?  ");
			list.add(params.get("managerUser"));
			list.add(params.get("signal"));		
			if(StringUtils.isNotEmptyWithTrim(params.get("io"))){
				sql.append("and w.input_or_output=?  ");
				list.add(params.get("io"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("startDate"))){
				sql.append(" and substr(w.create_time,1,10) = ?  ");
				list.add(params.get("startDate"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("beginTime"))){
				sql.append(" and substr(w.create_time,1,10) >= ?  ");
				list.add(params.get("beginTime"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("endTime"))){
				sql.append(" and substr(w.create_time,1,10) <=?  ");
				list.add(params.get("endTime"));
			}	
			sql.append(" UNION ALL ");
			sql.append( " select  w1.lgc_order_no orderNo ,w1.create_time IOTime ,w1.input_or_output IO,"+
					"  IF(w1.out_sub_or_courier='COURIER','C','S') OS,s.substation_name realName from "+     
					"    warehouse_io_register w1  join  substation s on w1.out_number_no = s.substation_no   "
					+ "  where w1.manager_no = ?  AND w1.signal= ?  ");
			list.add(params.get("managerUser"));
			list.add(params.get("signal"));		
			if(StringUtils.isNotEmptyWithTrim(params.get("io"))){
				sql.append("and w1.input_or_output=?  ");
				list.add(params.get("io"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("startDate"))){
				sql.append(" and substr(w1.create_time,1,10) = ?  ");
				list.add(params.get("startDate"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("beginTime"))){
				sql.append(" and substr(w1.create_time,1,10) >= ?  ");
				list.add(params.get("beginTime"));
			}
			if(StringUtils.isNotEmptyWithTrim(params.get("endTime"))){
				sql.append(" and substr(w1.create_time,1,10) <=?  ");
				list.add(params.get("endTime"));
			}	
			sql.append("  order by IOTime  ) n");				
		}
		return dao.find(sql.toString(), list.toArray(), pageRequest) ;
	}
	/**
	 * 获取仓管员个人信息
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public 	Map<String,Object>   getManagerInfoByNo(String managerNo) throws SQLException{
		String sql1 ="select  real_name realName  from warehouse_staff where user_name=?";
		String sql ="select  real_name realName  from boss_user where user_name=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(managerNo);	
		Map<String,Object>  m = dao.findFirst(sql1,list.toArray());
		if(m!=null && !m.isEmpty()){
			return m;
		}
		return dao.findFirst(sql,list.toArray());
	}
	/**
	 * 将订单分配给快递员
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public  void managerUpdateOrder(Map<String,Object> map) throws SQLException{
		String sql ="update order_info set send_courier_no = ?  ,send_substation_no=? , status= 8 ,pro_order='N' ,take_mark='N' ,"
				+ "  send_order_time = ? ,last_update_time= ? where lgc_order_no=?";		
		List<Object> list  = new ArrayList<Object>();
		list.add(map.get("sendCourierNo"));	
		list.add(map.get("sendSubstationNo"));	
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));	
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));	
		list.add(map.get("lgcOrderNo"));	
		dao.update(sql,list.toArray());
	}
	/**
	 * 将订单出仓到分站
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public  void managerUpdateOrderToSubstation(Map<String,Object> map) throws SQLException{
		String sql ="update order_info set send_courier_no =null ,send_substation_no=? , status= 7 ,pro_order='N' ,take_mark='Y' ,"
				+ "  send_order_time = null  ,last_update_time= ? where lgc_order_no=?";		
		List<Object> list  = new ArrayList<Object>();
		list.add(map.get("outNumberNo"));	
		list.add(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));	
		list.add(map.get("lgcOrderNo"));	
		dao.update(sql,list.toArray());
	}
	/**
	 * 仓管员入仓 订单状态更新 入仓及待派
	 * @param lgcOrderNo
	 * @param IO
	 * @param signal
	 * @return
	 * @throws SQLException
	 */
	public  void managerUpdateOrderStatus(Map<String,Object> map) throws SQLException{
		String sql ="update order_info set status= 7 , send_courier_no =NULL ,send_order_time=NULL ,take_mark='Y' ,pro_order='N' ,take_mark='Y'  where lgc_order_no=?";		
		List<Object> list  = new ArrayList<Object>();
		list.add(map.get("lgcOrderNo"));	
		dao.update(sql,list.toArray());
	}
	/**
	 * 查询当日出入仓条数
	 * @param params
	 * @param pageRequest
	 * @return
	 * @throws SQLException
	 */
	public String count(Map<String, String> params ) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		List<Object> list = new ArrayList<Object>();   
		sql.append( "select  count(id) cout from "
				+ "warehouse_io_register w where w.manager_no = ?  AND w.signal= ?  AND  substr(w.create_time,1,10) = ? AND  w.input_or_output=? ");
		list.add(params.get("managerUser"));
		list.add(params.get("signal"));
		list.add(params.get("startDate"));
		list.add(params.get("IOSignal"));
		Map<String,Object> map = dao.findFirst(sql.toString(), list.toArray());
		if(map==null){
			return "0";
		}
		return String.valueOf(map.get("cout"));
	}
	/**
	 * 通过分站查查询快递公司
	 */
	public String getLgcNo(String subNo) throws SQLException {
		String sql = "select lgc_no lgcNo  from substation where substation_no=?";
		List<Object> list = new ArrayList<Object>();   
		list.add(subNo);
		Map<String,Object> map = dao.findFirst(sql,list.toArray());
		if(map==null){
			return "";
		}
		return (String)map.get("lgcNo");
	}
	
	public User  getManagerInfo(String userName) throws SQLException{
		String sql1="select id,user_name userName,pass_word `passWord`,real_name realName,substation_no substationNo,"+ 
				" id_card idCard,phone,'' courierNo,'' queueName,regist_time registTime,head_image headImage,inner_no innerNo,inner_phone innerPhone,status"+
				" from warehouse_staff where user_name=?";
		String sql ="select id,user_name userName,`password` `passWord`,real_name realName,sno substationNo, "+
				" '' idCard,'' phone,'' courierNo,'' queueName,create_time registTime,'' headImage,'' innerNo,'' innerPhone,`status`"+
				" from boss_user where user_name=?";
		List<Object> list  = new ArrayList<Object>();
		list.add(userName);
		Map<String,Object> u= dao.findFirst(sql1,list.toArray());
		if(u==null){
			u=	 dao.findFirst(sql,list.toArray());
		}
		User user =new User();
		if(u!=null){
			user.setStatus(u.get("status").toString());
			user.setSubstationNo(u.get("substationNo").toString());
			user.setUserName(userName);
		}
		return user;
		
	}

}
