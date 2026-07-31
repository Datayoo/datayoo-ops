package org.datayoo.sengee.op.processing.r.filter;

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

@OpDefiner(name = "FilterByLineNo",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,filter")
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
            + "<parameter name=\"firstNo\" c_Option=\"false\" c_Alias=\"开始行\" c_Compox=\"sightx-uinteger\">1</parameter>"
            + "<parameter name=\"lastNo\" c_Option=\"false\" c_Alias=\"结束行\" c_Compox=\"sightx-integer\">-1</parameter>"
            + "<parameter name=\"invert\" c_Option=\"true\" c_Alias=\"反转过滤\" c_Compox=\"sightx-switch\">false</parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class FilterByLineNoDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_FIRST_NO = "firstNo";

  public static final String PARAM_LAST_NO = "lastNo";

  public static final String INVERT = "invert";

  protected int firstNo;

  protected int lastNo;

  public FilterByLineNoDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    firstNo = parameters.getParameterValueAsInt(PARAM_FIRST_NO, 1);
    lastNo = parameters.getParameterValueAsInt(PARAM_LAST_NO, -1);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  protected void validateParameters() {
    if (firstNo < 1) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_COL_GREATER, this.alias,
          PARAM_FIRST_NO, "1"));
    }
    if (lastNo > 0 && lastNo < firstNo) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_COL_GREATER, this.alias,
          PARAM_LAST_NO, PARAM_FIRST_NO));
    }
  }
}
