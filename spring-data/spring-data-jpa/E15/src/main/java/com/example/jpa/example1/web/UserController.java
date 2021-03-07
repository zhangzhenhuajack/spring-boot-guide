package com.example.jpa.example1.web;

import com.example.jpa.example1.User;
import com.example.jpa.example1.UserRepository;
import com.example.jpa.example1.util.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	@Autowired
	private UserRepository userRepository;
	/**
	 * @param user
	 * @return
	 */
	@PostMapping("/user")
	public User saveUser(@RequestBody User user) {
		return userRepository.save(user);
	}
	/**
	 * @param user
	 * @return
	 */
	@PostMapping("/user/notnull")
	public User saveUserNotNullProperties(@RequestBody User user) {
		//数据库里面取出最新的数据，当然了这一步严谨一点可以根据id和version来取数据，如果没取到可以报乐观锁异常
		User userSrc = userRepository.findById(user.getId()).get();
		//将不是null的字段copy到userSrc里面，我们只更新传递了不是null的字段
		PropertyUtils.copyNotNullProperty(user,userSrc);
		return userRepository.save(userSrc);
	}
}
