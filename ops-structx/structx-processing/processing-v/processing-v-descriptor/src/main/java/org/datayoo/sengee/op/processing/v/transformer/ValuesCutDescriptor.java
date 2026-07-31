package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
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

import java.util.List;

/**
 * 字符串截取
 *
 * @author hhn
 */
@OpDefiner(name = "ValuesCut",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,transformer")
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
            + "<parametertable name=\"columnSet\" c_Alias=\"待剪切列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名称\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parameter name=\"beginIndex\" c_Option=\"false\" c_Alias=\"剪切起始位置\" c_Compox=\"sightx-uinteger\">0</parameter>"
            + "<parameter name=\"endIndex\" c_Option=\"false\" c_Alias=\"剪切结束位置\" c_Compox=\"sightx-integer\">-1</parameter>"
            + "</head>" + "</parametertable></parametergroup></parameters>",
    compoxes = {})
public class ValuesCutDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_BEGIN_INDEX = "beginIndex";
  public static final String PARAM_END_INDEX = "endIndex";

  protected Object[][] params;

  public ValuesCutDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    params = new Object[rowParameters.size()][3];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      String column = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      int beginIndex = rowParameter
          .getParameterValueAsInt(PARAM_BEGIN_INDEX, 0);
      int endIndex = rowParameter.getParameterValueAsInt(PARAM_END_INDEX, -1);
      params[i][0] = column;
      params[i][1] = beginIndex;
      params[i][2] = endIndex;
      i++;
    }
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  public void validateParameters() {
    if (params.length == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource
          .format(OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
              PARAM_COLUMN_SET));
    }
    for (int i = 0; i < params.length; i++) {
      String column = (String) params[i][0];
      int beginIndex = (int) params[i][1];
      int endIndex = (int) params[i][2];
      if (beginIndex < 0) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
                PARAM_BEGIN_INDEX, beginIndex, "≥0"));
      }
      if(endIndex > 0 ){
        if(beginIndex > endIndex){
          throw new OperationRuntimeException(OperatorsI18nMessageResource
              .format(OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
                  PARAM_END_INDEX, endIndex, "≥beginIndex"));
        }
      }
    }
  }
}
