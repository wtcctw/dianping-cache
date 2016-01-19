package com.dianping.squirrel.common.zookeeper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PathProvider {

    protected Map<String, String> templateMap;
    
    public PathProvider() {
        this.templateMap = new LinkedHashMap<String, String>();
    }

    public void addTemplate(String templateName, String template) {
        checkNotNull(templateName, "template name is null");
        checkNotNull(template, "template is null");
        templateMap.put(templateName, template);
    }
    
    public Collection<String> getTemplates() {
        return templateMap.values();
    }
    
    public String getTemplate(String templateName) {
        return templateMap.get(templateName);
    }
    
    public String getRootPath() {
        return getPath("root");
    }
    
    public String getPath(String templateName, String... params) {
        checkNotNull(templateName, "template name is null");
        String template = templateMap.get(templateName);
        checkNotNull(template, "template is null");
        for(int i=0; i<params.length; i++) {
            template = template.replace("$"+i, params[i]);
        }
        return template;
    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder(256);
        buf.append("PathProvider: [\n");
        for(Map.Entry<String, String> template : templateMap.entrySet()) {
            buf.append('\t').append(template.getKey()).append(" => ").
                append(template.getValue()).append('\n');
        }
        buf.append("]");
        return buf.toString();
    }
    
}
