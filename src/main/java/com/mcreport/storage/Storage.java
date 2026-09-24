package com.mcreport.storage;

import com.mcreport.MCReportPlugin;

import java.util.List;
import java.util.Map;

public interface Storage {

    void init(MCReportPlugin plugin);

    void reload();

    void createReport(String reportId, String reporterTag, String reporterId,
                      String reportedPlayer, String reason, String description,
                      String evidence, String channelId);

    void resolveReport(String reportId);

    void setActionTaken(String reportId, String action);

    String getReportActionTaken(String reportId);

    int getPlayerWarns(String playerName);

    void setPlayerWarns(String playerName, int warns);

    Map<String, Long> getMutes();

    void saveMute(String uuid, long expiryMillis);

    void removeMute(String uuid);

    String getReportReporterId(String channelId);

    Map<String, Object> getReport(String reportId);

    List<String> getPendingReportsList();

    int getTotalReports();

    int getPendingReports();

    int getResolvedReports();

    int getActionCount(String action);

    List<String> getPlayerReports(String playerName);

    void createAppeal(String appealId, String appellantTag, String appellantId,
                      String playerName, String reason, String evidence, String channelId);

    boolean hasPendingAppeal(String appellantId, String playerName);

    void setAppealDecision(String appealId, String decision, String moderatorTag, String note);

    String getAppealPlayer(String appealId);

    String getAppealAppellantId(String appealId);

    List<String> getPendingAppealsList();

    void close();
}