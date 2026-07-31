package org.datayoo.sengee.op.processing.v.coding;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataConstraint;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.regex.Pattern;

@OpDefiner(name = "UnixCryptHash",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,coding")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"编码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待哈希列集合\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待哈希列\"></parameter>"
            + "</head></parametertable>"
            + "<parameter name=\"key\" c_Option=\"false\" c_Alias=\"加密key\" c_Compox=\"key\" ></parameter>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "key",
        parentId = "sightx-input",
        dataDescriptor = @DataDesc(constraint = @DataConstraint(constraintType = "PATTERN",
            properties = "^[./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]{2,}$"))),
        @Compox(id = "workingModeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "overwrite",
                data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))
    })
public class UnixCryptHashDescriptor extends AbstractCodecDescriptor {

  public static final String PARAM_KEY = "key";

  public static final String COL_SUFFIX = "crypt";

  private final Pattern saltPattern = Pattern.compile(
      "^[./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]{2,}$");

  public UnixCryptHashDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }

  protected void validateParameters() {
    String key = parameters.getParameterValue(PARAM_KEY, "");
    if (key.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias, PARAM_KEY));
    }
    if (!saltPattern.matcher(key).find()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_INVALID_FORMAT, this.alias,
          PARAM_KEY));
    }
  }

}
