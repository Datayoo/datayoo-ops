package org.datayoo.oyez.op.processing.r.sampling;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.r.sampling.SampleCtrl;
import org.datayoo.sengee.op.processing.r.sampling.SamplingDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 分层抽样
 *
 * @author he
 * @date 2021-11-18
 */
@OpDefiner(name = "Sampling",
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
public class Sampling extends BaseProcessOperator {

  private SampleCtrl sample;

  private int sampleSize;

  private double sampleRatio;

  private double sampleProbability;

  private int lineNo;

  LinkedList<Object[]> cacheRows;

  public Sampling(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    this.lineNo = 0;
    this.sample = SampleCtrl
        .valueOf(parameters.getParameterValue(SamplingDescriptor.PARAM_SAMPLE));
    this.sampleSize = parameters
        .getParameterValueAsInt(SamplingDescriptor.PARAM_SAMPLE_SIZE, 0);
    this.sampleRatio = parameters
        .getParameterValueAsDouble(SamplingDescriptor.PARAM_SAMPLE_RATIO, 0.0d);
    this.sampleProbability = parameters
        .getParameterValueAsDouble(SamplingDescriptor.PARAM_SAMPLE_PROBABILITY,
            0.0d);

    cacheRows = new LinkedList<>();
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    if (sample == SampleCtrl.ABSOLUTE) {
      if (lineNo + rowSet.getRowsCount() < sampleSize) {
        outputPort.write(rowSet, inputPort.getWaterMark());
        lineNo += rowSet.getRowsCount();
        return;
      }
    }
    List<Object[]> sampleData;
    if (sample == SampleCtrl.ABSOLUTE) {
      sampleData = absolute(rowSet.getRows());
    } else if (sample == SampleCtrl.RELATIVE) {
      sampleData = relative(rowSet.getRows());
    } else {
      sampleData = probability(rowSet.getRows());
    }

    if (sampleData != null && sampleData.size() > 0) {
      PlRowSet sampleRowSet = createRowSet(rowSet.getColumnSetMetadata());
      sampleRowSet.addRows(sampleData);
      outputPort.write(sampleRowSet, inputPort.getWaterMark());
    }
  }

  private List<Object[]> absolute(List<Object[]> rows) {
    List<Object[]> result = new ArrayList<>();
    for (Object[] row : rows) {
      if (lineNo < sampleSize) {
        result.add(row);
        lineNo++;
      }
    }
    return result;
  }

  private List<Object[]> relative(List<Object[]> rows) {
    int size = (new Double(rows.size() * sampleRatio)).intValue();
    return rows.subList(0, size);
  }

  private List<Object[]> probability(List<Object[]> rows) {
    List<Object[]> result = new LinkedList<>();
    int rowSize = rows.size();
    int size = (new Double(rowSize * sampleProbability)).intValue();
    for (int i = 0; i < size; i++) {
      long nano = System.nanoTime();
      if (nano % 100 < rowSize) {
        result.add(rows.get(i));
      } else {
        setCacheRows(rows.get(i));
      }
    }

    if (result.size() < size) {
      result.addAll(compensate(size - result.size()));
    }

    if (result.size() > size) {
      result = result.subList(0, size);
    }

    return result;
  }

  private void setCacheRows(Object[] row) {
    if (cacheRows.size() < getEngineContext().getRowBatchSize()) {
      cacheRows.add(row);
    } else {
      cacheRows.remove(0);
      cacheRows.add(row);
    }
  }

  private List<Object[]> compensate(int size) {
    List<Object[]> compensateRows = new LinkedList<>();
    int gap = cacheRows.size() / size;
    int i = 1;
    while (compensateRows.size() < size) {
      int index = cacheRows.size() - i;
      if (index < 0) {
        index = cacheRows.size() - 1;
      }
      compensateRows.add(cacheRows.get(index));
      cacheRows.remove(index);
      i += gap;
    }
    return compensateRows;
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
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }
}
