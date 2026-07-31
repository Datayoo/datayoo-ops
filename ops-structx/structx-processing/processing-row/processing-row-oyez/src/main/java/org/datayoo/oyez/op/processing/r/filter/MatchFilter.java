package org.datayoo.oyez.op.processing.r.filter;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.AbstractResDataDependencyOperator;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.filter.MatchFilterDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@OpDefiner(name = "MatchFilter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_RESOURCE_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class MatchFilter extends AbstractResDataDependencyOperator {

  private String[] srcColumns;
  private String[] dataColumns;
  private int[] srcColumnIndexes;
  private int[] dataColumnIndexes;

  private Set<Integer> dictHashSet;

  public MatchFilter(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void initializeResource(PlRowSet plRowSet) {
    ColumnSetMetadata columnSetMetadata = plRowSet.getColumnSetMetadata();
    for (int i = 0; i < srcColumns.length; i++) {
      srcColumnIndexes[i] = columnSetMetadata.getColumnIndex(srcColumns[i]);
    }
    dictHashSet = new HashSet<>(plRowSet.getRowsCount());
    for (Object[] row : plRowSet.getRows()) {
      Object[] temp = new Object[srcColumnIndexes.length];
      for (int i = 0; i < srcColumnIndexes.length; i++) {
        temp[i] = row[srcColumnIndexes[i]];
      }
      dictHashSet.add(Arrays.hashCode(temp));
    }
  }

  @Override
  protected void innerOperate(PlRowSet plRowSet) {
    int i = 0;
    ColumnSetMetadata columnSetMetadata = plRowSet.getColumnSetMetadata();
    PlRowSet outSet = this.createRowSet(columnSetMetadata);
    for (Object[] row : plRowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        Object[] out = this.innerOperate(columnSetMetadata, i++, row);
        if (out != null) {
          outSet.addRow(out);
        }
      } catch (Throwable var8) {
        if (!this.reportErrorAndRun(row, var8)) {
          throw new OperationInterruptionException(var8);
        }
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] row) {
    if (dataColumnIndexes == null) {
      int j = 0;
      dataColumnIndexes = new int[dataColumns.length];
      for (String dataColumn : dataColumns) {
        dataColumnIndexes[j] = columnSetMetadata.getColumnIndex(dataColumn);
        j++;
      }
    }
    Object[] data = new Object[dataColumns.length];
    int k = 0;
    for (int dataColumnIndex : dataColumnIndexes) {
      data[k] = row[dataColumnIndex];
      k++;
    }
    if (dictHashSet.contains(Arrays.hashCode(data))) {
      return row;
    }
    return null;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) this.parameters.getParameter(
        "columnSet");
    srcColumns = new String[columnSetParameter.getParameters().size()];
    srcColumnIndexes = new int[columnSetParameter.getParameters().size()];
    dataColumns = new String[columnSetParameter.getParameters().size()];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      srcColumns[i] = rowParameter.getParameterValue(
          MatchFilterDescriptor.PARAM_SRC_COLUMN);
      dataColumns[i] = rowParameter.getParameterValue(
          MatchFilterDescriptor.PARAM_COLUMN);
      i++;
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
