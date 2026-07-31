package org.datayoo.sengee.op.processing.st;

import org.datayoo.datax.sd.*;
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

@OpDefiner(name = "RenameColumnsByPattern",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
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
            + "<parameter name=\"regex\" c_Option=\"false\" c_Alias=\"列名待匹配的正则表达式\"></parameter>"
            + "<parameter name=\"replacement\" c_Option=\"false\" c_Alias=\"列名替换值\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class RenameColumnsByPatternDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_REGEX = "regex";
  public static final String PARAM_REPLACEMENT = "replacement";

  private String regex;

  private String replacement;

  public RenameColumnsByPatternDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    ColumnSetMetadata inputColumnSetMetadata = getInputColumnSetMetadata();
    for (ColumnMetadata columnMetadata : inputColumnSetMetadata.getColumns()) {
      String column = columnMetadata.getName();
      GeneralColumnMetadata generalColumnMetadata = new GeneralColumnMetadata(
          columnMetadata);
      generalColumnMetadata.setName(column.replaceAll(regex, replacement));
      columnSetMetadata.addColumn(generalColumnMetadata);
    }
    return columnSetMetadata;
  }

  @Override
  protected void readParameters() {
    regex = parameters.getParameterValue(PARAM_REGEX);
    replacement = parameters.getParameterValue(PARAM_REPLACEMENT);
  }

  @Override
  protected void validateParameters() {
    try {
      Pattern.compile(regex);
    } catch (Exception e) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource
          .format(OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
              PARAM_REGEX, regex), e);
    }
  }
}
