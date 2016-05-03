package com.dianping.cache.controller;

import com.dianping.cache.controller.vo.MachineStatus;
import com.dianping.cache.scale.instance.docker.paasbean.MachineStatusBean;
import com.dianping.cache.service.PaasApiService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/4/18.
 */
@Controller
public class PaasMachineController extends AbstractSidebarController {

    @Autowired
    PaasApiService paasApiService;

    private String subside;

    @RequestMapping(value = "/config/physicalMachine")
    public ModelAndView allPhysicalNodes() {
        subside = "physicalmachine";
        return new ModelAndView("config/phymachine",createViewMap());
    }

    @RequestMapping(value = "/config/getMachineInfo")
    @ResponseBody
    public List<MachineStatus> phyNodesData(){
        List<MachineStatus> allMachines = new ArrayList<MachineStatus>();
        List<MachineStatusBean> beans = paasApiService.getMachineStatus();
        for(MachineStatusBean bean : beans){
            allMachines.add(copyPaasMachine(bean));
        }
        return  allMachines;
    }

    private MachineStatus copyPaasMachine(MachineStatusBean bean){
        MachineStatus machine = new MachineStatus();
        try {
            BeanUtils.copyProperties(machine,bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return machine;
    }

    @Override
    protected String getSide() {
        return "config";
    }

    @Override
    public String getSubSide() {
        return subside;
    }
}
