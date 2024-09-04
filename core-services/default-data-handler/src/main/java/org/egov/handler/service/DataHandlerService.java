package org.egov.handler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final LocalizationUtil localizationUtil;

	private final TenantManagementUtil tenantManagementUtil;

	private final ServiceConfiguration serviceConfig;

	private final ObjectMapper objectMapper;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil, TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig, ObjectMapper objectMapper) {
		this.mdmsV2Util = mdmsV2Util;
		this.localizationUtil = localizationUtil;
		this.tenantManagementUtil = tenantManagementUtil;
		this.serviceConfig = serviceConfig;
		this.objectMapper = objectMapper;
	}

	public void createDefaultData(DefaultDataRequest defaultDataRequest) {
		if (defaultDataRequest.getSchemaCodes() != null) {
			DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.schemaCodes(defaultDataRequest.getSchemaCodes())
					.onlySchemas(defaultDataRequest.getOnlySchemas())
					.build();
			mdmsV2Util.createMdmsData(defaultMdmsDataRequest);
		}

		if (defaultDataRequest.getLocale() != null && defaultDataRequest.getModules() != null) {
			DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.locale(defaultDataRequest.getLocale())
					.modules(defaultDataRequest.getModules())
					.build();
			localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
		}
	}

	public void createTenantConfig(TenantRequest tenantRequest) {
		TenantConfigResponse tenantConfigSearchResponse = tenantManagementUtil.searchTenantConfig(serviceConfig.getDefaultTenantId(), tenantRequest.getRequestInfo());
		List<TenantConfig> tenantConfigList = tenantConfigSearchResponse.getTenantConfigs();

		for (TenantConfig tenantConfig : tenantConfigList) {
			// Set code and name according to target tenant
			tenantConfig.setCode(tenantRequest.getTenant().getCode());
			tenantConfig.setName(tenantRequest.getTenant().getName());

			TenantConfigRequest tenantConfigRequest = TenantConfigRequest.builder()
					.requestInfo(tenantRequest.getRequestInfo())
					.tenantConfig(tenantConfig)
					.build();

			tenantManagementUtil.createTenantConfig(tenantConfigRequest);
		}
	}

	public DefaultDataRequest setupDefaultData(DataSetupRequest dataSetupRequest) {
		Set<String> uniqueIdentifiers = new HashSet<>();
		uniqueIdentifiers.add(dataSetupRequest.getModule());

		MdmsCriteriaV2 mdmsCriteriaV2 = MdmsCriteriaV2.builder()
				.tenantId(serviceConfig.getDefaultTenantId())
				.schemaCode(serviceConfig.getModuleMasterConfig())
				.uniqueIdentifiers(uniqueIdentifiers)
				.build();
		MdmsCriteriaReqV2 mdmsCriteriaReqV2 = MdmsCriteriaReqV2.builder()
				.requestInfo(dataSetupRequest.getRequestInfo())
				.mdmsCriteria(mdmsCriteriaV2)
				.build();

		MdmsResponseV2 mdmsResponseV2 = mdmsV2Util.searchMdmsData(mdmsCriteriaReqV2);
		List<Mdms> mdmsList = mdmsResponseV2.getMdms();


		List<String> schemaCodes = new ArrayList<>();
		List<String> modules = new ArrayList<>();

		for (Mdms mdms : mdmsList) {
			try {
				MdmsData mdmsData = objectMapper.treeToValue(mdms.getData(), MdmsData.class);
				processMasterList(mdmsData.getMasterList(), schemaCodes, modules);
			} catch(IOException e) {
				log.error("Failed to parse MDMS data :{}", mdms.getData(), e);
				throw new CustomException("MDMS_DATA_PARSE_FAILED", "Failed to parse mdms data ");
			}
		}

		DefaultDataRequest defaultDataRequest = DefaultDataRequest.builder()
				.requestInfo(dataSetupRequest.getRequestInfo())
				.targetTenantId(dataSetupRequest.getTargetTenantId())
				.schemaCodes(schemaCodes)
				.onlySchemas(dataSetupRequest.getOnlySchemas())
				.build();

		try {
			createDefaultData(defaultDataRequest);
		} catch (Exception e) {
			log.error("Failed to create default data for : {}", dataSetupRequest.getTargetTenantId(), e);
			throw new CustomException("DEFAULT_DATA_CREATE_FAILED", "Failed to create default data ");
		}
		return defaultDataRequest;
	}

	private void processMasterList(List<Master> masterList, List<String> schemaCodes, List<String> modules) {
		for (Master master : masterList) {
			if ("module".equals(master.getType()) ||
					"common".equals(master.getType()) ||
					"config".equals(master.getType())) {
				schemaCodes.add(master.getCode());
			} else if ("localisation".equals(master.getType())) {
				modules.add(master.getCode());
			}
		}
	}
}
