package xyz.sparta_project.manjok.user.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RolePromotionServiceTest {
	@Autowired
	RolePromotionService service;

	@Test
	void sendRolePromotionRequest() {

	}
}