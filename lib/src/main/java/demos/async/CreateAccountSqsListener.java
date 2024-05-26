package demos.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class CreateAccountSqsListener {


	private String mailFrom;

	private String sendMailQueue;

	private String createAccountDlq;



}
