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

import java.util.regex.Pattern;

@OpDefiner(name = "ValuesReplace",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,transformer")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待替换列名称\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待替换配置\"><head>"
            + "<parameter name=\"regex\" c_Option=\"false\" c_Alias=\"待匹配的正则表达式\"></parameter>"
            + "<parameter name=\"replacement\" c_Option=\"true\" c_Alias=\"替换值\"></parameter>"
            + "</head>" + "</parametertable>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class ValuesReplaceDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_REGEX = "regex";
  public static final String PARAM_REPLACEMENT = "replacement";
  public static final String PARAM_COLUMN_SET = "columnSet";
  private String[][] replaceParams;
  private String columnName;

  public ValuesReplaceDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    TableParameter columnSetParameter = (TableParameter) this.parameters
        .getParameter(PARAM_COLUMN_SET);
    int i = 0;
    columnName = parameters.getParameterValue(PARAM_COLUMN_NAME);
    replaceParams = new String[columnSetParameter.getParameters().size()][2];
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String regex = rowParameter.getParameterValue(PARAM_REGEX);
      String replacement = rowParameter.getParameterValue(PARAM_REPLACEMENT,"");
      replaceParams[i][0] = regex;
      replaceParams[i][1] = replacement;
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
    if (replaceParams.length == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource
          .format(OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
              PARAM_COLUMN_SET));
    }

    for (int i = 0; i < replaceParams.length; i++) {
      try {
        Pattern.compile(replaceParams[i][0]);
      } catch (Exception e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
                PARAM_REGEX, replaceParams[i][0]), e);
      }
    }
  }

}
