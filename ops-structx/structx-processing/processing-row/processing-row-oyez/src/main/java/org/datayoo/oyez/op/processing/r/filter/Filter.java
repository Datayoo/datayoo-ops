package org.datayoo.oyez.op.processing.r.filter;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.MoqlException;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.r.filter.FilterDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "Filter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class Filter extends BaseProcessOperator {
  private org.datayoo.moql.Filter moqlFilter;

  public Filter(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if (moqlFilter != null) {
      if (!moqlFilter.isBinded()) {
        String[] columns = new String[columnSetMetadata.getColumns().size()];
        for (int j = 0; j < columnSetMetadata.getColumns().size(); j++) {
          columns[j] = columnSetMetadata.getColumn(j).getName();
        }
        moqlFilter.bind(columns);
      }
      if (moqlFilter.isMatch(objects)) {
        return objects;
      }
    }
    return null;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    String condition = parameters.getParameterValue(
        FilterDescriptor.PARAM_CONDITION);
    try {
      moqlFilter = ExpressionFactory.createFilter(condition);
    } catch (MoqlException e) {
      throw new IllegalArgumentException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
          FilterDescriptor.PARAM_CONDITION));
    }
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }
}
