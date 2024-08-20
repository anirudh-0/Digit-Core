package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Schema(description = "Details of Tenant Document")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("fileStoreId")
    private String fileStoreId = null;

    @JsonProperty("url")
    private String url = null;


}
