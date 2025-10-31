package xyz.sparta_project.manjok.global.presentation.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandlerTest.TestSecurityConfig.class
})
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("GlobalException 발생 시 ErroResponse 반환")
    void handle_global_exception() throws Exception {
        // when & then
        mockMvc.perform(get("/test/global-exception"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/test/global-exception"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    @DisplayName("커스텀 메시지를 가진 GlobalException 처리")
    void handle_global_exception_with_custom_message() throws Exception {
        // when & then
        mockMvc.perform(get("/test/global-exception-custom"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_101"))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/global-exception-custom"));
    }
    
    @Test
    @DisplayName("MethodArgumentNotValidException 발 생 시 validation 에러 처리")
    void handle_method_argument_not_valid_exception() throws Exception {
        // given
        TestRequst invalidRequest = new TestRequst("", "invalid-email");
        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        // when & then
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_101"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/test/validation"));

    }

    @Test
    @DisplayName("BindException 발생 시 바인딩 에러 처리")
    void handle_bind_exception() throws Exception {
        // when & then
        mockMvc.perform(get("/test/bind-exception"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_101"))
                .andExpect(jsonPath("$.status").value(400));
        
    }
    
    @Test
    @DisplayName("일반 Exception 발생 시 500 에러 반환")
    void handle_generic_exception() throws Exception {
        // when & then
        mockMvc.perform(get("/test/exception"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/test/exception"));
    }
    
    @Test
    @DisplayName("다양한 HTTP 상태 코드의 GlobalException 처리")
    void handle_different_status_codes() throws Exception {
        // 401 Unauthorized
        mockMvc.perform(get("/test/unauthorized"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GLOBAL_200"))
                .andExpect(jsonPath("$.status").value(401));

        // 403 Forbidden
        mockMvc.perform(get("/test/forbidden"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GLOBAL_210"))
                .andExpect(jsonPath("$.status").value(403));

        // 404 Not Found
        mockMvc.perform(get("/test/not-found"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GLOBAL_300"))
                .andExpect(jsonPath("$.status").value(404));

    }

    @RestController
    static class TestController {
        @GetMapping("/test/global-exception")
        public void throwGllbalException() {
            throw new TestGlobalException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }

        @GetMapping("/test/global-exception-custom")
        public void throwGlobalExceptionWithCustomMessage() {
            throw new TestGlobalException(
                    GlobalErrorCode.INVALID_INPUT_VALUE,
                    "이메일 형식이 올바르지 않습니다."
            );
        }
        
        @PostMapping("/test/validation")
        public void validateRequest(@Valid @RequestBody TestRequst requst) {
            // validation이 실패하면 MethodArgumentNotValidException 발생
        }

        @GetMapping("/test/bind-exception")
        public void throwBindException() throws BindException {
            BindException bindException = new BindException(new TestRequst("",""), "testRequest");
            bindException.addError(new FieldError("testRequst", "name", "이름은 필수입니다."));
            throw bindException;
        }

        @GetMapping("/test/exception")
        public void throwGenericException() {
            throw new RuntimeException("예상치 못한 에러");
        }

        @GetMapping("/test/unauthorized")
        public void throwUnauthorizedException() {
            throw new TestGlobalException(GlobalErrorCode.UNAUTHORIZED);
        }

        @GetMapping("/test/forbidden")
        public void throwForbiddenException() {
            throw new TestGlobalException(GlobalErrorCode.FORBIDDEN);
        }

        @GetMapping("/test/not-found")
        public void throwNotFoundException() {
            throw new TestGlobalException(GlobalErrorCode.RESOURCE_NOT_FOUND);
        }


    }

    static class TestGlobalException extends GlobalException {
        public TestGlobalException(ErrorCode errorCode) {
            super(errorCode);
        }

        public TestGlobalException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }

    static class TestRequst {
        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        public TestRequst(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());  // 모든 요청 허용
            return http.build();
        }
    }


}