package xyz.sparta_project.manjok.global.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.global.infrastructure.email.exception.EmailException;

import static xyz.sparta_project.manjok.global.infrastructure.email.exception.EmailErrorCode.EMAIL_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	private final JavaMailSender mailSender;

	@Value("${backend.base-url}")
	private String backendBaseUrl;

//	@Value("${frontend.base-url}")
    @Value("${backend.base-url}")
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
        String resetUrl = frontendBaseUrl + "/view/client/reset?token=" + token;

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

    private String createPasswordEmailHtml(String title, String message, String linkUrl, String buttonText, String footer, String buttonColor) {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f5f5f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                            <!-- 헤더 -->
                            <tr>
                                <td style="padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid #eee;">
                                    <h1 style="margin: 0; font-size: 28px; color: #222; font-weight: 700;">배달의 만족</h1>
                                </td>
                            </tr>
                            <!-- 본문 -->
                            <tr>
                                <td style="padding: 40px;">
                                    <h2 style="margin: 0 0 20px; font-size: 22px; color: #333; font-weight: 600;">%s</h2>
                                    <p style="margin: 0 0 30px; font-size: 15px; color: #666; line-height: 1.6;">%s</p>
                                    
                                    <!-- 버튼 (새 창으로 열기) -->
                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td align="center" style="padding: 20px 0;">
                                                <a href="%s" 
                                                   target="_blank"
                                                   onclick="window.open(this.href, 'passwordReset', 'width=500,height=700,scrollbars=yes,resizable=yes'); return false;"
                                                   style="display: inline-block; padding: 14px 40px; background-color: %s; color: white; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 2px 8px rgba(0,0,0,0.15);">
                                                    %s
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                    
                                    <p style="margin: 30px 0 0; font-size: 13px; color: #999; line-height: 1.6;">%s</p>
                                </td>
                            </tr>
                            <!-- 푸터 -->
                            <tr>
                                <td style="padding: 30px 40px; background-color: #f9f9f9; border-top: 1px solid #eee; text-align: center; border-radius: 0 0 12px 12px;">
                                    <p style="margin: 0; font-size: 13px; color: #999;">
                                        이 메일은 발신 전용입니다.<br/>
                                        © 2025 배달의 만족. All rights reserved.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(title, title, message, linkUrl, buttonColor, buttonText, footer);
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
