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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-05-06T07:15:40.423Z[GMT]")


public class IdsUseObject   {
  @JsonProperty("dataObject")
  private Object dataObject = null;

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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdsUseObject idsUseObject = (IdsUseObject) o;
    return Objects.equals(this.dataObject, idsUseObject.dataObject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataObject);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdsUseObject {\n");
    
    sb.append("    dataObject: ").append(toIndentedString(dataObject)).append("\n");
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
