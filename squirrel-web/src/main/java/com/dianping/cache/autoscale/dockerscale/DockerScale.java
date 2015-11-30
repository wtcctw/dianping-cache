package com.dianping.cache.autoscale.dockerscale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.autoscale.AppId;
import com.dianping.cache.autoscale.AutoScale;
import com.dianping.cache.autoscale.Instance;
import com.dianping.cache.autoscale.Result;
import com.dianping.cache.autoscale.dockerscale.DockerResultParse.OperateResult;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.RequestUtil;


public class DockerScale implements AutoScale{
	
	private static Logger logger = LoggerFactory.getLogger(DockerScale.class);
	
	private static final String SCALE_URL = "http://10.3.21.21:8080/api/v1/apps/";
	
	private static final String OPERATION_RESULT_URL = "http://10.3.21.21:8080/api/v1/operations/";
	
	private static final String DOCKER_SHUTDOWN_URL = "http://10.3.21.21:8080/api/v1/instances/app/";
	
	private static final String DOCKER_REMOVE_URL = "http://10.3.21.21:8080/api/v1/instances/app/";
	
	private static final Map<Integer,Result> operationResultMap = new HashMap<Integer,Result>();
	
	private ServerService serverService = SpringLocator.getBean("serverService");

	@Override
	public int scaleUp(AppId appId, int number){
		int operationid = sendScaleRequest(appId.toString(),number);
		Result value = new Result();
		value.setAppId(appId);
		value.setNeed(number);
		operationResultMap.put(operationid, value);
		operate(value, operationid);
		return operationid;
	}

	@Override
	public boolean scaleDown(AppId appId,String address) {
		// TODO Auto-generated method stub
		destroy(appId.toString(), address);
		return true;
	}

