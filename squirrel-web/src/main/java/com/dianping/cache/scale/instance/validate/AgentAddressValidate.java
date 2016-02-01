package com.dianping.cache.scale.instance.validate;

import com.dianping.cache.scale.AgentConflictException;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/29.
 */
public class AgentAddressValidate extends AbstractValidate{

    private String slaveAgent;

    public AgentAddressValidate(Validate validator,String slaveAgent) {
        super(validator);
        this.slaveAgent = slaveAgent;
    }

    @Override
    public void validate() throws Exception {
        validator.validate();
        if(slaveAgent.equals(validator.getResult().getInstances().get(0).getAgentIp())){
            throw new AgentConflictException("master slave has the same agent.");
        }
    }

}
