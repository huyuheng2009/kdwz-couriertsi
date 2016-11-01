package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.PushMsg;
import com.yogapay.couriertsi.dto.MsgDto;
import com.yogapay.couriertsi.dto.MsgInfoDto;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.StringUtils;

/**
 * 消息通知service
 * @author 
 *
 */
@Service
public class MsgService {

	@Resource
	private Dao dao ;
	
      public long save(PushMsg msg) throws SQLException {
  		String sql = "insert into push_msg(user_no,user_type,msg_code,msg_content,msg_data,readed,create_time,expire_time) values(?,?,?,?,?,?,?,?)";
  		List<Object> list = new ArrayList<Object>();
        list.add(msg.getUserNo());
        list.add(msg.getUserType());
        list.add(msg.getMsgCode());
        list.add(msg.getMsgContent());
        list.add(msg.getMsgData());
        list.add(msg.isReaded());
        list.add(msg.getCreateTime());
        list.add(msg.getExpireTime());
  		return dao.updateGetID(sql, list) ;
	}
	
	
	public Page<MsgDto> list(Map<String, String> params,PageRequest pageRequest) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 sql.append("select m.*,t.jump_type from push_msg m,msg_type t where 1=1 and m.msg_code=t.msg_code") ;
		 List<Object> list = new ArrayList<Object>();
		 if (!StringUtils.isEmptyWithTrim(params.get("userNo"))) {
			sql.append(" and m.user_no=? ") ;
			list.add(params.get("userNo")) ;
		 }
		 if (!StringUtils.isEmptyWithTrim(params.get("userType"))) {
				sql.append(" and m.user_type=? ") ;
				list.add(params.get("userType")) ;
		 }
		 if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
				sql.append(" and m.create_time>=? ") ;
				list.add(params.get("beginTime")) ;
			 }
		 if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
					sql.append(" and m.create_time<? ") ;
					list.add(params.get("endTime")) ;
			 }
		 if (!StringUtils.isEmptyWithTrim(params.get("msgCode"))) {
				sql.append(" and m.msg_code in ("+params.get("msgCode")+")") ;
		 }
		 sql.append("order by m.readed asc ,m.create_time desc") ;
		return dao.find(MsgDto.class,sql.toString(), list.toArray(), pageRequest) ;
	}
	
	public boolean read(String userNo,String msgId) throws SQLException {
		StringBuffer str= new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		str.append( "update  push_msg set readed=true where user_no=?  and   id=?");		
		list.add(userNo) ;
		list.add(msgId) ;
		dao.update(str.toString(), list.toArray());
		return true ;
	}
	
	public boolean delById(String userNo,String msgIds) throws SQLException {
		StringBuffer str= new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		str.append( "delete from push_msg  where user_no=?  and   id in ("+msgIds+")");		
		list.add(userNo) ;
		//list.add(msgIds) ;
		dao.update(str.toString(), list.toArray());
		return true ;
	}
	
	public boolean delAll(String userNo,String msgCode) throws SQLException {
		StringBuffer str= new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		str.append( "delete from push_msg  where user_no=?  and msg_code in( "+	msgCode+")");	
		list.add(userNo) ;
		//list.add(msgIds) ;
		dao.update(str.toString(), list.toArray());
		return true ;
	}
	
	public boolean readAll(String userNo,String msgCode) throws SQLException {
		StringBuffer str= new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		str.append( "update  push_msg set readed=true where user_no=?  and msg_code in( "+	msgCode+")");
		list.add(userNo) ;
		dao.update(str.toString(), list.toArray());
		return true ;
	}
	

	public MsgInfoDto info(String msgId) throws SQLException {
		String sql = "select * from push_msg where id=? ";
		List<Object> list = new ArrayList<Object>();
		list.add(msgId) ;
		return dao.findFirst(MsgInfoDto.class, sql, list.toArray()) ;
	}
	
	
	public int ureadCount(Map<String, String> params) throws SQLException {
		StringBuffer sql = new StringBuffer() ;
		sql.append("select count(*) c from push_msg where user_type=2 and readed=0 ") ;
		List<Object> list = new ArrayList<Object>();
		if (!StringUtils.isEmptyWithTrim(params.get("userNo"))) {
			sql.append(" and user_no=? ") ;
			list.add(params.get("userNo")) ;
		 }
		 if (!StringUtils.isEmptyWithTrim(params.get("beginTime"))) {
				sql.append(" and create_time>=? ") ;
				list.add(params.get("beginTime")) ;
			 }
		 if (!StringUtils.isEmptyWithTrim(params.get("endTime"))) {
					sql.append(" and create_time<? ") ;
					list.add(params.get("endTime")) ;
			 }
		 if (!StringUtils.isEmptyWithTrim(params.get("msgCode"))) {
				sql.append(" and msg_code in ("+params.get("msgCode")+")") ;
		 }
		Map<String, Object> ret = dao.findFirst(sql.toString(), list.toArray()) ;
		return Integer.parseInt(ret.get("c").toString()) ;
	}
	
	
}