	private void operate(final Result value,final int operationId){
		Runnable runnable = new Runnable() {
			@Override
			public void run(){
				while(true){
					String resultstr = RequestUtil.sendGet(OPERATION_RESULT_URL + operationId, null);
					DockerResultParse.parse(value,resultstr);
					if(value.getStatus() == 200){
						for(Instance ins : value.getInstances()){
							serverService.insert(ins.getIp()+":"+value.getAppId().getPort(),
									value.getAppId().toString(), ins.getInstanceid(),
									-2,ins.getAgentip());// -2 未分配
						}
						break;
					}else if(value.getStatus() == 500){
						destroy(value);
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						logger.info("Refresh Dockerscale Result with InterruptedException :" + e);
					}
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	private void destroy(final String appid,final String... instances) {
		
		Runnable runnable = new Runnable(){

			@Override
			public void run() {
				//update servers database ,set type = -1
				for(String instance : instances){
					serverService.setDeleteType(instance);
				}

				String requestUrl = DOCKER_SHUTDOWN_URL + appid + "/shutdown";
				InstanceIdParam instanceIdParam = new InstanceIdParam();
				instanceIdParam.setInstances(instances);
				ObjectMapper objectMapper = new ObjectMapper();
				String paras = null;
				try {
					paras = objectMapper.writeValueAsString(instanceIdParam);
				} catch (Exception e) {
					logger.error("ShutDown docker container failed,caused by Convert instanceIdParam to json error : "
							+ e);
				}
				// send shutdown request
				String opIdStr = RequestUtil.sendPost(requestUrl, paras);
				
				int operationid = -1;
				try {
					OperationId opid = objectMapper.readValue(opIdStr, OperationId.class);
					operationid = opid.getOperationId();
				} catch (Exception e) {
					logger.error("ShutDown docker container failed,cause by Parse operationid with error !" + e);
				}
				while (true) {
					String response = RequestUtil.sendGet(OPERATION_RESULT_URL + operationid, null);
					OperateResult result = null;
					try {
						result = objectMapper.readValue(response, OperateResult.class); // parse json
						if (result.getOperationStatus() == 200) {
							// shutdown success
							logger.info("ShutDown instances " + paras + "  success,start to remove !");
							// remove instance
							requestUrl = DOCKER_REMOVE_URL + appid + "/delete";
							String delIdStr = RequestUtil.sendPost(requestUrl, paras);
							int delOperationId = -1;
							try {
								OperationId opid = objectMapper.readValue(delIdStr, OperationId.class);
								delOperationId = opid.getOperationId();
							} catch (Exception e) {
								logger.error("ShutDown docker container failed,cause by Parse operationid with error !" + e);
							}
							while(true){
								response = RequestUtil.sendGet(OPERATION_RESULT_URL + delOperationId, null);
								try{
									result = objectMapper.readValue(response, OperateResult.class);
									if(result.getOperationStatus() == 200){
										logger.info("Remove docker instances success ! appId : " + appid + " \n instancesId : " + instances);
										for(String instance : instances){
											serverService.deleteByInstanceId(instance);
										}
									}else if(result.getOperationStatus() == 500){
										logger.error("Delete instances error,need to remove instance manal !" + result.getLog());
									}
									Thread.sleep(500);
								} catch (Exception e) {
									logger.error("Delete instances error,need to remove instance manal !" + e);
								}
							}
							
						} else if (result.getOperationStatus() == 500) {
							logger.error("ShutDown instances " + paras
									+ "  failed,need to remove instance manal !");
						}
						Thread.sleep(100);
					} catch (Exception e) {
						logger.error("ShutDown instances with Parse OperateResult with error,need to remove instance manal !"
								+ e);
					}
				}
				
			}
			
		};
		Thread t = new Thread(runnable);
		t.start();
	}

	@Override
	public void destroy(Result value){
		if(value == null || value.getInstances().size() == 0)
			return;

		String[] instances = new String[value.getInstances().size()];
		int index = 0;
		for(Instance ins : value.getInstances()){
			instances[index++] = ins.getInstanceid();
		}
		
		destroy(value.getAppId().toString(),instances);
	}
	
	@Override
	public Result getValue(int operateid,Result value) {
		Result result = operationResultMap.get(operateid);
		result.getInstances().addAll(value.getInstances());
		value = result;
		return value;
	}
	
	@Override
	public Result getValue(int operationId) {
		Result value = new Result();
		String resultstr = RequestUtil.sendGet(OPERATION_RESULT_URL + operationId, null);
		DockerResultParse.parse(value,resultstr);
		return value;
	}

	@Override
	public void destroyByInstanceId(String instanceId) {

	}

	public static void main(String[] arges){
		List<String> des = new ArrayList<String>();
		String[] arr = new String[]{
				"c72e7326bfb78742d39fb1add52ad7c128f5e1e4b8f4c04fc7f78fa5486d9eda"
		};
//		des = Arrays.asList(arr);
//		for(String str : des){
//			destroyStatic("redis10",str);
//		}
	
		String OPERATION_RESULT_URL = "http://10.3.21.21:8080/api/v1/operations/";
		String resultstr = RequestUtil.sendGet(OPERATION_RESULT_URL + 311, null);
		Result result = new Result();
		DockerResultParse.parse(result,resultstr);
		System.out.println(result.getInstances().size());
		for(Instance ins : result.getInstances()){
			System.out.println(ins.getIp() +"  --   " + ins.getAgentip());
			//if(des.contains(ins.getIp())){
				//System.out.println(ins.getInstanceid());
				//destroyStatic("redis10",ins.getInstanceid());
			//}
		}
	}
	
	
	
	private int sendScaleRequest(String appid,int instances){
		String operationidStr = RequestUtil.sendPost(SCALE_URL + appid + "/scale", "{\"instanceCount\" : "+instances+"}");
		ObjectMapper objectMapper = new ObjectMapper();
		int operationid = -1;
		try {
			OperationId opid = objectMapper.readValue(operationidStr, OperationId.class);
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
	@Override
	public Result operation(String appid, int operateid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void destroyStatic(String appid,String[] id){
		
	}
	
	public static List<DockerInfo> info(){
		String requestUrl = "http://10.3.21.21:8080/api/v1/machines/";
		String result = RequestUtil.sendGet(requestUrl, null);
		ObjectMapper objectMapper = new ObjectMapper();
		List<DockerInfo> di = null;
		try {
			di = objectMapper.readValue(result, List.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return di;
	}
	public static void destroyStatic(String appid,String id){
		String requestUrl = DOCKER_SHUTDOWN_URL + appid + "/shutdown";
		InstanceIdParam instanceIdParam = new InstanceIdParam();
		instanceIdParam.setInstances(new String[]{id});
		ObjectMapper objectMapper = new ObjectMapper();
		String paras = null;
		try {
			paras = objectMapper.writeValueAsString(instanceIdParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// send shutdown request
		String opIdStr = RequestUtil.sendPost(requestUrl, paras);

		int operationid = -1;
		try {
			OperationId opid = objectMapper.readValue(opIdStr, OperationId.class);
			operationid = opid.getOperationId();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(id + "      -- failed");
		}
		while (true) {
			String response = RequestUtil.sendGet(OPERATION_RESULT_URL + operationid, null);
			OperateResult result = null;
			try {
				result = objectMapper.readValue(response, OperateResult.class); // parse json
				if (result.getOperationStatus() == 200) {
					// remove instance
					requestUrl = DOCKER_REMOVE_URL + appid + "/delete";
					String delIdStr = RequestUtil.sendPost(requestUrl, paras);
					int delOperationId = -1;
					try {
						OperationId opid = objectMapper.readValue(delIdStr, OperationId.class);
						delOperationId = opid.getOperationId();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					while(true){
						response = RequestUtil.sendGet(OPERATION_RESULT_URL + delOperationId, null);
						try{
							result = objectMapper.readValue(response, OperateResult.class);
							if(result.getOperationStatus() == 200){
								System.out.println(100);
								break;
							}else if(result.getOperationStatus() == 500){
								System.out.println(id + "      -- failed");
								break;
							}
							Thread.sleep(100);
						} catch (Exception e) {
							System.out.println(id + "      -- failed");
							break;
						}
					}
					break;
				} else if (result.getOperationStatus() == 500) {
					System.out.println(id + "      -- failed");
					break;
				}
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println(id + "      -- failed");
				break;
			}
		}
	}
	
}
