package com.dianping.cache.scale.instance.docker;
import java.util.Iterator;

import com.dianping.squirrel.common.util.JsonUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.scale.instance.Instance;
import com.dianping.cache.scale.instance.Result;

public class DockerResultParse {
	
	private static Logger logger = LoggerFactory.getLogger(DockerResultParse.class);
	
	public static void parse(Result value,String response){
		OperateResult result = null;
		try {
			result = JsonUtils.fromStr(response, OperateResult.class); //parse json
			int status = result.getOperationStatus();
			if((status == 200) || (status == 500)){// 500 中也可能有成功的实例
				String xml = result.getLog();
				parseLogXml(value,xml);
				value.setStatus(status);
			}
		} catch (Exception e) {
			value.setStatus(500);
			logger.error("Parse OperateResult with error" + e);
		}
	}
	
	
	private static void parseLogXml(Result result,String xml) throws DocumentException{
		Document document = DocumentHelper.parseText(xml);
		Element root = document.getRootElement();
		
		for(Iterator<Element> iter = root.elementIterator("host-operation");iter.hasNext();){
			Element hostElement = iter.next();
			Attribute statusAttribute = hostElement.attribute("status");
			String status = statusAttribute.getText();
			if("200".equals(status)){
				Attribute ipAttribute = hostElement.attribute("instance-ip");
				String ip = ipAttribute.getText();
				Attribute idAttribute = hostElement.attribute("instance-id");
				String instanceId = idAttribute.getText();
				Attribute agentAttribute = hostElement.attribute("agent-ip");
				String agentip = agentAttribute.getText();
				Instance instance = new Instance(result.getAppId(),instanceId,ip,agentip);
				result.getInstances().add(instance);
			}
		}

	}
	
	static class OperateResult{
		private int operationStatus;
		private String msg;
		private String log;
		
		public int getOperationStatus() {
			return operationStatus;
		}
		public void setOperationStatus(int operationStatus) {
			this.operationStatus = operationStatus;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		public String getLog() {
			return log;
		}
		public void setLog(String log) {
			this.log = log;
		}
	}
}
