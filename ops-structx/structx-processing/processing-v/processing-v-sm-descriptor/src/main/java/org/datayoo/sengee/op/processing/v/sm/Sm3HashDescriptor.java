package org.datayoo.sengee.op.processing.v.sm;

import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.moql.MoqlException;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.coding.AbstractCodecDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "Sm3Hash",
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
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"编码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待哈希列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待哈希列\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"salt\" c_Option=\"true\" c_Alias=\"盐\" c_Compox=\"sengee-column-input-selector\"></parameter>"
            + "</head></parametertable>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "workingModeCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "overwrite",
            data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))
    })
public class Sm3HashDescriptor extends AbstractCodecDescriptor {

  public static final String COL_SUFFIX = "sm3";
  public static final String PARAM_SALT = "salt";

  public Sm3HashDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
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
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        PARAM_COLUMN_SET);
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String salt = rowParameter.getParameterValue(PARAM_SALT, "");
      if (!salt.isEmpty()) {
        try {
          ExpressionFactory.createExpression(salt);
        } catch (MoqlException e) {
          throw new IllegalArgumentException(
              OperatorsI18nMessageResource.format(
                  OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias, salt));
        }
      }
    }
  }
}
