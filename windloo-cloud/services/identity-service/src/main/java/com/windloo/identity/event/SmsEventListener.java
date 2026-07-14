package com.windloo.identity.event;
import com.windloo.identity.sms.SmsSender;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SmsEventListener {
    private final SmsSender smsSender;
    public SmsEventListener(SmsSender smsSender) { this.smsSender = smsSender; }
    @Async @EventListener
    public void onUserCreated(UserCreatedEvent e) { smsSender.sendAsync(e.phone(), "您的初始密码：" + e.password()); }
    @Async @EventListener
    public void onPasswordReset(PasswordResetEvent e) { smsSender.sendAsync(e.phone(), "您的新密码：" + e.password()); }
    @Async @EventListener
    public void onResetCode(ResetCodeEvent e) { smsSender.sendAsync(e.phone(), "您的验证码：" + e.code() + "，5分钟内有效"); }
}