package org.llw.common.core.dao.handler;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.llw.com.util.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("rawtypes")
@Slf4j
public class DateHandler implements TypeHandler {
	
	@Override
	public void setParameter(PreparedStatement ps, int i, Object parameter,
			JdbcType jdbcType) throws SQLException {
		if(parameter != null && !"".equals(parameter)){
			if (parameter instanceof String) {
				String date = ((String) parameter).trim();
				int length = date.length();
				if(date.indexOf(".")>0){
					date = date.replace(".", "-");
				}
				try{
					if(length == 10){
						ps.setDate(i, new java.sql.Date(DateTimeUtil.toDateByString(date,
								DateTimeUtil.DATE_PATTERN_YYYY_MM_DD).getTime()));
					}else if(length==19){
						ps.setTimestamp(i, new java.sql.Timestamp(
								DateTimeUtil.toDateByString(date,DateTimeUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS).getTime()));
					}else{
						date = date.replaceAll("(\\d{4}).{0,1}(\\d{2}).{0,1}(\\d{2})(.*)", "$1-$2-$3$4");
						ps.setTimestamp(i, Timestamp.valueOf(date));
					}
				}catch(java.lang.IllegalArgumentException e){
					log.error("  date is invalid:{}", date,e);
					ps.setTimestamp(i, null);
				}
			} else if (parameter instanceof java.util.Date) {
				java.util.Date date = (java.util.Date) parameter;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateStr = sdf.format(date);
				if (dateStr.indexOf("00:00:00") != -1)
					ps.setDate(i,
							new Date(((java.util.Date) parameter).getTime()));
				else
					ps.setTimestamp(i, new Timestamp(
							((java.util.Date) parameter).getTime()));
			} else if (parameter instanceof Date)
				ps.setDate(i, (Date) parameter);
			else if (parameter instanceof Timestamp)
				ps.setTimestamp(i, (Timestamp) parameter);
		}else{
			ps.setTimestamp(i, null);
		}
	}

	@Override
	public Object getResult(ResultSet rs, String columnName)
			throws SQLException {
		Object o =  rs.getObject(columnName);
		if(o instanceof Date){
			return (Date)o;
		}else if(o instanceof Timestamp){
			return (Timestamp)o;
		}else{
			return o;
		}
	}
	
	@SuppressWarnings("unused")
	private static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	@Override
	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		Object o =  rs.getObject(columnIndex);
		if(o instanceof Date){
			return (Date)o;
		}else if(o instanceof Timestamp){
			return (Timestamp)o;
		}else{
			return o;
		}
	}

	@Override
	public Object getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		Object o =  cs.getObject(columnIndex);
		if(o instanceof Date){
			return (Date)o;
		}else if(o instanceof Timestamp){
			return (Timestamp)o;
		}else{
			return o;
		}
	}
}