package org.datayoo.sengee.op.processing.v.coding;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "BaseDecode",
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
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"解码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待解码列集合\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待解码列\"></parameter>"
            + "</head>\n" + "</parametertable>"
            + "<parameter name=\"baseAlgorithm\" c_Option=\"false\" c_Alias=\"base算法\" c_Compox=\"baseAlgorithmCtrl\"></parameter>"
            + "<parameter name=\"outPutMode\" c_Option=\"false\" c_Alias=\"解码输出格式\" c_Compox=\"outPutModeCtrl\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "baseAlgorithmCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "BASE62",
            data =
                "[{\"label\" : \"BASE62\", \"value\" : \"BASE62\"},{\"label\": \"BASE64\", "
                    + "\"value\": \"BASE64\"}]")),
        @Compox(id = "workingModeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "overwrite",
                data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]")),
        @Compox(id = "outPutModeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "string",
                data = "[{\"label\": \"字符串\", \"value\": \"string\"}, {\"label\": \"二进制\", \"value\": \"binary\"}]"))
    })
public class BaseDecodeDescriptor extends AbstractCodecDescriptor {

  public static final String PARAM_BASE_ALGORITHM = "baseAlgorithm";

  public static final String COL_SUFFIX = "base";
  public static final String PARAM_OUTPUT_MODE = "outPutMode";

  public BaseDecodeDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }

  @Override
  protected void validateParameters() {
    super.validateParameters();
  }

  @Override
  protected DataType getOutputDataType() {
    String outPutMode = this.parameters.getParameterValue(PARAM_OUTPUT_MODE,
        "string");
    if (outPutMode.equals("string")) {
      return DataTypeUtils.STRING_TYPE;
    } else {
      return DataTypeUtils.BINARY_TYPE;
    }
  }
}
