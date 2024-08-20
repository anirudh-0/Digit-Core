package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import digit.web.models.AuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of Tenant Configuration
 */
@Schema(description = "Details of Tenant Configuration")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantConfig {

    @JsonProperty("id")
    @Valid
    private UUID id = null;

    @JsonProperty("defaultLoginType")
    private String defaultLoginType = null;

    @JsonProperty("enableUserBasedLogin")
    private Boolean enableUserBasedLogin = null;

    @JsonProperty("additionalAttributes")
    private Object additionalAttributes = null;

    @JsonProperty("documents")
    private Document documents = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

}