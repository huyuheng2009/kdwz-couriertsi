package com.yogapay.couriertsi.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yogapay.couriertsi.domain.OrderPic;
import com.yogapay.couriertsi.domain.User;
import com.yogapay.couriertsi.utils.Dao;
import com.yogapay.couriertsi.utils.DateUtils;
import com.yogapay.couriertsi.utils.StringUtils;

/**
 * 订单图片service
 * @author 
 *
 */
@Service
public class OrderPicService {

	@Resource
	private Dao dao ;
	
	
	public boolean save( List<OrderPic> imagesList) throws SQLException {
		String sql = "insert into order_pic(order_no,file_type,file_name,file_size,file_path,file_uri,orginal_name) values(?,?,?,?,?,?,?)";
		Object[][] list = new Object[imagesList.size()][7];
		for(int i=0;i<imagesList.size();i++){
        	list[i][0]=imagesList.get(i).getOrderNo() ;
        	list[i][1]=imagesList.get(i).getFileType();
        	list[i][2]=imagesList.get(i).getFileName();
            list[i][3]=imagesList.get(i).getFileSize();
        	list[i][4]=imagesList.get(i).getFilePath();
        	list[i][5]=imagesList.get(i).getFileUri() ;
        	list[i][6]=imagesList.get(i).getOrginalName();
          }
        dao.batchUpdate(sql,list);
		return true ;
	}
	
	
	
	public List<Map<String, Object>> list(Map<String, String> params) throws SQLException {
		 StringBuffer sql = new StringBuffer() ;
		 List<Object> list = new ArrayList<Object>();
		 sql.append( "select id,file_uri filePath from order_pic ") ;
		 sql.append(" where 1=1 ");
		 if (!StringUtils.isEmptyWithTrim(params.get("orderNo"))) {
			 sql.append("and order_no=? ") ;
			 list.add(params.get("orderNo"));
		 }
		return dao.find(sql.toString(), list.toArray()) ;
	}
	
	
	public boolean delById(String id) throws SQLException {
		String sql = "delete from order_pic where id=?";
		List<Object> list = new ArrayList<Object>();
		list.add(id) ;
		dao.update(sql, list.toArray());
		return true;
	}
	
	
	
	public boolean delPics(List<OrderPic> imagesList) throws SQLException {
		String sql = "delete from order_pic where id=? and order_no=? ";
		Object[][] list = new Object[imagesList.size()][2];
		for(int i=0;i<imagesList.size();i++){
        	list[i][0]=imagesList.get(i).getId() ;
        	list[i][1]=imagesList.get(i).getOrderNo();
          }
        dao.batchUpdate(sql,list);
		return true ;
	}
	
	
}
