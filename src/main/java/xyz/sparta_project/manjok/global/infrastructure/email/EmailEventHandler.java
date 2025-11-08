package xyz.sparta_project.manjok.global.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.sparta_project.manjok.global.infrastructure.email.exception.EmailException;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.EmailSentEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

import static xyz.sparta_project.manjok.global.infrastructure.email.exception.EmailErrorCode.INVALID_EMAIL_TYPE;

@Slf4j
@EventHandler(eventType = EmailSentEvent.class)
@RequiredArgsConstructor
public class EmailEventHandler implements EventHandlerProcessor<EmailSentEvent> {

	private final EmailService emailService;

	@Override
	public void handle(EmailSentEvent event) throws Exception {
		log.info("이메일 발송 이벤트 처리 시작: type={}, to={}",
				event.getEmailType(), event.getTo());

		try {
			switch (event.getEmailType()) {
				case VERIFICATION -> emailService.sendVerificationEmail(event.getTo(), event.getToken());
				case PASSWORD_RESET -> emailService.sendPasswordResetEmail(event.getTo(), event.getToken());
				default -> throw new EmailException(INVALID_EMAIL_TYPE);
			}

			log.info("이메일 발송 성공: type={}, to={}", event.getEmailType(), event.getTo());
		} catch (Exception e) {
			log.error("이메일 발송 실패: type={}, to={}", event.getEmailType(), event.getTo(), e);
			throw e;
		}
	}
}
