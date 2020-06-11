package com.java110.front.smo.feeConfig.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.exception.SMOException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.front.smo.feeConfig.IParkingSpaceCreateFeeSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 查询feeConfig服务类
 */
@Service("parkingSpaceCreateFeeSMOImpl")
public class ParkingSpaceCreateFeeSMOImpl extends AbstractComponentSMO implements IParkingSpaceCreateFeeSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> createFee(IPageData pd) throws SMOException {
        return businessProcess(pd);
    }

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {
        Assert.hasKeyAndValue(paramIn, "communityId", "未包含小区ID");
        Assert.hasKeyAndValue(paramIn, "locationTypeCd", "未包含收费范围");
        Assert.hasKeyAndValue(paramIn, "locationObjId", "未包含收费对象");
        Assert.hasKeyAndValue(paramIn, "configId", "未包含收费项目");
    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ComponentValidateResult result = super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        Map paramMap = BeanConvertUtil.beanCovertMap(result);
        paramIn.putAll(paramMap);

        String apiUrl = ServiceConstant.SERVICE_API_URL + "/api/fee.saveParkingSpaceCreateFee";


        ResponseEntity<String> responseEntity = this.callCenterService(restTemplate, pd, JSONObject.toJSONString(paramIn),
                apiUrl,
                HttpMethod.POST);

        return responseEntity;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}