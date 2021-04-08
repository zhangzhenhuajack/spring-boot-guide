package com.example.jpa.example1;

import com.example.jpa.example1.configuration.JpaConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@Import(JpaConfiguration.class)
public class UserInfoRepositoryTest {
	@Autowired
	private UserInfoRepository userInfoRepository;

	@Test
	public void testVersion() {
		userInfoRepository.save(UserInfo.builder().ages(20).telephone("1233456").build());
		UserInfo userInfo = userInfoRepository.getOne(1L);
		System.out.println(userInfo.toString());
		Assertions.assertEquals(0,userInfo.getVersion());
		Assertions.assertEquals(20,userInfo.getAges());
//		userInfo.setAges(30);
		userInfoRepository.saveAndFlush(userInfo);

		UserInfo u2 = userInfoRepository.getOne(1L);
		System.out.println(u2);
//		Assertions.assertEquals(1,u2.getVersion());
//		Assertions.assertEquals(30,u2.getAges());
	}
}
