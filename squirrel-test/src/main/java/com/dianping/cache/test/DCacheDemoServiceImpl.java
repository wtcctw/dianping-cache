package com.dianping.cache.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import com.dianping.avatar.cache.CacheService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.squirrel.client.impl.dcache.DCacheClient;
import com.qq.cloud.component.dcache.client.api.BatchCacheResult;
import com.qq.cloud.component.dcache.client.api.CacheResult;
import com.qq.cloud.component.dcache.client.api.ConditionStatement;
import com.qq.cloud.component.dcache.client.api.DCacheClientAPI;
import com.qq.cloud.component.dcache.client.api.Record;
import com.qq.cloud.component.dcache.client.api.Statement;
import com.qq.cloud.component.dcache.client.dcache.DCacheConst;
import com.qq.cloud.component.dcache.client.dcache.Op;

@Service(url = "com.dianping.cache.test.DCacheDemoService")
public class DCacheDemoServiceImpl implements DCacheDemoService {

	private static final String FIELD_0 = "field0"; // short type

	private static final String FIELD_1 = "field1"; // int type

	private static final String FIELD_2 = "field2"; // string type

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	private DCacheClientAPI getDCacheClientAPI() {
		// 需指定cache类型，这里是dcache，如果是其他dcache集群，需指定具体集群名称，如dcache-mobile
		return ((DCacheClient) cacheService.getCacheClient("dcache")).getClient();
	}

