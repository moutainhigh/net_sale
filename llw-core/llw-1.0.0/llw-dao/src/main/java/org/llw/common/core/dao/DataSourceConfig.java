package org.llw.common.core.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor;

@Component
@Configuration
@ConfigurationProperties(prefix = "jdbc")
@ConditionalOnProperty(name = "jdbc.url")
public class DataSourceConfig {
		
		static final String MAPPER_LOCATION = "classpath*:mybatis/**/*.xml";
		
	    private String url;
	 
	    private String username;
	 
	    private String password;
	 
	    private String driverClass;
	    
	    private int initialSize;
	    
	    private int minIdle;
	    
	    private int maxActive;
	 
	    @Bean(name = "dataSource")
	    @Primary
	    public DataSource masterDataSource() {
	        DruidDataSource dataSource = new DruidDataSource();
	        dataSource.setDriverClassName(driverClass);
	        dataSource.setUrl(url);
	        dataSource.setUsername(username);
	        dataSource.setPassword(password);
	        List<String> initSqls = new ArrayList<String>();
	        initSqls.add("set names utf8mb4");
	        dataSource.setConnectionInitSqls(initSqls);
	        dataSource.setTimeBetweenEvictionRunsMillis(60000);
	        dataSource.setValidationQuery("SELECT 'x'");
	        //初始化配置
	        dataSource.setInitialSize(initialSize);
	        dataSource.setMinIdle(minIdle);
	        dataSource.setMaxActive(maxActive == 0 ? 20 : maxActive);
	        
	        return dataSource;
	    }
	 
	    @Bean(name = "transactionManager")
	    @Primary
	    public DataSourceTransactionManager masterTransactionManager() {
	        return new DataSourceTransactionManager(masterDataSource());
	    }
	 
	    @Bean(name = "sqlSessionFactory")
	    @Primary
	    public SqlSessionFactoryBean masterSqlSessionFactory(@Qualifier("dataSource") DataSource dataSource)
	            throws Exception {
	        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
	        sessionFactory.setDataSource(dataSource);
	        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
	                .getResources(DataSourceConfig.MAPPER_LOCATION));
	        sessionFactory.setTypeAliasesPackage("org.llw.common.core.dao.handler");
	        sessionFactory.setVfs(SpringBootVFS.class);
	        sessionFactory.setPlugins(new OffsetLimitInterceptor[] {
	        	getOffsetLimitInterceptor()
	        });
	        return sessionFactory;
	    }
	    
	    OffsetLimitInterceptor getOffsetLimitInterceptor() {
	    	OffsetLimitInterceptor interceptor = new OffsetLimitInterceptor();
	    	interceptor.setDialectClass("com.github.miemiedev.mybatis.paginator.dialect.MySQLDialect");
	    	return interceptor;
	    }

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDriverClass() {
			return driverClass;
		}

		public void setDriverClass(String driverClass) {
			this.driverClass = driverClass;
		}

		public int getInitialSize() {
			return initialSize;
		}

		public void setInitialSize(int initialSize) {
			this.initialSize = initialSize;
		}

		public int getMinIdle() {
			return minIdle;
		}

		public void setMinIdle(int minIdle) {
			this.minIdle = minIdle;
		}

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}
	    
	    
}
