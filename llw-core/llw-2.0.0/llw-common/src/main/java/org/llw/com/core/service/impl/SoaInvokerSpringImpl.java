/*
 * 反射专用类
 */
package org.llw.com.core.service.impl;

import java.lang.reflect.Method;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.core.SpringAppContext;
import org.llw.com.core.service.SoaInvoker;
import org.llw.com.exception.AppException;
import org.llw.com.exception.ErrorCode;
import org.llw.com.exception.SysException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.extern.slf4j.Slf4j;

@Service("soaInvoker")
@Slf4j
public class SoaInvokerSpringImpl implements SoaInvoker {


	@Override
	public AppResult invoke(AppParam context) {
		Status s = beginTx(context);
		AppResult resultContext = null;
		try {
			Object service = SpringAppContext.getBean(context.getService());
			Method method = service.getClass().getMethod(context.getMethod(),new Class[] { AppParam.class });
			
			resultContext = (AppResult) method.invoke(service,new Object[] { context });
		} catch (Exception e) {
			log.error("invoke Exception:" + context.toJson(),e);
			if (s != null) {
				s.needRollback = true;
			}
			if (e instanceof AppException) {
				throw (AppException)e;
			}else if (e instanceof SysException) {
				throw (SysException)e;
			}else if(e.getCause() instanceof AppException){
				throw (AppException)e.getCause();
			}else if(e.getCause() instanceof SysException){
				throw (SysException)e.getCause();
			}else{
				
				throw new SysException("Service invoke error:" + e.getMessage());
			}
			
		} finally {
			endTx(s);
		}
		return resultContext;
	}


	private Status beginTx(AppParam context) {
		Status s = new SoaInvokerSpringImpl.Status();
		Integer txType = (Integer) context.removeAttr("transactionType");
		if (txType == null) {
			s.canTx = false;
			return s;
		}
		log.info("**SoaManage Call by TxType[" + txType + "]" + 
				context.getService() +"." + context.getMethod());
		String tm = "transactionManager";
		if(context.getDataBase() != null && !context.getDataBase().trim().isEmpty()){
			tm = context.getDataBase() + tm;
		}
		PlatformTransactionManager txMgr = null;
		try{
			txMgr = (PlatformTransactionManager) SpringAppContext.getBean(tm);
		}catch (Exception e) {
			throw new SysException(ErrorCode.ERROR_DATABASE_ACCESS_100);
		}
		s.txManager = txMgr;
		s.def = new DefaultTransactionDefinition(txType.intValue());

		s.def.setIsolationLevel(2);
		s.canTx = true;
		if (txMgr != null) {
			try {
				s.tx = txMgr.getTransaction(s.def);
				log.info("**SoaManage Get New Transaction Success!" + 
						context.getService() +"." + context.getMethod());
			} catch (Exception e) {
				log.info("**SoaManage Get New Transaction Fail!" + context.toJson(), e);
				s.canTx = false;
				throw new SysException("get Trasaction error!" + e.getMessage());
			}
		} else {
			log.info("**SoaManage Get New Transaction Fail!" + 
					context.getService() +"." + context.getMethod());
			s.canTx = false;
		}
		return s;
	}

	private void endTx(Status s) {
		if (s.canTx) {
			if (s.needRollback) {
				log.info("**SoaManage Rollback Transaction!");
				s.txManager.rollback(s.tx);
			} else {
				log.info("**SoaManage Commit Transaction!");
				s.txManager.commit(s.tx);
			}
		}
	}
	
	
	
	@Override
	public AppResult busInvoke(AppParam param) {
		Object service = SpringAppContext.getBean(param.getService());
		Method method;
		try {
			method = service.getClass().getMethod(param.getMethod(),new Class[] { AppParam.class });
			return (AppResult) method.invoke(service,new Object[] { param });
		} catch (Exception e) {
			log.error("busInvoke error:{}", param.toJson(),e);
			if (e instanceof AppException) {
				throw (AppException)e;
			}else if (e instanceof SysException) {
				throw (SysException)e;
			}else {
				
				throw new SysException("busInvoke:" + e.getMessage());
			}
		}
	
	}


	private static class Status {
		public DefaultTransactionDefinition def;
		public TransactionStatus tx;
		public boolean canTx;
		public PlatformTransactionManager txManager;

		public boolean needRollback = false;
	}

}