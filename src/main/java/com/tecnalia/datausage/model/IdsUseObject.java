package com.tecnalia.datausage.model;


import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * IdsUseObject
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-29T06:29:34.462Z[GMT]")


public class IdsUseObject   {
  @JsonProperty("dataObject")
  private Object dataObject = null;

  @JsonProperty("targetDataUri")
  private String targetDataUri = null;

  @JsonProperty("providerUri")
  private String providerUri = null;

  @JsonProperty("consumerUri")
  private String consumerUri = null;

  @JsonProperty("consuming")
  private Boolean consuming = null;

  public IdsUseObject dataObject(Object dataObject) {
    this.dataObject = dataObject;
    return this;
  }

  /**
   * Get dataObject
   * @return dataObject
   **/
  @Schema(example = "{\"text\":\"Hallo\",\"wert\":5}", description = "")
  
    public Object getDataObject() {
    return dataObject;
  }

  public void setDataObject(Object dataObject) {
    this.dataObject = dataObject;
  }

  public IdsUseObject targetDataUri(String targetDataUri) {
    this.targetDataUri = targetDataUri;
    return this;
  }

  /**
   * Get targetDataUri
   * @return targetDataUri
   **/
  @Schema(example = "http://mdm-connector.ids.isst.fraunhofer.de/artifact/15", description = "")
  
    public String getTargetDataUri() {
    return targetDataUri;
  }

  public void setTargetDataUri(String targetDataUri) {
    this.targetDataUri = targetDataUri;
  }

  public IdsUseObject providerUri(String providerUri) {
    this.providerUri = providerUri;
    return this;
  }

  /**
   * Get providerUri
   * @return providerUri
   **/
  @Schema(example = "http://example.com/party/my-party", description = "")
  
    public String getProviderUri() {
    return providerUri;
  }

  public void setProviderUri(String providerUri) {
    this.providerUri = providerUri;
  }

  public IdsUseObject consumerUri(String consumerUri) {
    this.consumerUri = consumerUri;
    return this;
  }

  /**
   * Get consumerUri
   * @return consumerUri
   **/
  @Schema(example = "http://example.com/party/consumer-party", description = "")
  
    public String getConsumerUri() {
    return consumerUri;
  }

  public void setConsumerUri(String consumerUri) {
    this.consumerUri = consumerUri;
  }

  public IdsUseObject consuming(Boolean consuming) {
    this.consuming = consuming;
    return this;
  }

  /**
   * Get consuming
   * @return consuming
   **/
  @Schema(example = "true", description = "")
  
    public Boolean isConsuming() {
    return consuming;
  }

  public void setConsuming(Boolean consuming) {
    this.consuming = consuming;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdsUseObject idsUseObject = (IdsUseObject) o;
    return Objects.equals(this.dataObject, idsUseObject.dataObject) &&
        Objects.equals(this.targetDataUri, idsUseObject.targetDataUri) &&
        Objects.equals(this.providerUri, idsUseObject.providerUri) &&
        Objects.equals(this.consumerUri, idsUseObject.consumerUri) &&
        Objects.equals(this.consuming, idsUseObject.consuming);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataObject, targetDataUri, providerUri, consumerUri, consuming);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdsUseObject {\n");
    
    sb.append("    dataObject: ").append(toIndentedString(dataObject)).append("\n");
    sb.append("    targetDataUri: ").append(toIndentedString(targetDataUri)).append("\n");
    sb.append("    providerUri: ").append(toIndentedString(providerUri)).append("\n");
    sb.append("    consumerUri: ").append(toIndentedString(consumerUri)).append("\n");
    sb.append("    consuming: ").append(toIndentedString(consuming)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
