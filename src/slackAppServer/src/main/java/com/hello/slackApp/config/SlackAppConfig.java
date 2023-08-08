package com.hello.slackApp.config;

import com.hello.slackApp.service.*;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class SlackAppConfig {

    private final String token;
    private final String signingSecret;
    @Autowired
    private ChatgptService chatgptService;

    @Autowired
    private GptCacheService gptCacheService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private LogFetcher LogFetcher;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private GrafanaService grafanaService;

    @Autowired
    private SlackAlertService slackAlertService;

    private static final String WAIT_MESSAGE = ":speech_balloon: 잠시만 기다려주세요. Monibot이 답변을 작성하고 있습니다.";

    public SlackAppConfig(Environment env) {
        this.token = env.getProperty("token");
        this.signingSecret = env.getProperty("signingSecret");
    }
    @Qualifier("bot")
    @Bean
    public App initSlackApp(){
        AppConfig appConfig = AppConfig.builder().singleTeamBotToken(token).signingSecret(signingSecret).build();
        App app = new App(appConfig);

        app.command("/monitor", (req, ctx)->{

            SlashCommandPayload payload = req.getPayload();

            String userId = "<@" + payload.getUserId() + ">";
            String query = payload.getText();
            ctx.respond(r -> r.responseType("in_channel").text(":question: " + userId + "님의 질문 : " + query));
            ctx.respond(r -> r.responseType("in_channel").text(WAIT_MESSAGE));

            String gptResponse = gptCacheService.getCachedResponse(query);
            if (gptResponse == null) {
                String gptPrompt ="앞의 요청 내용과 일치하는 promQL을 알려줘. Only answer promQl please. No need other explanations. Don’t even say yes. promQl에서 pod 이름을 전달하는 key는 pod_name에서 pod으로 변경됐다는 점에 유의해.";
                gptResponse = chatgptService.processSearch(query+gptPrompt);
                gptCacheService.setCachedResponse(query, gptResponse);
            }

            String finalGptResponse = gptResponse;
            String metricResult = prometheusService.processQuery(finalGptResponse);
            String dashboardUrl = grafanaService.getDashboardUrl(finalGptResponse);
            slackAlertService.sendSlackNotificationMonitor(finalGptResponse, metricResult, dashboardUrl);

            return ctx.ack();
        });

        app.command("/alert", (req, ctx)->{
            SlashCommandPayload payload = req.getPayload();
            String query = payload.getText();
            ctx.respond(r -> r.responseType("in_channel").text("Alert 정상 등록 완료 되었습니다."));
        
            String[] alert = query.split(" ");
            if (alert[0].equals("insert")){
                schedulerService.addToDatabase(alert);
            }
            if (alert[0].equals("delete")){
                schedulerService.removeFromDatabase(alert[1]);
            }
        
            return ctx.ack();
        });

        app.command("/alert-loki", (req, ctx)->{
            SlashCommandPayload payload = req.getPayload();
            String query = payload.getText();

            ctx.respond(r -> r.responseType("in_channel").text("Monitoring App이 정상 등록 완료 되었습니다."));
        
            String[] loki = query.split(" ");
            if (loki[0].equals("insert")){
                LogFetcher.addToDatabase(loki[1]);
            }
            if (loki[0].equals("delete")){
                LogFetcher.removeFromDatabase(loki[1]);
            }
        
            return ctx.ack();
        });

        return app;
    }
}
