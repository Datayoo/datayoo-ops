package org.datayoo.oyez.op.processing.r;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.ColumnStatFeature;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.datax.util.sd.ColumnFeatureStatisticor;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.r.FeatureStatisticorDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.wangee.job.RunningState;

import java.util.LinkedList;
import java.util.List;

@OpDefiner(name = "FeatureStatisticor",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_COLUMN_FEATURE,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class FeatureStatisticor extends BaseProcessOperator {
  private int rowCount;

  private double distinctRatio = FeatureStatisticorDescriptor.DEFAULT_DISTINCT_RATIO;
  private double sampleCount = FeatureStatisticorDescriptor.DEFAULT_SAMPLE_COUNT;

  private int rowOffset = 0;

  private ColumnFeatureStatisticor[] columnFeatureStatisticors;

  public FeatureStatisticor(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = read();
    if (rowCount > 0) {
      if (rowOffset == rowCount)
        return;
      if (rowOffset + rowSet.getRowsCount() > rowCount) {
        rowSet = rebuildRowSet(rowSet);
      }
      rowOffset += rowSet.getRowsCount();
    }
    if (columnFeatureStatisticors == null) {
      buildColumnFeatureStatisticors(
          rowSet.getColumnSetMetadata().getColumnsCount());
    }
    try {
      for (int i = 0; i < columnFeatureStatisticors.length; i++) {
        columnFeatureStatisticors[i].cumulativeStat(rowSet, i, null);
      }
      if (rowCount > 0 && rowOffset == rowCount) {
        write(buildOutputRowSet());
      }
    } catch (Exception e) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_RUNNING_FAILED, this.alias,
          this.getId()), e);
    }
  }

  @Override
  protected void finishOperate(RunningState state, String errorMessage) {
    if (errorMessage == null) {
      if (rowCount < 1 || rowCount > 0 && rowOffset < rowCount) {
        write(buildOutputRowSet());
      }
    }
    super.finishOperate(state, errorMessage);
  }

  protected void buildColumnFeatureStatisticors(int size) {
    columnFeatureStatisticors = new ColumnFeatureStatisticor[size];
    for (int i = 0; i < size; i++) {
      columnFeatureStatisticors[i] = new ColumnFeatureStatisticor();
    }
  }

  protected PlRowSet rebuildRowSet(PlRowSet plRowSet) {
    PlRowSet dupSet = new GeneralPlRowSet(plRowSet.getName(),
        plRowSet.getColumnSetMetadata());
    List<Object[]> rows = new LinkedList<>();
    int offset = rowOffset;
    for (Object[] row : plRowSet.getRows()) {
      rows.add(row);
      if (++offset == rowCount)
        break;
    }
    dupSet.addRows(rows);
    return dupSet;
  }

  protected PlRowSet buildOutputRowSet() {
    PlRowSet plRowSet = createRowSet(
        ColumnSetMetadataLibrary.createColumnFeatureMetadata());
    for (int i = 0; i < columnFeatureStatisticors.length; i++) {
      ColumnStatFeature columnStatFeature = columnFeatureStatisticors[i].getResult();
      Object[] row = new Object[plRowSet.getColumnSetMetadata()
          .getColumnsCount()];
      int j = 0;
      row[j++] = columnStatFeature.getColumnName();
      row[j++] = columnStatFeature.getColumnType().getType();
      row[j++] = columnStatFeature.getValueCount();
      row[j++] = columnStatFeature.getMissingValueCount();
      row[j++] = columnStatFeature.getMissingValueRatio();
      row[j++] = columnStatFeature.getDistinctValueCount();
      row[j++] = columnStatFeature.getDistinctValueRatio();
      row[j++] = columnStatFeature.getMax();
      row[j++] = columnStatFeature.getMin();
      row[j++] = columnStatFeature.getMode();
      row[j++] = columnStatFeature.getMean();
      row[j++] = columnStatFeature.getMedian();
      row[j++] = columnStatFeature.getVariance();
      row[j++] = columnStatFeature.getStandardDeviation();
      row[j++] = columnStatFeature.getSkewness();
      row[j++] = columnStatFeature.getKurtosis();
      if (columnStatFeature.getDistinctValueRatio() <= distinctRatio)
        row[j++] = columnStatFeature.getDistinctValues();
      else {
        int valCount = (int) sampleCount;
        if (sampleCount <= 1.0) {
          valCount = (int) (columnStatFeature.getDistinctValueCount()
              * sampleCount);
        }
        row[j++] = columnStatFeature.getDistinctValues().subList(0, valCount);
      }
      plRowSet.addRow(row);
    }
    return plRowSet;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    rowCount = parameters.getParameterValueAsInt(
        FeatureStatisticorDescriptor.PARAM_ROW_COUNT);
    distinctRatio = parameters.getParameterValueAsDouble(
        FeatureStatisticorDescriptor.PARAM_DISTINCT_RATIO);
    sampleCount = parameters.getParameterValueAsDouble(
        FeatureStatisticorDescriptor.PARAM_SAMPLE_COUNT);
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
