package xyz.sparta_project.manjok.user.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.user.infrastructure.email.exception.EmailException;

import static xyz.sparta_project.manjok.user.infrastructure.email.exception.EmailErrorCode.EMAIL_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	private final JavaMailSender mailSender;

	@Value("${backend.base-url}")
	private String backendBaseUrl;

	@Value("${frontend.base-url}")
	private String frontendBaseUrl;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendVerificationEmail(String toEmail, String token) {
		String subject = "이메일 인증을 완료해주세요";
		String verificationUrl = backendBaseUrl + "/v1/auth/verify-email?token=" + token;

		String body = createEmailHtml(
				"이메일 인증",
				"안녕하세요!<br><br>회원가입을 완료하시려면 아래 버튼을 클릭해주세요.",
				verificationUrl,
				"이메일 인증하기",
				"이 링크는 24시간 동안 유효합니다.<br>본인이 요청하지 않았다면 이 메일을 무시하세요.",
				"#4F46E5"
		);

		sendEmail(toEmail, subject, body);
	}

	public void sendPasswordResetEmail(String toEmail, String token) {
		String subject = "비밀번호 재설정";
		String resetUrl = frontendBaseUrl + "/v1/auth/reset-password?token=" + token;

		String body = createEmailHtml(
				"비밀번호 재설정",
				"안녕하세요!<br><br>비밀번호 재설정을 요청하셨습니다.<br>아래 버튼을 클릭하여 새 비밀번호를 설정해주세요.",
				resetUrl,
				"비밀번호 재설정하기",
				"이 링크는 1시간 동안 유효합니다.<br>요청하지 않으셨다면 이 메일을 무시하세요.",
				"#EF4444"
		);

		sendEmail(toEmail, subject, body);
	}

	private void sendEmail(String to, String subject, String body) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);

			mailSender.send(message);
		} catch (Exception e) {
			throw new EmailException(EMAIL_ERROR);
		}
	}

	private String createEmailHtml(
			String title,
			String content,
			String buttonUrl,
			String buttonText,
			String footer,
			String buttonColor
	) {
		return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 20px; font-family: 'Apple SD Gothic Neo', sans-serif; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    
                    <div style="background-color: %s; padding: 30px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">%s</h1>
                    </div>
                    
                    <div style="padding: 40px 30px;">
                        <p style="font-size: 16px; color: #333333; line-height: 1.6;">
                            %s
                        </p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" 
                               style="display: inline-block;
                                      background-color: %s;
                                      color: #ffffff;
                                      text-decoration: none;
                                      padding: 14px 30px;
                                      border-radius: 5px;
                                      font-weight: bold;">
                                %s
                            </a>
                        </div>
                        
                        <p style="font-size: 14px; color: #666666; line-height: 1.6;">
                            %s
                        </p>
                    </div>
                    
                    <div style="background-color: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #999999;">
                        © 2025 배달의 만족. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(buttonColor, title, content, buttonUrl, buttonColor, buttonText, footer);
	}
}
