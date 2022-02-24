/*
 * Tencent is pleased to support the open source community by making BK-JOB蓝鲸智云作业平台 available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-JOB蓝鲸智云作业平台 is licensed under the MIT License.
 *
 * License for BK-JOB蓝鲸智云作业平台:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.tencent.bk.job.common.iam.service;

import com.tencent.bk.job.common.app.Scope;
import com.tencent.bk.job.common.constant.ErrorCode;
import com.tencent.bk.job.common.exception.InternalException;
import com.tencent.bk.sdk.iam.constants.CommonResponseCode;
import com.tencent.bk.sdk.iam.dto.PathInfoDTO;
import com.tencent.bk.sdk.iam.dto.callback.request.CallbackRequestDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.CallbackBaseResponseDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.ListAttributeResponseDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.ListAttributeValueResponseDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.ListInstanceByPolicyResponseDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.ListInstanceResponseDTO;
import com.tencent.bk.sdk.iam.dto.callback.response.SearchInstanceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;

@Slf4j
public abstract class BaseIamCallbackService {

    public CallbackBaseResponseDTO getFailResp(Long code, String message) {
        CallbackBaseResponseDTO respDTO = new CallbackBaseResponseDTO();
        respDTO.setCode(code);
        respDTO.setMessage(message);
        return respDTO;
    }

    public CallbackBaseResponseDTO getNotFoundResp(String message) {
        return getFailResp(CommonResponseCode.NOT_FOUND, message);
    }

    public CallbackBaseResponseDTO getNotFoundRespById(String id) {
        String msg = String.format("cannot find resource by id %s, may be deleted", id);
        log.warn(msg);
        return getNotFoundResp(msg);
    }

    /**
     * 根据Job内部业务网Id获取映射为CMDB scope，生成对应的权限中心path节点
     *
     * @param appId         Job内部业务ID
     * @param appIdScopeMap 内部业务Id与Scope映射表
     * @return Path节点
     */
    public PathInfoDTO getPathNodeByAppId(Long appId, Map<Long, Scope> appIdScopeMap) {
        Scope scope = appIdScopeMap.get(appId);
        if (scope == null) {
            FormattingTuple msg = MessageFormatter.format("Cannot find scope by appId {}", appId);
            log.warn(msg.getMessage());
            throw new InternalException(msg.getMessage(), ErrorCode.INTERNAL_ERROR);
        }
        PathInfoDTO rootNode = new PathInfoDTO();
        rootNode.setType(scope.getType());
        rootNode.setId(scope.getId());
        return rootNode;
    }

    protected abstract ListInstanceResponseDTO listInstanceResp(CallbackRequestDTO callbackRequest);

    protected abstract SearchInstanceResponseDTO searchInstanceResp(CallbackRequestDTO callbackRequest);

    protected abstract CallbackBaseResponseDTO fetchInstanceResp(CallbackRequestDTO callbackRequest);

    public CallbackBaseResponseDTO baseCallback(CallbackRequestDTO callbackRequest) {
        CallbackBaseResponseDTO response;
        switch (callbackRequest.getMethod()) {
            case LIST_INSTANCE:
                response = listInstanceResp(callbackRequest);
                break;
            case FETCH_INSTANCE_INFO:
                response = fetchInstanceResp(callbackRequest);
                break;
            case LIST_ATTRIBUTE:
                response = new ListAttributeResponseDTO();
                response.setCode(0L);
                break;
            case LIST_ATTRIBUTE_VALUE:
                response = new ListAttributeValueResponseDTO();
                response.setCode(0L);
                break;
            case LIST_INSTANCE_BY_POLICY:
                response = new ListInstanceByPolicyResponseDTO();
                response.setCode(0L);
                break;
            case SEARCH_INSTANCE:
                response = searchInstanceResp(callbackRequest);
                break;
            default:
                log.error("Unknown callback method!|{}|{}|{}|{}", callbackRequest.getMethod(),
                    callbackRequest.getType(), callbackRequest.getFilter(), callbackRequest.getPage());
                response = new CallbackBaseResponseDTO();
        }
        return response;
    }
}
