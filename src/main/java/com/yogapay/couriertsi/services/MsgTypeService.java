package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.MsgType;
import com.yogapay.couriertsi.utils.Dao;

/**
 * 推送消息类型service
 * @author 
 *
 */
@Service
public class MsgTypeService {
	
	@Resource
	private Dao dao ;
	
	 public List<MsgType> getChildren(int msgCode) throws SQLException{
	        String sql = "select * from msg_type where parent_code=?";
	        return dao.find(MsgType.class, sql, msgCode);
	    }

	    public List<MsgType> getAllChildren(int msgCode) throws SQLException{
	        List<MsgType> all = new ArrayList<MsgType>();
	        List<MsgType> first = getChildren(msgCode);
	        if(null!=first&&first.size()>0){
	            all.addAll(first);
	            recurChildren(all,first);
	        }
	        return all;
	    }
	
	    private void recurChildren(List<MsgType> container,List<MsgType> msgTypeList) throws SQLException{
	        List<MsgType> temp = null;
	        for(MsgType msgType:msgTypeList){
	            if(null!=msgType){
	                temp =  getChildren(msgType.getMsgCode());
	                if(null!=temp&&temp.size()>0){
	                    container.addAll(temp);
	                    recurChildren(container,temp);
	                }
	            }
	        }
	    }

	    public String  getChildrenString(int msgCode) throws SQLException {
	           List<MsgType> list = getAllChildren(msgCode);
	      		String msgCodeString = String.valueOf(msgCode) ;
	      		for(MsgType tmp:list) {
	      			msgCodeString +=","+String.valueOf(tmp.getMsgCode());
				}
	      		return msgCodeString ;
	      	}
	    
	    
}
