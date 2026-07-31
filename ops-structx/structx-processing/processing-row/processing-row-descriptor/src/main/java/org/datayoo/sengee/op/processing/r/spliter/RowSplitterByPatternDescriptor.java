package org.datayoo.sengee.op.processing.r.spliter;

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

/**
 * 过滤
 *
 * @author
 */
@OpDefiner(name = "RowSplitterByPattern",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,splitter")
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
            + "<parametertable name=\"columnSet\" c_Alias=\"待拆分列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名称\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "</head></parametertable>"
            + "<parameter name=\"pattern\" c_Option=\"false\" c_Alias=\"行拆分表达式\" c_Compox=\"sightx-input\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class RowSplitterByPatternDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_PATTERN = "pattern";

  private String pattern;

  public RowSplitterByPatternDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  protected void readParameters() {
    super.readParameters();
    pattern = parameters.getParameterValue(PARAM_PATTERN, "");
  }

  @Override
  public void validateParameters() {
    super.validateParameters();
    if (pattern.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_PATTERN));
    }
    try {
      Pattern p = Pattern.compile(pattern);
    } catch (Throwable t) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
          PARAM_PATTERN, pattern), t);
    }
  }

}
