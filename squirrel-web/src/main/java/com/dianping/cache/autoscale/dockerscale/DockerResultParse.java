package com.dianping.cache.autoscale.dockerscale;

import java.util.Iterator;

import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.autoscale.Instance;
import com.dianping.cache.autoscale.Result;
import com.dianping.cache.util.RequestUtil;

public class DockerResultParse {
	
	private static Logger logger = LoggerFactory.getLogger(DockerResultParse.class);
	
	public static Result parse(Result value,String response){
		ObjectMapper objectMapper = new ObjectMapper();
		OperateResult result = null;
		Result res = new Result();
		try {
			result = objectMapper.readValue(response, OperateResult.class); //parse json
			res.setStatus(result.getOperationStatus());
			value.setStatus(result.getOperationStatus());
			if((res.getStatus() == 200) || (res.getStatus() == 500)){// 500 中也可能有成功的实例
				String xml = result.getLog();
				parseLogXml(res,xml);
				parseLogXml(value,xml);
			}
			
		} catch (Exception e) {
			res.setStatus(500);
			value.setStatus(500);
			logger.error("Parse OperateResult with error" + e);
		}
		return res;
	}
	
	
	private static void parseLogXml(Result res,String xml) throws DocumentException{
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
				Instance instance = new Instance(instanceId,ip,agentip);
				res.getInstances().add(instance);
			}
		}

	}
	
	
	public static void main(String[] agrs){
		String OPERATION_RESULT_URL = "http://10.3.21.21:8080/api/v1/operations/";
		String resultstr = RequestUtil.sendGet(OPERATION_RESULT_URL + 123, null);
		Result result = new Result();
		DockerResultParse.parse(result,resultstr);
		System.out.println(result.getInstances().size());
		for(Instance ins : result.getInstances()){
			System.out.println(ins.getIp() +" :  " +ins.getAgentip());
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
