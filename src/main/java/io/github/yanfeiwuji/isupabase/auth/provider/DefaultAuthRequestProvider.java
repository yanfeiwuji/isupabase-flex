package io.github.yanfeiwuji.isupabase.auth.provider;

import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.request.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 10:27
 */
@RequiredArgsConstructor
public class DefaultAuthRequestProvider implements AuthRequestProvider {

    @Override
    public Optional<AuthRequest> apply(String s, AuthConfig authConfig) {
        return Optional.ofNullable(s).map(it -> switch (it) {
            case "github" -> new AuthGithubRequest(authConfig);
            case "weibo" -> new AuthWeiboRequest(authConfig);
            case "gitee" -> new AuthGiteeRequest(authConfig);
            case "dingtalk" -> new AuthDingTalkRequest(authConfig);
            case "dingtalk_account" -> new AuthDingTalkAccountRequest(authConfig);
            case "baidu" -> new AuthBaiduRequest(authConfig);
            case "csdn" -> new AuthCsdnRequest(authConfig);
            case "coding" -> new AuthCodingRequest(authConfig);
            case "oschina" -> new AuthOschinaRequest(authConfig);
            case "alipay" -> new AuthAlipayRequest(authConfig);
            case "qq" -> new AuthQqRequest(authConfig);
            case "wechat_open" -> new AuthWeChatOpenRequest(authConfig);
            case "wechat_mp" -> new AuthWeChatMpRequest(authConfig);
            case "taobao" -> new AuthTaobaoRequest(authConfig);
            case "google" -> new AuthGoogleRequest(authConfig);
            case "facebook" -> new AuthFacebookRequest(authConfig);
            case "douyin" -> new AuthDouyinRequest(authConfig);
            case "linkedin" -> new AuthLinkedinRequest(authConfig);
            case "microsoft" -> new AuthMicrosoftRequest(authConfig);
            case "microsoft_cn" -> new AuthMicrosoftCnRequest(authConfig);
            case "mi" -> new AuthMiRequest(authConfig);
            case "toutiao" -> new AuthToutiaoRequest(authConfig);
            case "teambition" -> new AuthTeambitionRequest(authConfig);
            case "renren" -> new AuthRenrenRequest(authConfig);
            case "pinterest" -> new AuthPinterestRequest(authConfig);
            case "stack_overflow" -> new AuthStackOverflowRequest(authConfig);
            case "huawei" -> new AuthHuaweiRequest(authConfig);
            case "wechat_enterprise" -> new AuthWeChatEnterpriseQrcodeRequest(authConfig);
            case "wechat_enterprise_qrcode_third" -> new AuthWeChatEnterpriseThirdQrcodeRequest(authConfig);
            case "wechat_enterprise_web" -> new AuthWeChatEnterpriseWebRequest(authConfig);
            case "kujiale" -> new AuthKujialeRequest(authConfig);
            case "gitlab" -> new AuthGitlabRequest(authConfig);
            case "meituan" -> new AuthMeituanRequest(authConfig);
            case "eleme" -> new AuthElemeRequest(authConfig);
            case "twitter" -> new AuthTwitterRequest(authConfig);
            case "feishu" -> new AuthFeishuRequest(authConfig);
            case "jd" -> new AuthJdRequest(authConfig);
            case "aliyun" -> new AuthAliyunRequest(authConfig);
            case "xmly" -> new AuthXmlyRequest(authConfig);
            case "amazon" -> new AuthAmazonRequest(authConfig);
            case "slack" -> new AuthSlackRequest(authConfig);
            case "line" -> new AuthLineRequest(authConfig);
            case "okta" -> new AuthOktaRequest(authConfig);
            case "proginn" -> new AuthProginnRequest(authConfig);
            case "afdian" -> new AuthProginnRequest(authConfig);
            default -> null;
        });
    }

    //  out put all impl is auth
//    public static void main(String[] args) {
//        Arrays.stream(AuthDefaultSource.values()).map(it -> {
//            return "case \"%s\" -> new %s(authConfig);".formatted(it.name().toLowerCase(), it.getTargetClass().getSimpleName());
//        }).forEach(System.out::println);
//    }
}
