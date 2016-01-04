package com.dianping.cache.scale.instance.docker;

import java.util.HashMap;
import java.util.Map;

import com.dianping.squirrel.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.instance.AppId;
import com.dianping.cache.scale.instance.Apply;
import com.dianping.cache.scale.instance.Instance;
import com.dianping.cache.scale.instance.Result;
import com.dianping.cache.scale.instance.docker.DockerResultParse.OperateResult;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.RequestUtil;
import org.springframework.dao.DuplicateKeyException;

public class DockerApply implements Apply{


	private static Logger logger = LoggerFactory.getLogger(DockerApply.class);
	
	private static final String SCALE_URL = "http://10.3.21.21:8080/api/v1/apps/";
	
	private static final String OPERATION_RESULT_URL = "http://10.3.21.21:8080/api/v1/operations/";
	
	private static final String DOCKER_SHUTDOWN_URL = "http://10.3.21.21:8080/api/v1/instances/app/";
	
	private static final String DOCKER_REMOVE_URL = "http://10.3.21.21:8080/api/v1/instances/app/";
	
	private static final Map<Integer,Result> operationResultMap = new HashMap<Integer,Result>();
	
	private ServerService serverService = SpringLocator.getBean("serverService");
	
	
	@Override
	public int apply(AppId appId, int number) {
		logger.info("Apply instance,appId : {},number : {}",appId,number);
		int operationid = sendScaleRequest(appId.toString(),number);
		Result value = new Result();
		value.setAppId(appId);
		value.setNeed(number);
		operationResultMap.put(operationid, value);
		operate(value, operationid);
		return operationid;
	}

	@Override
	public void destroy(String address) {
		Server server = serverService.findByAddress(address);
		if(server != null){
			destroy(server.getAppId(),server.getInstanceId());
		}
	}
	
	@Override
	public void destroy(Result value) {
		if(value == null || value.getInstances().size() == 0)
			return;
		String[] instances = new String[value.getInstances().size()];
		int index = 0;
		for(Instance ins : value.getInstances()){
			instances[index++] = ins.getInstanceId();
		}
		destroy(value.getAppId().toString(),instances);
	}
	
	@Override
	public Result getValue(int operationId) {
		return operationResultMap.get(operationId);
	}
	
	
	private void operate(final Result value,final int operationId){
		Runnable runnable = new Runnable() {
			@Override
			public void run(){
				while(true){
					String resultstr = RequestUtil.sendGet(OPERATION_RESULT_URL + operationId, null);
					DockerResultParse.parse(value,resultstr);
					if(value.getStatus() == 200){
						logger.info("Apply instance success:");
						for(Instance ins : value.getInstances()){
							logger.info("instanceId : {} , ip : {}, agentIp : {}",new Object[]{ins.getInstanceId(),ins.getIp(),ins.getAgentIp()});

							try {
								serverService.insert(ins.getIp()+":"+ins.getAppId().getPort(),
                                        ins.getAppId().toString(), ins.getInstanceId(),
                                        -2,ins.getAgentIp());// -2 未分配
							} catch (DuplicateKeyException e) {
								logger.error("DuplicateKeyException , ip : {} , update with new.",ins.getIp());
								serverService.delete(ins.getIp()+":"+ins.getAppId().getPort());
								serverService.insert(ins.getIp()+":"+ins.getAppId().getPort(),
										ins.getAppId().toString(), ins.getInstanceId(),
										-2,ins.getAgentIp());
							}
						}
						break;
					}else if(value.getStatus() == 500){
						logger.error("Apply instance failed!");
						destroy(value);
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						logger.error("Apply instance failed! Refresh Dockerscale Result with InterruptedException :" + e);
					}
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	@Override
	public void destroy(final String appId,final String... instances) {
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				//update servers database ,set type = -1
				for(String instance : instances){
					serverService.setDeleteType(instance);
				}
				String requestUrl = DOCKER_SHUTDOWN_URL + appId + "/shutdown";
				InstanceIdParam instanceIdParam = new InstanceIdParam();
				instanceIdParam.setInstances(instances);
				String paras = null;
				try {
					paras = JsonUtils.toStr(instanceIdParam);
				} catch (Exception e) {
					logger.error("ShutDown docker container failed,caused by Convert instanceIdParam to json error : "
							+ e);
				}
				// send shutdown request
				String opIdStr = RequestUtil.sendPost(requestUrl, paras);
				int operationid = -1;
				try {
					OperationId opid;
					opid = JsonUtils.fromStr(opIdStr,OperationId.class);
					operationid = opid.getOperationId();
				} catch (Exception e) {
					logger.error("ShutDown docker container failed,cause by Parse operationid with error !" + e);
				}
				while (true) {
					String response = RequestUtil.sendGet(OPERATION_RESULT_URL + operationid, null);
					OperateResult result;
					try {
						result = JsonUtils.fromStr(response, OperateResult.class); // parse json
						if (result.getOperationStatus() == 200) {
							// shutdown success
							logger.info("ShutDown instances " + paras + "  success,start to remove !");
							// remove instance
							requestUrl = DOCKER_REMOVE_URL + appId + "/delete";
							String delIdStr = RequestUtil.sendPost(requestUrl, paras);
							int delOperationId = -1;
							try {
								OperationId opid = JsonUtils.fromStr(delIdStr, OperationId.class);
								delOperationId = opid.getOperationId();
							} catch (Exception e) {
								logger.error("ShutDown docker container failed,cause by Parse operationid with error !" + e);
								break;
							}
							while(true){
								response = RequestUtil.sendGet(OPERATION_RESULT_URL + delOperationId, null);
								try{
									result = JsonUtils.fromStr(response, OperateResult.class);
									if(result.getOperationStatus() == 200){
										for(String instance : instances){
											logger.info("Remove docker instances success ! appId : " + appId + " \n instancesId : " + instance);
											serverService.deleteByInstanceId(instance);
										}
										break;
									}else if(result.getOperationStatus() == 500){
										logger.error("Delete instances error,need to remove instance manal !" + result.getLog());
										break;
									}
									Thread.sleep(500);
								} catch (Exception e) {
									logger.error("Delete instances error,need to remove instance manal !" + e);
									break;
								}
							}
							
						} else if (result.getOperationStatus() == 500) {
							logger.error("ShutDown instances " + paras + "  failed,need to remove instance manal !");
							break;
						}
						Thread.sleep(100);
					} catch (Exception e) {
						logger.error("ShutDown instances with Parse OperateResult with error,need to remove instance manal !" + e);
						break;
					}
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	private int sendScaleRequest(String appid,int instances){
		String operationidStr = RequestUtil.sendPost(SCALE_URL + appid + "/scale", "{\"instanceCount\" : "+instances+"}");
		int operationid = -1;
		try {
			OperationId opid = JsonUtils.fromStr(operationidStr, OperationId.class);
			operationid = opid.getOperationId();
		} catch (Exception e) {
			logger.error("Parse operationid with error !" + e);
		}
		return operationid;
	}
	static class OperationId{
		
		private int operationId;

		public int getOperationId() {
			return operationId;
		}

		public void setOperationId(int operationId) {
			this.operationId = operationId;
		}
	}
	
	static class InstanceIdParam{
		String[] instances;

		public String[] getInstances() {
			return instances;
		}

		public void setInstances(String[] instances) {
			this.instances = instances;
		}
	}

}
