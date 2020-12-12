package com.qiguliuxing.dts.wx.annotation.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.service.UserTokenManager;

public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
	public static final String LOGIN_TOKEN_KEY = "X-Dts-Token";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(Integer.class)
				&& parameter.hasParameterAnnotation(LoginUser.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request,
			WebDataBinderFactory factory) throws Exception {

		// return new Integer(1);
		String token = request.getHeader(LOGIN_TOKEN_KEY);
		if (token == null || token.isEmpty()) {
			return null;
		}

		return UserTokenManager.getUserId(token);
	}
}
