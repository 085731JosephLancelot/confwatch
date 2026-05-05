package com.confwatch.action;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the configured actions (webhooks and reload commands) to trigger on a file change event.
 */
public class ActionConfig {

    private List<String> webhookUrls = new ArrayList<>();
    private List<String> reloadCommands = new ArrayList<>();

    public ActionConfig() {}

    public ActionConfig(List<String> webhookUrls, List<String> reloadCommands) {
        this.webhookUrls = webhookUrls != null ? new ArrayList<>(webhookUrls) : new ArrayList<>();
        this.reloadCommands = reloadCommands != null ? new ArrayList<>(reloadCommands) : new ArrayList<>();
    }

    public List<String> getWebhookUrls() {
        return webhookUrls;
    }

    public void setWebhookUrls(List<String> webhookUrls) {
        this.webhookUrls = webhookUrls != null ? new ArrayList<>(webhookUrls) : new ArrayList<>();
    }

    public void addWebhookUrl(String url) {
        this.webhookUrls.add(url);
    }

    public List<String> getReloadCommands() {
        return reloadCommands;
    }

    public void setReloadCommands(List<String> reloadCommands) {
        this.reloadCommands = reloadCommands != null ? new ArrayList<>(reloadCommands) : new ArrayList<>();
    }

    public void addReloadCommand(String command) {
        this.reloadCommands.add(command);
    }

    @Override
    public String toString() {
        return "ActionConfig{webhookUrls=" + webhookUrls + ", reloadCommands=" + reloadCommands + "}";
    }
}