	public void put() {
		Statement st = new Statement("one");
		st.newRecord().set(FIELD_0, (short) 1).set(FIELD_1, 4).set(FIELD_2, "str");
		try {
			CacheResult result = getDCacheClientAPI().put(st);

			System.out.println(result.getKey() + ", put result:" + result.getRetCode());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void get() {
		Statement st = new Statement("one");
		try {
			CacheResult result = getDCacheClientAPI().get(st);
			// 获取查询的结果
			if (result.getRetCode() == DCacheConst.ET_SUCC) {
				List<Record> dataList = result.getRecordList();
				for (Record record : dataList) {
					short field0 = record.getShort(FIELD_0);
					int field1 = record.getInt(FIELD_1);
					String field2 = record.getString(FIELD_2);

					System.out.println(result.getKey() + ", get result:" + field0 + "," + field1 + "," + field2);
				}
			}
			// 根据返回值处理后续的逻辑

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void getWithCondition() {
		ConditionStatement ct = new ConditionStatement("one");
		// 在主key下查询field1>3 且 field2==str 的记录
		ct.condition().add(FIELD_1, 3, Op.GT).add(FIELD_2, "str");
		try {
			CacheResult result = getDCacheClientAPI().getWithCond(ct);
			// 获取查询的结果
			if (result.getRetCode() == DCacheConst.ET_SUCC) {
				List<Record> dataList = result.getRecordList();
				for (Record record : dataList) {
					short field0 = record.getShort(FIELD_0);
					int field1 = record.getInt(FIELD_1);
					String field2 = record.getString(FIELD_2);

					System.out.println(result.getKey() + ", get result:" + field0 + "," + field1 + "," + field2);
				}
			}
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void update() {
		Statement st = new Statement("one");
		st.newRecord().set(FIELD_2, "new_value");// 设定要更新的字段和值
		try {

			CacheResult result = getDCacheClientAPI().update(st);
			// 获取查询的结果
			if (result.getRetCode() == DCacheConst.ET_SUCC) {
				List<Record> dataList = result.getRecordList();
				for (Record record : dataList) {
					short field0 = record.getShort(FIELD_0);
					int field1 = record.getInt(FIELD_1);
					String field2 = record.getString(FIELD_2);

					System.out.println(result.getKey() + ", get result:" + field0 + "," + field1 + "," + field2);
				}
			}
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void updateWithCondition() {
		ConditionStatement ct = new ConditionStatement("one");

		// 在主key下 field1>3 且 field2==str 的记录
		ct.condition().add(FIELD_1, 3, Op.GT).add(FIELD_2, "str");
		// 设定要更新的字段和值
		ct.newRecord().set(FIELD_2, "new_value");

		try {

			CacheResult result = getDCacheClientAPI().updateWithCond(ct);
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void delete() {
		Statement st = new Statement("one");
		try {
			CacheResult result = getDCacheClientAPI().delete(st);
			// 获取查询的结果
			System.out.println(result.getKey() + ", delete result:" + result.getRetCode());
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void deleteWithCondition() {
		ConditionStatement ct = new ConditionStatement("one");
		// 在主key下field1>3 且 field2==str 的记录
		ct.condition().add(FIELD_1, 3, Op.GT).add(FIELD_2, "str");
		try {
			CacheResult result = getDCacheClientAPI().deleteWithCond(ct);
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void erase() {
		Statement st = new Statement("one");
		try {
			CacheResult result = getDCacheClientAPI().erase(st);
			// 根据返回值处理后续的逻辑
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void batchPut() {
		// 构造第一个主key下面的数据
		Statement st = new Statement("one");
		st.newRecord().set(FIELD_0, (short) 1).set(FIELD_1, 2).set(FIELD_2, "str");

		// 构造第二个主key下面的数据
		Statement st2 = new Statement("two");
		st.newRecord().set(FIELD_0, (short) 1).set(FIELD_1, 2).set(FIELD_2, "str");

		List<Statement> stList = new ArrayList<Statement>(2);
		stList.add(st);
		stList.add(st2);

		try {
			BatchCacheResult result = getDCacheClientAPI().batchPut(stList);
			int returnCode = result.getRetCode();
			if (returnCode == DCacheConst.ET_SUCC) {
				// 添加成功
			} else if (returnCode == DCacheConst.ET_PARTIAL_FAIL) {
				Map<Object, CacheResult> keyResultMap = result.getResultMap();
				Set<Entry<Object, CacheResult>> entrySet = keyResultMap.entrySet();
				for (Entry<Object, CacheResult> entry : entrySet) {
					CacheResult keyResult = entry.getValue();
					Map<Integer, Integer> codeMap = keyResult.getRecordCodeMap();// 获取当前主key下面的失败记录
					if (!codeMap.isEmpty()) {
						// /处理失败的记录
					}
				}
			} else {
				// 其他错误
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void batchUpdate() {
		Statement st1 = new Statement("one");
		// 设定要更新的字段和值
		st1.newRecord().set(FIELD_2, "new_value");

		Statement st2 = new Statement("two");
		// 设定要更新的字段和值
		st2.newRecord().set(FIELD_2, "new_value");

		List<Statement> list = new ArrayList<Statement>(2);
		list.add(st1);
		list.add(st2);

		try {
			BatchCacheResult result = getDCacheClientAPI().batchUpdate(list);
			int returnCode = result.getRetCode();
			if (returnCode == DCacheConst.ET_SUCC) {
				// 成功
			} else if (returnCode == DCacheConst.ET_PARTIAL_FAIL) {
				Map<Object, CacheResult> keyResultMap = result.getResultMap();
				Set<Entry<Object, CacheResult>> entrySet = keyResultMap.entrySet();
				for (Entry<Object, CacheResult> entry : entrySet) {
					CacheResult keyResult = entry.getValue();
					Map<Integer, Integer> codeMap = keyResult.getRecordCodeMap();// 获取当前主key下面的失败记录
					if (!codeMap.isEmpty()) {
						// /处理失败的记录
					}
				}
			} else {
				// 其他错误
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void batchGet() {
		List<Statement> list = new ArrayList<Statement>(2);
		list.add(new Statement("one"));
		list.add(new Statement("two"));
		try {
			BatchCacheResult batchResult = getDCacheClientAPI().batchGet(list);
			if (batchResult.getRetCode() == DCacheConst.ET_SUCC) {
				Map<Object, CacheResult> resultMap = batchResult.getResultMap();
				Set<Entry<Object, CacheResult>> entrySet = resultMap.entrySet();
				for (Entry<Object, CacheResult> entry : entrySet) {
					CacheResult keyResult = entry.getValue();
					List<Record> dataList = keyResult.getRecordList();
					// 处理查询的结果数据
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void batchGetWithCond() {

		ConditionStatement ct1 = new ConditionStatement("one");
		ct1.condition().add(FIELD_1, 3, Op.GT).add(FIELD_2, "str");// 在主key下查询field1>3
																	// 且
																	// field2==str
																	// 的记录

		ConditionStatement ct2 = new ConditionStatement("two");
		ct2.condition().add(FIELD_1, 3, Op.GT).add(FIELD_2, "str");

		List<ConditionStatement> list = new ArrayList<ConditionStatement>();
		list.add(ct1);
		list.add(ct2);

		try {
			BatchCacheResult batchResult = getDCacheClientAPI().batchGetWithCond(list);
			if (batchResult.getRetCode() == DCacheConst.ET_SUCC) {
				Map<Object, CacheResult> resultMap = batchResult.getResultMap();
				Set<Entry<Object, CacheResult>> entrySet = resultMap.entrySet();
				for (Entry<Object, CacheResult> entry : entrySet) {
					CacheResult keyResult = entry.getValue();
					List<Record> dataList = keyResult.getRecordList();
					// 处理查询的结果数据
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
