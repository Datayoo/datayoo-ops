package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.base.types.DataTypeName;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 4:12 PM
 */
@OpDefiner(name = "SemiStructuredUpdate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
    },
    inputPorts = {@Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"auto\" c_Option=\"true\" c_Alias=\"是否自动创建对象\" c_Compox=\"sightx-switch\">true</parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待更新列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名称\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"path\" c_Option=\"false\" c_Alias=\"对象路径\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"value\" c_Option=\"false\" c_Alias=\"赋值表达式\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "</head>" + "</parametertable></parametergroup></parameters>",
    compoxes = {})
public class SemiStructuredUpdateDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_PATH = "path";
  public static final String PARAM_VALUE = "value";
  public static final String PARAM_AUTO = "auto";
  private String[][] params;

  public SemiStructuredUpdateDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getPortColumnSetMetadata(SengeeOperatorConstants.PORT_DATA_IN);
  }

  @Override
  protected void readParameters() {
    TableParameter columnSetParameter = this.getColumnSetParameter();
    this.params = new String[columnSetParameter.getParameters().size()][3];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      params[i][0] = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      params[i][1] = rowParameter.getParameterValue(PARAM_PATH);
      params[i][2] = rowParameter.getParameterValue(PARAM_VALUE);
      i++;
    }
  }

  @Override
  protected void validateParameters() {
    readParameters();
    ColumnSetMetadata columnSetMetadata = this.getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_DATA_IN);
    for (String[] param : params) {
      ColumnMetadata columnMetadata = columnSetMetadata.getColumn(param[0]);
      DataTypeName dataTypeName = columnMetadata.getType().getName();
      if (!dataTypeName.equals(DataTypeName.Map)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_COL_INVALID_TYPE, this.alias,
            param[0], "Map"));
      }
    }
  }
}
