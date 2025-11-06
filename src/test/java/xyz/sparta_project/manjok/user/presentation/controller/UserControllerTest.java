package xyz.sparta_project.manjok.user.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("회원가입 시도")
	void signUp() throws Exception {
		String requestBody = """
				{
					"username": "test1234",
					"password": "aAbB1234?",
					"confirmPassword" : "aAbB1234?",
					"email": "test1234@gmail.com"
				}
				""";

		mockMvc.perform(post("/v1/auth/signup")
					   .contentType(MediaType.APPLICATION_JSON)
					   .content(requestBody))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.message").value("회원가입 성공! 로그인은 이메일 인증 후 가능합니다. 24시간 이내에 이메일 인증을 완료해주세요."));
	}

}