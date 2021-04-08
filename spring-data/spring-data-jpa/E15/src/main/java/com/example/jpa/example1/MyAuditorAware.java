package com.example.jpa.example1;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
public class MyAuditorAware implements AuditorAware<Integer> {
	/**
	 * Returns the current auditor of the application.
	 *
	 * @return the current auditor
	 */
	@Override
	public Optional<Integer> getCurrentAuditor() {
//		ServletRequestAttributes servletRequestAttributes =
//				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		Integer userId = (Integer) servletRequestAttributes.getRequest().getSession().getAttribute("userId");

		return Optional.ofNullable(1);
	}
}
