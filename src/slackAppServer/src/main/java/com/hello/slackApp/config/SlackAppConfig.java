package com.hello.slackApp.config;

import com.hello.slackApp.service.*;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

@Configuration
@PropertySource("classpath:application.properties")
public class SlackAppConfig {

    private final String token;
    private final String signingSecret;

    private final Logger log = LoggerFactory.getLogger(SlackAppConfig.class);

    @Autowired
    private ChatgptService chatgptService;

    @Autowired
    private GptCacheService gptCacheService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private LokiLogFetchService LokiLogFetchService;

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

        app.command("/monitor", (req, ctx) -> {

            SlashCommandPayload payload = req.getPayload();
            String userId = "<@" + payload.getUserId() + ">";
            String query = payload.getText();

            CompletableFuture<Void> responseFuture = CompletableFuture.runAsync(() -> {
                try {
                    ctx.respond(r -> r.responseType("in_channel").text(WAIT_MESSAGE));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String gptResponse = null;
                try {
                    gptResponse = gptCacheService.getCachedResponse(query);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
                if(gptResponse == null) {
                    try{
                        String gptPrompt = "앞의 요청 내용과 일치하는 promQL을 알려줘. Only answer promQl please. No need other explanations. Don’t even say yes. promQl에서 pod 이름을 전달하는 key는 pod_name에서 pod으로 변경됐다는 점에 유의해.";
                        gptResponse = chatgptService.processSearch(query + gptPrompt);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
                String metricResult = null;
                try {
                    metricResult = prometheusService.processQuery(gptResponse);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                String dashboardUrl = null;
                try {
                    dashboardUrl = grafanaService.getDashboardUrl(gptResponse);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info("alert send with {}, {}", metricResult, dashboardUrl);
                slackAlertService.sendSlackNotificationMonitor(userId, query, gptResponse, metricResult, dashboardUrl);
                if (metricResult!=null){
                    try {
                        gptCacheService.setCachedResponse(query, gptResponse);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            });
            return ctx.ack();
        });

        app.command("/alert-metric", (req, ctx)->{
            SlashCommandPayload payload = req.getPayload();
            String query = payload.getText();
        
            String[] alert = query.split(" ");
            if (alert[0].equals("insert")){
                schedulerService.addToDatabase(alert);
                ctx.respond(r -> r.responseType("in_channel").text(":white_check_mark: Alert Metric이 정상적으로 등록 완료되었습니다."));
            }
            if (alert[0].equals("delete")){
                schedulerService.removeFromDatabase(alert[1]);
                ctx.respond(r -> r.responseType("in_channel").text(":white_check_mark: Alert Metric이 정상적으로 삭제 완료되었습니다."));
            }
        
            return ctx.ack();
        });

        app.command("/alert-loki", (req, ctx)->{
            SlashCommandPayload payload = req.getPayload();
            String query = payload.getText();

        
            String[] loki = query.split(" ");
            if (loki[0].equals("insert")){
                LokiLogFetchService.addToDatabase(loki[1]);
                ctx.respond(r -> r.responseType("in_channel").text(":white_check_mark: Monitoring App이 정상적으로 등록 완료되었습니다."));
            }
            if (loki[0].equals("delete")){
                LokiLogFetchService.removeFromDatabase(loki[1]);
                ctx.respond(r -> r.responseType("in_channel").text(":white_check_mark: Monitoring App이 정상적으로 삭제 완료되었습니다."));
            }
        
            return ctx.ack();
        });

        return app;
    }
}
