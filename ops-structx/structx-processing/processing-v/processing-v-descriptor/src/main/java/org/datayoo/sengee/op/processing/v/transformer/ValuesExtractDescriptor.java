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
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 字符串根据正则截取
 *
 * @author hhn
 */
@OpDefiner(name = "ValuesExtract",
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
            + "<parametertable name=\"columnSet\" c_Alias=\"待匹配列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parameter horizontal=\"true\" name=\"regex\" c_Option=\"false\" c_Alias=\"正则表达式\"></parameter>"
            + "</head></parametertable></parametergroup></parameters>",
    compoxes = {})
public class ValuesExtractDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_REGEX = "regex";

  protected String[][] params;

  public ValuesExtractDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    params = new String[columnSetParameter.getParameters().size()][2];
    int i = 0;
    columns = new ArrayList<>(columnSetParameter.getParameters().size());
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String column = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      String regex = rowParameter.getParameterValue(PARAM_REGEX);
      params[i][0] = column;
      params[i][1] = regex;
      columns.add(column);
      i++;
    }
  }

  @Override
  public void validateParameters() {
    super.validateParameters();
    for(int i = 0;i<params.length;i++){
      if(null == params[i][0] || 0 == params[i][0].length()){
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
                PARAM_COLUMN_NAME));
      }
      if(null == params[i][1] || 0 == params[i][1].length()){
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
                PARAM_REGEX));
      }
      try {
        Pattern.compile(params[i][1]);
      } catch (Exception e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
                PARAM_REGEX, params[i][1]), e);
      }
    }
  }
}
