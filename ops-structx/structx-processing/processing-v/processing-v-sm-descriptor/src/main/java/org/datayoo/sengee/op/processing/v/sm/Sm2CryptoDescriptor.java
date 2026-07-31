package org.datayoo.sengee.op.processing.v.sm;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.coding.AbstractCodecDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "Sm2Crypto",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,coding,sm")
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
            + "<parameter name=\"mode\" c_Option=\"false\" c_Alias=\"加解密模式\" c_Compox=\"modeCtrl\"></parameter>"
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"编码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待处理列集合\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待处理列\"></parameter>"
            + "</head>"
            + "</parametertable>"
            + "<parameter name=\"publicKey\" c_Option=\"true\" c_Alias=\"公钥\" c_Compox=\"sightx-input\"></parameter>"
            + "<parameter name=\"privateKey\" c_Option=\"true\" c_Alias=\"私钥\" c_Compox=\"sightx-input\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {
        @Compox(id = "modeCtrl",
            parentId = "sightx-combox",
            dataDescriptor = @DataDesc(defaultValue = "encrypt",
                data = "[{\"label\": \"加密\", \"value\": \"encrypt\"}, {\"label\": \"解密\", \"value\": \"decrypt\"}]")),
        @Compox(id = "workingModeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "overwrite",
                data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))
    })
public class Sm2CryptoDescriptor extends AbstractCodecDescriptor {

  public static final String PARAM_MODE = "mode";
  public static final String MODE_ENCRYPT = "encrypt";
  public static final String MODE_DECRYPT = "decrypt";
  public static final String PARAM_PUBLIC_KEY = "publicKey";
  public static final String PARAM_PRIVATE_KEY = "privateKey";
  public static final String COL_SUFFIX = "sm2";

  public Sm2CryptoDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }

  @Override
  protected void validateParameters() {
    super.validateParameters();
    String mode = parameters.getParameterValue(PARAM_MODE, MODE_ENCRYPT);
    if (MODE_ENCRYPT.equals(mode)) {
      String publicKey = parameters.getParameterValue(PARAM_PUBLIC_KEY, "");
      if (publicKey.isEmpty()) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias, PARAM_PUBLIC_KEY));
      }
    } else if (MODE_DECRYPT.equals(mode)) {
      String privateKey = parameters.getParameterValue(PARAM_PRIVATE_KEY, "");
      if (privateKey.isEmpty()) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias, PARAM_PRIVATE_KEY));
      }
    }
  }
}
