package org.datayoo.sengee.op.processing.r.filter;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.engine.MoqlEngine;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 过滤
 *
 * @author hhn
 */
@OpDefiner(name = "Filter",
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
            + "<parameter name=\"filter\" c_Option=\"false\" c_Alias=\"条件\" c_Compox=\"sengee-sql-filter\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class FilterDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_CONDITION = "filter";

  private String condition;

  public FilterDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  protected void readParameters() {
    condition = parameters.getParameterValue(PARAM_CONDITION, "");
  }

  @Override
  public void validateParameters() {
    if (condition.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_CONDITION));
    }
    try {
      ExpressionFactory.createFilter(condition);
    } catch (MoqlException e) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
          PARAM_CONDITION, condition), e);
    }
  }

}
