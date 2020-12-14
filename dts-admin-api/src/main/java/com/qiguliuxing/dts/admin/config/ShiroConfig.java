package com.qiguliuxing.dts.admin.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.qiguliuxing.dts.admin.shiro.AdminAuthorizingRealm;
import com.qiguliuxing.dts.admin.shiro.AdminWebSessionManager;

@Configuration
public class ShiroConfig {

	@Bean
	public Realm realm() {
		return new AdminAuthorizingRealm();
	}

	/**
	 anon:例子/admins/**=anon 没有参数，表示可以匿名使用。
	 authc:例如/admins/user/**=authc表示需要认证(登录)才能使用，没有参数
	 roles(角色)：例子/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，例如admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。
	 perms（权限）：例子/admins/user/**=perms[user:add:*],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，例如/admins/user/**=perms["user:add:*,user:modify:*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法。
	 rest：例子/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user:method] ,其中method为post，get，delete等。
	 port：例子/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal://serverName:8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数。
	 authcBasic：例如/admins/user/**=authcBasic没有参数表示httpBasic认证
	 ssl:例子/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
	 user:例如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查
	 */
	@Bean
	public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

		// swagger接口权限 开放
//		filterChainDefinitionMap.put("/swagger-ui.html", "anon");
//		filterChainDefinitionMap.put("/webjars/**", "anon");
//		filterChainDefinitionMap.put("/v2/**", "anon");
//		filterChainDefinitionMap.put("/swagger-resources/**", "anon");

		// 认证登陆页面
		filterChainDefinitionMap.put("/admin/auth/login", "anon");
		filterChainDefinitionMap.put("/admin/auth/401", "anon");
		filterChainDefinitionMap.put("/admin/auth/index", "anon");
		filterChainDefinitionMap.put("/admin/auth/403", "anon");

		filterChainDefinitionMap.put("/admin/**", "authc");
		shiroFilterFactoryBean.setLoginUrl("/admin/auth/401");
		shiroFilterFactoryBean.setSuccessUrl("/admin/auth/index");
		shiroFilterFactoryBean.setUnauthorizedUrl("/admin/auth/403");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	@Bean
	public SessionManager sessionManager() {
		AdminWebSessionManager mySessionManager = new AdminWebSessionManager();
		return mySessionManager;
	}

	@Bean
	public DefaultWebSecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(realm());
		securityManager.setSessionManager(sessionManager());
		return securityManager;
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	public static DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
		creator.setProxyTargetClass(true);
		return creator;
	}
}
